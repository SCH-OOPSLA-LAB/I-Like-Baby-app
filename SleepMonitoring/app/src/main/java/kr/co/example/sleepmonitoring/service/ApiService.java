package kr.co.example.sleepmonitoring.service;

import kr.co.example.sleepmonitoring.model.DetectionResult;
import kr.co.example.sleepmonitoring.model.DetectionResultAndImage;
import kr.co.example.sleepmonitoring.model.MemberLogin;
import kr.co.example.sleepmonitoring.model.MonitorStatusResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface ApiService {

    @POST("/member/login")
    Call<Void> loginMember(@Body MemberLogin memberLogin);

    // 프레임 전송을 위한 API 추가
    @POST("/uploadFrame")
    Call<Void> uploadFrame(@Body String encodedFrame);

    // Polling
    // 모니터링 요청 없으면 WAIT
    // 모니터링 요청 오면 MONITORING
    @GET("/monitor/check")
    Call<MonitorStatusResponse> checkPolling();

    @POST("/monitor/box")
    Call<Void> sendModelResult(@Body DetectionResult detectionResult);

    @POST("/monitor/boximage")
    Call<Void> sendModelResultAndImage(@Body DetectionResultAndImage detectionResultAndImage);

    @GET("/monitor/monitoring/stop")
    Call<MonitorStatusResponse> stopMonitoring();
}
