package com.example.termproject;

public class User {
    private String userid;
    private String password;
    private String fullname;
    private String birth;
    private String email;
    private String onoff;

    public User(){
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOnoff() {return onoff;}

    public void setOnoff(String onoff) {
        this.onoff = onoff;
    }
}
