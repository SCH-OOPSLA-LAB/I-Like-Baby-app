package kr.co.company.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class StartActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Button startButton = findViewById(R.id.startButton);
        startButton.setOnClickListener(view -> {
            // 다음 화면으로 이동
            Intent intent = new Intent(StartActivity.this, LoginActivity.class);
            startActivity(intent);
        });
        System.out.println("sdf");
    }
}
