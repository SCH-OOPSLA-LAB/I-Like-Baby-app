package kr.co.company.myapplication.model.request;

public class MemberLogin {
    String accountId;
    String password;

    public MemberLogin(String accountId, String password) {
        this.accountId = accountId;
        this.password = password;
    }
}
