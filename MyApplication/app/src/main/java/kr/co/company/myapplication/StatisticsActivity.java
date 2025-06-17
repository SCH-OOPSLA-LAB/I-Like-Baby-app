package kr.co.company.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;

import kr.co.company.myapplication.model.response.StatsDto;
import kr.co.company.myapplication.retrofit.RetrofitClient;
import kr.co.company.myapplication.service.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StatisticsActivity extends AppCompatActivity {

    private LineChart lineChart;
    private ApiService apiService;

    private Spinner yearSpinner, monthSpinner, daySpinner;
    private String selectedYear, selectedMonth, selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        ImageButton backButton = findViewById(R.id.backButton);

        // Spinner 초기화
        yearSpinner = findViewById(R.id.yearSpinner);
        monthSpinner = findViewById(R.id.monthSpinner);
        daySpinner = findViewById(R.id.daySpinner);

        // 기본 년도, 월, 일 설정 (오늘 날짜 기준)
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // 0부터 시작하므로 1을 더함
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // 스피너에 오늘 날짜 설정
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"2024", "2025", "2026"});
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner.setSelection(yearAdapter.getPosition(String.valueOf(currentYear))); // 오늘 년도 선택

        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"});
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        monthSpinner.setAdapter(monthAdapter);
        monthSpinner.setSelection(currentMonth - 1); // 오늘 월 선택

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"});
        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        daySpinner.setAdapter(dayAdapter);
        daySpinner.setSelection(currentDay - 1); // 오늘 일자 선택

        // Retrofit 초기화
        Retrofit retrofit = RetrofitClient.getClient("http://220.69.208.119:8080");
        apiService = retrofit.create(ApiService.class);

        // 차트 설정
        lineChart = findViewById(R.id.lineChart);

        // Y축 최소값을 1000으로 설정
        lineChart.getAxisLeft().setAxisMinimum(1000f);
        lineChart.getAxisRight().setAxisMinimum(1000f);

        // 기본적으로 오늘 날짜로 설정
        selectedYear = String.valueOf(currentYear);
        selectedMonth = String.format("%02d", currentMonth); // 월을 두 자릿수로 포맷
        selectedDay = String.format("%02d", currentDay); // 일을 두 자릿수로 포맷

        // Spinner의 선택된 항목이 변경될 때마다 호출되는 리스너 설정
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                selectedYear = parentView.getItemAtPosition(position).toString();
                requestStats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                selectedMonth = parentView.getItemAtPosition(position).toString();
                requestStats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, android.view.View selectedItemView, int position, long id) {
                selectedDay = parentView.getItemAtPosition(position).toString();
                requestStats();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });

        // 서버에서 통계 데이터를 가져오기
        requestStats();

        backButton.setOnClickListener(view -> {
            Intent intent = new Intent(StatisticsActivity.this, MainScreenActivity.class);
            startActivity(intent);
        });
    }

    // 서버에서 통계 데이터를 받아오는 메서드
    private void requestStats() {
        String day = selectedYear + selectedMonth + selectedDay;

        getStatsFromServer(day);
    }

    private void getStatsFromServer(String day) {
        Call<StatsDto> call = apiService.getStats(day);

        call.enqueue(new Callback<StatsDto>() {
            @Override
            public void onResponse(Call<StatsDto> call, Response<StatsDto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    StatsDto statsDto = response.body();
                    ArrayList<Entry> entries = new ArrayList<>();

                    // 데이터 처리 및 차트 설정
                    for (int i = 0; i < statsDto.getTimeAndSums().size(); i++) {
                        StatsDto.TimeAndSum data = statsDto.getTimeAndSums().get(i);
                        entries.add(new Entry(i, data.getSum()));
                    }

                    // "Sum per Time"을 빈 문자열로 수정하고, 값 표시를 끄기
                    LineDataSet lineDataSet = new LineDataSet(entries, "");
                    lineDataSet.setColor(getResources().getColor(R.color.colorAccent));
                    lineDataSet.setValueTextColor(getResources().getColor(R.color.black));
                    lineDataSet.setLineWidth(3f);

                    // 그래프의 값 텍스트를 숨기기
                    lineDataSet.setDrawValues(false);

                    LineData lineData = new LineData(lineDataSet);
                    lineChart.setData(lineData);
                    lineChart.invalidate();

                    XAxis xAxis = lineChart.getXAxis();
                    xAxis.setGranularity(1f);
                    xAxis.setAxisMaximum(23);

                    xAxis.setValueFormatter(new IndexAxisValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            int index = (int) value;
                            int minutes = index * 10;
                            int hours = minutes / 60;
                            int mins = minutes % 60;

                            if (mins == 0 || mins == 30) {
                                return String.format("%02d:%02d", hours, mins);
                            } else {
                                return "";
                            }
                        }
                    });
                } else {
                    Toast.makeText(StatisticsActivity.this, "Failed to retrieve data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<StatsDto> call, Throwable t) {
                Toast.makeText(StatisticsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
