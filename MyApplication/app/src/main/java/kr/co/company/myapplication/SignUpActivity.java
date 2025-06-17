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
import kr.co.company.myapplication.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignUpActivity extends AppCompatActivity {

    ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        EditText usernameInput = findViewById(R.id.usernameInput);
        EditText passwordInput = findViewById(R.id.passwordInput);
        EditText emailInput = findViewById(R.id.emailInput);
        Button signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MemberJoin memberJoin = new MemberJoin(usernameInput.toString(), passwordInput.toString());

                apiService.joinMember(memberJoin).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignUpActivity.this, "회원가입 성공", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            String errorMessage = "회원가입 실패: " + response.message();
                            Log.e("SignUpActivity", errorMessage);
                            Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        String errorMessage = "회원가입 실패: " + t.getMessage();
                        Log.e("LoginActivity", errorMessage, t);
                        Toast.makeText(SignUpActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
