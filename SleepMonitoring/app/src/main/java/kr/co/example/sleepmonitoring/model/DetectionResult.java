package kr.co.example.sleepmonitoring.model;

// 탐지 결과를 저장할 클래스
public class DetectionResult {
    public final float[] boundingBox;  // 좌표 배열로 변경
    public final int classId;
    public final float score;

    public DetectionResult(float[] boundingBox, int classId, float score) {
        this.boundingBox = boundingBox;
        this.classId = classId;
        this.score = score;
    }

    public float[] getBoundingBox() {
        return boundingBox;
    }

    public int getClassId() {
        return classId;
    }

    public float getScore() {
        return score;
    }
}