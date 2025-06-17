package kr.co.company.myapplication.model.request;

public class MemberJoin {
    String accountId;
    String password;

    public MemberJoin(String accountId, String password) {
        this.accountId = accountId;
        this.password = password;
    }
}
