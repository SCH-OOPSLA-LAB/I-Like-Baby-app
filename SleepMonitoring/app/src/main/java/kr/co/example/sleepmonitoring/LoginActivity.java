package kr.co.example.sleepmonitoring;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.example.sleepmonitoring.model.MemberLogin;
import kr.co.example.sleepmonitoring.retrofit.RetrofitClient;
import kr.co.example.sleepmonitoring.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {
    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connection);  // 접속 아이디 레이아웃 설정

        EditText connectionIdEditText = findViewById(R.id.editText_connectionId);
        EditText passwordEditText = findViewById(R.id.editText_password); // 비밀번호 입력 필드 추가
        Button connectButton = findViewById(R.id.button_connect);

        Retrofit retrofit = RetrofitClient.getClient("http://220.69.208.119:8080");
        apiService = retrofit.create(ApiService.class);

        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String connectionId = connectionIdEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                Intent intent = new Intent(LoginActivity.this, kr.co.example.sleepmonitoring.CameraActivity.class);
                startActivity(intent);
                finish();

                if (!connectionId.isEmpty() && !password.isEmpty()) {
                    // 기존 코드 주석 처리
                    MemberLogin memberLogin = new MemberLogin(connectionId, password);
                    asdasd(memberLogin);

                    /*// 테스트용 코드 - 바로 MainActivity로 이동
                    Intent intent = new Intent(LoginActivity.this, kr.co.example.sleepmonitoring.CameraActivity.class);
                    startActivity(intent);
                    finish();*/
                } else {
                    Toast.makeText(LoginActivity.this, "접속 아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    void asdasd(MemberLogin memberLogin) {
        // API 요청 보내기
        apiService.loginMember(memberLogin).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("LoginActivity", "로그인 성공");
                    Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                    // MainActivity로 이동
                    Intent intent = new Intent(LoginActivity.this, kr.co.example.sleepmonitoring.CameraActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMessage = "로그인 실패1: " + response.message();
                    Log.e("LoginActivity", errorMessage);

                    Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String errorMessage = "로그인 실패2: " + t.getMessage();
                Log.e("LoginActivity", errorMessage, t);
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}