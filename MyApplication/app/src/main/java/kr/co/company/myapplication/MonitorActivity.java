package kr.co.company.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.util.UUID;

import kr.co.company.myapplication.model.response.MonitorStatusResponse;
import kr.co.company.myapplication.retrofit.RetrofitClient;
import kr.co.company.myapplication.service.ApiService;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MonitorActivity extends AppCompatActivity {

    ApiService apiService;
    private ImageView imageView;
    private Handler handler;
    private Runnable imageFetcherRunnable;
    private int i = 1;
    private String folder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor);

        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(view -> {
            stopMonitoring();  // 모니터링 중지
            // MainScreenActivity로 이동
            Intent intent = new Intent(MonitorActivity.this, MainScreenActivity.class);
            startActivity(intent);
            finish();
        });


        imageView = findViewById(R.id.imageView);

        // Retrofit 초기화
        Retrofit retrofit = RetrofitClient.getClient("http://220.69.208.119:8080");
        apiService = retrofit.create(ApiService.class);

        // 랜덤 8자리 생성
        folder = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        // 서버에 startMonitoring 신호 보내기
        Call<MonitorStatusResponse> monitoringCall = apiService.startMonitoring(folder);
        monitoringCall.enqueue(new Callback<MonitorStatusResponse>() {
            @Override
            public void onResponse(Call<MonitorStatusResponse> call, Response<MonitorStatusResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("MonitorActivity", "Monitoring started successfully: " + response.body());
                } else {
                    Log.e("MonitorActivity", "Failed to start monitoring: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<MonitorStatusResponse> call, Throwable t) {
                Log.e("MonitorActivity", "Error in starting monitoring: " + t.getMessage());
            }
        });

        // Handler와 Runnable 설정 (주기적인 네트워크 요청을 위한)
        handler = new Handler(Looper.getMainLooper());
        imageFetcherRunnable = new Runnable() {
            @Override
            public void run() {
                fetchImageFromServer(); // i 값을 매개변수로 넘기기
                handler.postDelayed(this, 500); // 1초 후에 다시 실행
            }
        };

        handler.postDelayed(imageFetcherRunnable, 1000); // 5초 후에 처음 실행
    }

    private void fetchImageFromServer() {
        Call<ResponseBody> call = apiService.getImage();

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        // 네트워크 작업을 별도의 스레드에서 실행
                        new Thread(() -> {
                            try {
                                // ResponseBody를 byte[]로 변환
                                byte[] imageBytes = response.body().bytes();

                                // Bitmap으로 변환 후 이미지뷰에 표시
                                Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

                                // 메인 스레드에서 UI 업데이트
                                runOnUiThread(() -> imageView.setImageBitmap(bitmap));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }).start();
                    }
                } else {
                    if (response.code() == 400) {
                        // 400 오류가 발생하면 핸들러의 반복 작업을 멈춤
                        Log.e("MonitorActivity", "400 error - stopping image fetch");
                        handler.removeCallbacks(imageFetcherRunnable);
                        stopMonitoring();
                    } else {
                        stopMonitoring();
                        // 다른 오류 처리
                        Log.e("ImageDownloader", "Failed to download image: " + response.message());
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                stopMonitoring();
                Log.e("MonitorActivity", "Error: " + t.getMessage());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopMonitoring();
        handler.removeCallbacks(imageFetcherRunnable);
    }

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

}
