package kr.co.example.sleepmonitoring;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;

import kr.co.example.sleepmonitoring.model.DetectionResult;
import kr.co.example.sleepmonitoring.model.DetectionResultAndImage;
import kr.co.example.sleepmonitoring.model.MonitorStatusResponse;
import kr.co.example.sleepmonitoring.retrofit.RetrofitClient;
import kr.co.example.sleepmonitoring.service.ApiService;
import kr.co.example.sleepmonitoring.tflite.TFLiteModel;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CameraActivity extends AppCompatActivity {


    String isMonitoring = "초기";

    ApiService apiService;

    @SuppressLint("RestrictedApi")
    androidx.camera.core.Camera camera = null;

    // 카메라 미리보기를 위한 뷰와 버튼 정의
    PreviewView previewView;

    // 카메라 변수 설정
    ProcessCameraProvider processCameraProvider;
    int lensFacing = CameraSelector.LENS_FACING_BACK; // 후면 카메라 사용

    // 필요한 클래스 설정
    private TFLiteModel tfliteModel;

    // 필요한 변수 설정
    private boolean isCameraInitialized = false;



    @SuppressLint({"RestrictedApi", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Retrofit retrofit = RetrofitClient.getClient("http://220.69.208.119:8080");
        apiService = retrofit.create(ApiService.class);

        setContentView(R.layout.activity_main);
        Log.d("CameraActivity", "onCreate called");

        try {
            tfliteModel = new TFLiteModel(this, "model_no_quantization.tflite");
        } catch (IOException e) {
            Log.e("CameraActivity", "Failed to load model file.", e);
        }

        previewView = findViewById(R.id.previewBt);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);

        startPolling();

        initializeCamera();
    }

    private final Handler pollingHandler = new Handler(Looper.getMainLooper());
    private final Runnable pollingTask = new Runnable() {
        @Override
        public void run() {
            apiService.checkPolling().enqueue(new Callback<MonitorStatusResponse>() {
                @Override
                public void onResponse(Call<MonitorStatusResponse> call, Response<MonitorStatusResponse> response) {
                    if (response.isSuccessful()) {
                        isMonitoring = response.body().getMonitorStatus();
                        Log.e("Polling", "Monitor status: " + isMonitoring);
                    } else {
                        Log.e("Polling Error", "Response code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<MonitorStatusResponse> call, Throwable t) {
                    Log.e("Network Error", "Network error: " + t.getMessage());
                }
            });
            // 1초 후에 다시 실행
            pollingHandler.postDelayed(this, 1000);
        }
    };

    private void startPolling() {
        pollingHandler.post(pollingTask); // 폴링 시작
    }


    //-------------------------------- 카메라 프로바이더 초기화 --------------------------------//
    private void initializeCamera() {
        if (isCameraInitialized) {
            Log.d("initializeCamera", "Camera already initialized");
            return;
        }

        String TAG = "initializeCamera";
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                processCameraProvider = cameraProviderFuture.get();
                bindPreview();
                isCameraInitialized = true;
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Camera provider initialization failed", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    //-------------------------------- 카메라 미리보기와 이미지 분석 바인딩 --------------------------------//
    private void bindPreview() {
        String TAG = "bindPreview";
        Log.d(TAG, "Binding camera preview");

        // 카메라 선택자 설정
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(lensFacing)
                .build();

        // Preview 객체 생성
        Preview preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build();

        // ImageAnalysis 설정
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();

        imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new ImageAnalysis.Analyzer() {
            private boolean isProcessing = false; // 현재 처리 중인지 여부
            private final Handler handler = new Handler(Looper.getMainLooper());

            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                if (isProcessing) {
                    // 현재 이미지를 처리 중인 경우, 현재 프레임을 닫습니다.
                    imageProxy.close();
                    return;
                }

                isProcessing = true;

                if (isMonitoring.equals("MONITORING")) {
                    // 5초 후에 다시 처리할 수 있도록 설정
                    handler.postDelayed(() -> isProcessing = false, 500);
                    processFrame(imageProxy, true);
                } else if (isMonitoring.equals("WAIT")){
                    // 1초 후에 다시 처리할 수 있도록 설정
                    handler.postDelayed(() -> isProcessing = false, 1000);
                    processFrame(imageProxy, false);
                }

                // 프레임을 닫아 리소스를 해제
                imageProxy.close();
            }
        });


        // Preview와 ImageAnalysis를 바인딩
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
    }


    //-------------------------------- 프레임 처리(전처리) --------------------------------//
    private void processFrame(ImageProxy imageProxy, boolean isImageFlag) {
        try {
            Bitmap bitmap = imageProxyToBitmap(imageProxy);

            if (bitmap == null) {
                Log.e("processFrame", "Bitmap is null, skipping frame processing");
                return;
            }
            bitmap = rotateBitmap(bitmap, 90);

            DetectionResult result = tfliteModel.runModel(bitmap);

            if (!isImageFlag) { // 박스만
                Log.e("모니터링", "모니터링 중 아님 " + isMonitoring + " flag " + isImageFlag );
                apiService.sendModelResult(result).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.e("API Call", "Successfully sent detection results");
                        } else {
                            Log.e("API Call", "Failed to send detection results - Code: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API Call", "Error: " + t.getMessage());
                    }
                });
            } else { // 이미지도 같이
                Log.e("모니터링", "모니터링 중  박스랑 사진 " + isMonitoring + " flag " + isImageFlag );

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                byte[] byteImage = stream.toByteArray();

                DetectionResultAndImage r = new DetectionResultAndImage(result.getBoundingBox(), result.getClassId(), result.getScore(), byteImage);

                apiService.sendModelResultAndImage(r).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.e("API Call", "Successfully sent detection results");
                        } else {
                            Log.e("API Call", "Failed to send detection results - Code: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("API Call", "Error: " + t.getMessage());
                    }
                });
            }

        } finally {
            imageProxy.close();
        }
    }

    /*private void saveBitmapToFile(Bitmap bitmap) {

        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "image");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = "image_" + System.currentTimeMillis() + ".jpg";
        File file = new File(directory, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.d("SaveBitmap", "Bitmap saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            Log.e("SaveBitmap", "Error saving bitmap", e);
        }
    }*/


    private Bitmap rotateBitmap(Bitmap bitmap, int rotationDegrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(rotationDegrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }


    // ImageProxy 에서 바이트 배열을 가져와 Bitmap 변환
    // 코드에서 ImageProxy의 프레임이 YUV 형식으로 제공
    // 이 YUV 데이터를 RGB 형식으로 변환한 후 Bitmap으로 변환하는 과정
    private Bitmap imageProxyToBitmap(ImageProxy imageProxy) {
        ImageProxy.PlaneProxy[] planes = imageProxy.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();

        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();

        byte[] nv21 = new byte[ySize + uSize + vSize];

        // Copy the Y buffer
        yBuffer.get(nv21, 0, ySize);

        // Copy the U and V buffers
        vBuffer.get(nv21, ySize, vSize);
        uBuffer.get(nv21, ySize + vSize, uSize);

        YuvImage yuvImage = new YuvImage(nv21, ImageFormat.NV21, imageProxy.getWidth(), imageProxy.getHeight(), null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, yuvImage.getWidth(), yuvImage.getHeight()), 100, out);
        byte[] imageBytes = out.toByteArray();
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    }


    //-------------------------------- 사용자 권한 요청 결과를 처리 --------------------------------//
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initializeCamera();
        } else {
            Log.e("CameraActivity", "Camera permission not granted");
        }
    }

    //-------------------------------- 카메라 및 리소스 해제 --------------------------------//
    private void stopCamera() {
        if (processCameraProvider != null) {
            processCameraProvider.unbindAll();
            isCameraInitialized = false;
            Log.d("stopCamera", "Camera stopped and unbound.");
        }
    }


    private void stopPolling() {
        pollingHandler.removeCallbacks(pollingTask); // 폴링 중지
        Log.d("stopPolling", "Polling stopped.");
    }

    // 서버에 stopMonitoring 요청 보내고 액티비티 종료
    private void stopMonitoring() {
        Call<MonitorStatusResponse> stopMonitoringCall = apiService.stopMonitoring();
        stopMonitoringCall.enqueue(new Callback<MonitorStatusResponse>() {
            @Override
            public void onResponse(Call<MonitorStatusResponse> call, Response<MonitorStatusResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("MonitorActivity", "Monitoring stopped successfully: " + response.body());
                } else {
                    Log.e("MonitorActivity", "Failed to stop monitoring: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MonitorStatusResponse> call, Throwable t) {
                Log.e("MonitorActivity", "Error in stopping monitoring: " + t.getMessage());
            }
        });
    }


    //-------------------------------- Activity가 일시 중지될 때 호출 --------------------------------//
    @Override
    protected void onPause() {
        super.onPause();
        stopMonitoring();
        stopCamera();
    }

    //-------------------------------- Activity가 종료될 때 호출 --------------------------------//
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopCamera();
        stopPolling();
        stopMonitoring();
        // TFLite 모델 해제
        if (tfliteModel != null) {
            tfliteModel.close();
            tfliteModel = null;
            Log.d("onDestroy", "TFLite model closed.");
        }
    }
}