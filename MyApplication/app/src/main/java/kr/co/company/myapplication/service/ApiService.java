package kr.co.company.myapplication.service;

import kr.co.company.myapplication.model.request.MemberJoin;
import kr.co.company.myapplication.model.request.MemberLogin;
import kr.co.company.myapplication.model.request.ChatMessage;
import kr.co.company.myapplication.model.response.ChatResponse;
import kr.co.company.myapplication.model.response.MonitorStatusResponse;
import kr.co.company.myapplication.model.response.StatsDto;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ApiService {
    @POST("/member/login")
    Call<Void> loginMember(@Body MemberLogin memberLogin);

    @POST("/member/join")
    Call<Void> joinMember(@Body MemberJoin memberJoin);

    @POST("/api/clova/chat")
    Call<ChatResponse> sendMessage(@Body ChatMessage chatMessage);

    @Streaming
    @GET("/monitor/image/bumo")
    Call<ResponseBody> getImage();

    @GET("/monitor/monitoring/start")
    Call<MonitorStatusResponse> startMonitoring(@Query("folder") String folder);

    @GET("/monitor/monitoring/stop")
    Call<MonitorStatusResponse> stopMonitoring();

    @GET("/stats/{day}")
    Call<StatsDto> getStats(@Path("day") String day);
}
