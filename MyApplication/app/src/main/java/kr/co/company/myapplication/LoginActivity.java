package kr.co.company.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.company.myapplication.model.request.MemberJoin;
import kr.co.company.myapplication.model.request.MemberLogin;
import kr.co.company.myapplication.retrofit.RetrofitClient;
import kr.co.company.myapplication.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Retrofit retrofit = RetrofitClient.getClient("http://220.69.208.119:8080");
        apiService = retrofit.create(ApiService.class);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        Button loginButton = findViewById(R.id.loginButton);
        Button signUpButton = findViewById(R.id.signUpButton);

        //테스트용
       loginButton.setOnClickListener(view -> {
            String username = usernameInput.getText().toString();
            String password = passwordInput.getText().toString();

            if (true) {
                Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력해 주세요.", Toast.LENGTH_SHORT).show();
            }
       });

       /*loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MemberLogin memberLogin = new MemberLogin(usernameInput.toString(), passwordInput.toString());


                // API 요청 보내기
                apiService.loginMember(memberLogin).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d("LoginActivity", "로그인 성공");
                            Toast.makeText(LoginActivity.this, "로그인 성공", Toast.LENGTH_SHORT).show();
                            // MainScreenActivity로 이동
                            Intent intent = new Intent(LoginActivity.this, MainScreenActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMessage = "로그인 실패: " + response.message();
                            Log.e("LoginActivity", errorMessage);
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        String errorMessage = "로그인 실패: " + t.getMessage();
                        Log.e("LoginActivity", errorMessage, t);
                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });*/

        signUpButton.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
            startActivity(intent);
        });
    }
}
