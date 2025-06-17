package kr.co.example.sleepmonitoring.model;

public class MemberLogin {
    String accountId;
    String password;

    public MemberLogin(String accountId, String password) {
        this.accountId = accountId;
        this.password = password;
    }
}