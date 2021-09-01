package com.example.termproject;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


public class TerminateService  extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) { //핸들링 하는 부분
        Log.e("Error","onTaskRemoved - " + rootIntent);
        chat_room activity = (chat_room)chat_room.chatActivity;
        udp_chat activity2 = (udp_chat) udp_chat.udp_chatActivity;
        activity.finish();
        activity2.finish();
        stopSelf(); //서비스도 같이 종료
    }
}
