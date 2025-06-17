package kr.co.example.sleepmonitoring.tflite;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

import kr.co.example.sleepmonitoring.model.DetectionResult;

public class TFLiteModel {
    private Interpreter interpreter;
    private static final int MODEL_INPUT_SIZE = 640; // 입력 크기를 모델에 맞게 조정 (640x640)
    private static final int NUM_DETECTIONS = 51150;   // 모델이 반환하는 최대 객체 수
    private static final float SCORE_THRESHOLD = 0.2f;

    public TFLiteModel(Context context, String modelPath) throws IOException {
        interpreter = new Interpreter(loadModelFile(context, modelPath));
    }

    // 모델 파일을 불러오는 함수
    private MappedByteBuffer loadModelFile(Context context, String modelPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(context.getAssets().openFd(modelPath).getFileDescriptor());
        FileChannel fileChannel = fileInputStream.getChannel();
        long startOffset = context.getAssets().openFd(modelPath).getStartOffset();
        long declaredLength = context.getAssets().openFd(modelPath).getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    static int i = 0;
    public DetectionResult runModel(Bitmap bitmap) {
        // 1. 모델 입력 크기인 640x640으로 Bitmap을 리사이즈합니다.
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE, true);

        // 2. Bitmap에서 픽셀을 가져와 배열로 변환합니다.
        int[] intValues = new int[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE];
        scaledBitmap.getPixels(intValues, 0, MODEL_INPUT_SIZE, 0, 0, MODEL_INPUT_SIZE, MODEL_INPUT_SIZE);

        // 3. 픽셀 값을 UINT8 형식으로 변환해 배열을 만듭니다.
        byte[] inputArray = new byte[MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3];
        for (int i = 0; i < intValues.length; i++) {
            int pixel = intValues[i];
            inputArray[i * 3] = (byte) ((pixel >> 16) & 0xFF); // R 채널
            inputArray[i * 3 + 1] = (byte) ((pixel >> 8) & 0xFF); // G 채널
            inputArray[i * 3 + 2] = (byte) (pixel & 0xFF); // B 채널
        }

        // 4. 4차원 텐서로 변환 (배치 크기 1)
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(1 * MODEL_INPUT_SIZE * MODEL_INPUT_SIZE * 3);
        inputBuffer.put(inputArray);
        inputBuffer.rewind();

        // 5. 모델 출력 배열 생성
        float[][][] outputBoxes = new float[1][NUM_DETECTIONS][4];
        float[][][] outputClassesAndScores = new float[1][NUM_DETECTIONS][3];

        // 6. 추론 입력 및 출력 설정
        Object[] inputs = {inputBuffer};
        Map<Integer, Object> outputs = new HashMap<>();
        try {
            outputs.put(interpreter.getOutputIndex("StatefulPartitionedCall:6"), outputBoxes); // bounding boxes
            outputs.put(interpreter.getOutputIndex("StatefulPartitionedCall:7"), outputClassesAndScores); // classes and scores
        } catch (Exception e) {
            //Log.e("runModel", "Failed to get output index.", e);
        }

        // 7. 추론 실행
        try {
            interpreter.runForMultipleInputsOutputs(inputs, outputs);
        } catch (Exception e) {
            //Log.e("runModel", "Error during model inference.", e);
        }

        // 8. 후처리 - 신뢰도 임계값으로 필터링
        DetectionResult results = postprocessDetections(outputBoxes[0], outputClassesAndScores[0]);
        //Log.d("runModel", "Number of detections after postprocessing: " + results.size());

        return results;
    }


    // 감지 결과를 후처리하는 함수
    private DetectionResult postprocessDetections(
            float[][] boxes, float[][] classesAndScores) {

        //Log.d("postprocessDetections", "Starting postprocessing of detections.");
        //Log.e("박스 길이 ", "" + boxes.length);

        float maxScore = -1;
        DetectionResult detectionResult = null;

        for (int i = 0; i < boxes.length; i++) {
            float score = classesAndScores[i][2];  // 점수는 세 번째 값
            int classId = (int) classesAndScores[i][1];  // 클래스 ID는 두 번째 값
            float[] box = boxes[i];  // Bounding box coordinates

            if ((classId == 0) && (score > maxScore)) {
                maxScore = score;
                detectionResult = new DetectionResult(box, classId, score);
                //Log.d("postprocessDetections", "New best detection found with score: " + maxScore);
            }
        }

        float[] box = detectionResult.getBoundingBox();

        Log.d("postprocessDetections", "Detection " + i + " - Class ID: " + detectionResult.getClassId() +
                ", Score: " + detectionResult.getScore() +
                ", Box: [" + box[0] + ", " + box[1] + ", " + box[2] + ", " + box[3] + "]");

        //Log.d("postprocessDetections", "Total detections after filtering: " + detectionResults.size());
        return detectionResult;
    }

    // Interpreter 해제 메서드
    public void close() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
            Log.d("TFLiteModel", "Interpreter closed.");
        }
    }
}
