package com.example.termproject;

public class MessageItem {

    String name;
    String message;
    String time;
    String nickName;

    public MessageItem(String name, String message, String time, String nickName) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.nickName = nickName;
    }

    public MessageItem() {
    }

    //Getter & Setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }
}