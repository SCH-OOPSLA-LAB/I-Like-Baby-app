package kr.co.company.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import kr.co.company.myapplication.model.request.ChatMessage;
import kr.co.company.myapplication.model.response.ChatResponse;
import kr.co.company.myapplication.retrofit.RetrofitClient;
import kr.co.company.myapplication.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ChatActivity extends AppCompatActivity {

    ApiService apiService;
    EditText userMessageInput;
    TextView chatBotResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        userMessageInput = findViewById(R.id.userMessageInput);
        chatBotResponse = findViewById(R.id.chatBotResponse);
        Button sendButton = findViewById(R.id.sendButton);
        ImageButton backButton = findViewById(R.id.backButton);

        Retrofit retrofit = RetrofitClient.getClient("http://220.69.208.119:8080");
        apiService = retrofit.create(ApiService.class);

        sendButton.setOnClickListener(view -> {
            String userMessage = userMessageInput.getText().toString();

            if (!userMessage.isEmpty()) {
                sendMessageToChatBot(userMessage);

                // 메시지 전송 후 입력 필드 초기화
                userMessageInput.setText("");

                // 키패드 숨기기
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(userMessageInput.getWindowToken(), 0);
                }
            } else {
                Toast.makeText(ChatActivity.this, "메시지를 입력하세요.", Toast.LENGTH_SHORT).show();
            }
        });

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(ChatActivity.this, MainScreenActivity.class);
            startActivity(intent);
        });
    }

    private void sendMessageToChatBot(String message) {
        // 사용자 메시지를 말풍선 형태로 화면에 표시
        displayUserMessage(message);

        ChatMessage chatMessage = new ChatMessage(message);

        apiService.sendMessage(chatMessage).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                Log.d("ResponseTest",response.code()+" ");
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    displayBotResponse(chatResponse.getMessage());
                } else {
                    Toast.makeText(ChatActivity.this, "챗봇 응답 오류", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                String errorMessage = "로그인 실패: " + t.getMessage();
                Log.e("ChatActivity", errorMessage, t);
                Toast.makeText(ChatActivity.this, "통신 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayBotResponse(String message) {
        TextView botMessage = new TextView(ChatActivity.this);
        botMessage.setText(message);
        botMessage.setTextSize(25); // 텍스트 크기를 25dp로 설정
        botMessage.setTextColor(Color.BLACK);
        botMessage.setBackground(getResources().getDrawable(R.drawable.chatbot_bubble));
        botMessage.setPadding(10, 10, 10, 10);  // 내부 패딩 설정

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 0, 50, 30);
        layoutParams.gravity = Gravity.START;

        botMessage.setLayoutParams(layoutParams);

        LinearLayout layout = findViewById(R.id.chatBotMessageLayout);
        layout.addView(botMessage);
        scrollToBottom();
    }

    // 사용자 메시지를 말풍선 형태로 화면에 표시
    private void displayUserMessage(String message) {
        TextView userMessage = new TextView(ChatActivity.this);
        userMessage.setText(message);
        userMessage.setTextSize(25); // 텍스트 크기를 25dp로 설정
        userMessage.setTextColor(Color.WHITE);
        userMessage.setBackground(getResources().getDrawable(R.drawable.user_bubble));
        userMessage.setPadding(10, 10, 10, 10);  // 내부 패딩 설정

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(50, 0, 0, 30);
        layoutParams.gravity = Gravity.END;

        userMessage.setLayoutParams(layoutParams);

        LinearLayout layout = findViewById(R.id.chatBotMessageLayout);
        layout.addView(userMessage);
        scrollToBottom();
    }

    private void scrollToBottom() {
        ScrollView scrollView = findViewById(R.id.chatScrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));  // 스크롤을 아래로 이동
    }

}