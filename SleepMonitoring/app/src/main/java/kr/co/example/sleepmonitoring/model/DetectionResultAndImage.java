package kr.co.example.sleepmonitoring.model;

// 탐지 결과를 저장할 클래스
public class DetectionResultAndImage {
    public final float[] boundingBox;  // 좌표 배열로 변경
    public final int classId;
    public final float score;
    public final byte[] byteImage;

    public DetectionResultAndImage(float[] boundingBox, int classId, float score, byte[] byteImage) {
        this.boundingBox = boundingBox;
        this.classId = classId;
        this.score = score;
        this.byteImage = byteImage;
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

    public byte[] getByteImage() {
        return byteImage;
    }
}