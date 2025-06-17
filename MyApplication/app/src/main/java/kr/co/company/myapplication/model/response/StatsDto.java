package kr.co.company.myapplication.model.response;

import java.util.List;

public class StatsDto {
    private List<TimeAndSum> timeAndSums;

    public List<TimeAndSum> getTimeAndSums() {
        return timeAndSums;
    }

    public static class TimeAndSum {
        private String time;
        private int sum;

        public String getTime() {
            return time;
        }

        public int getSum() {
            return sum;
        }
    }
}
