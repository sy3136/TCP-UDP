package com.example.termproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;


public class udp_chat extends AppCompatActivity {

    public String msg;
    public String msg_client;
    private Button button;
    private EditText transClientText;
    public static Activity udp_chatActivity;

    private int port;
    private String username;
    private String nickName;
    private DatabaseReference ref;
    private DatabaseReference ref2;
    private DatabaseReference ref3;
    private ArrayList<InetAddress> mAddress;
    private String address;
    private String userId;
    boolean isHost = false;

    private String clientMsg;
    ArrayList<MessageItem> messageItems=new ArrayList<>();
    ChatAdapter adapter;
    ListView listView;


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msgg) {
            super.handleMessage(msgg);

            String[] split_msg = msg.split(":");

            if(split_msg.length == 2){
                String name = split_msg[0];
                String content = split_msg[1];

                Calendar calendar= Calendar.getInstance(); //현재 시간을 가지고 있는 객체
                String time=calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE);

                MessageItem messageItem = new MessageItem(name, content, time, nickName);
                messageItems.add(messageItem);
                adapter.notifyDataSetChanged();
                listView.setSelection(messageItems.size()-1); //리스트뷰의 마지막 위치로 스크롤 위치 이동
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, TerminateService.class));
        setContentView(R.layout.activity_udp_chat);
        udp_chatActivity = udp_chat.this;
        SharedPreferences registerInfo = getSharedPreferences("registerUserName", Context.MODE_PRIVATE);
        nickName = registerInfo.getString("Username", "NULL");
        if (getIntent().getExtras() != null) {
            Intent chatIntent = getIntent();
            port = chatIntent.getIntExtra("portNumber", 7777);
            username = chatIntent.getStringExtra("Username");
            userId = chatIntent.getStringExtra("UserId");
            if (port < 7000) {
                port += 7000;
            }
        }
        listView = findViewById(R.id.listview1);
        adapter=new ChatAdapter(messageItems,getLayoutInflater());
        listView.setAdapter(adapter);
        button = (Button) findViewById(R.id.trans_client_button1);
        transClientText = (EditText) findViewById(R.id.trans_client_text1);

        ref = FirebaseDatabase.getInstance().getReference().child("UDP").child("IPADDRESS" + username);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
//                    ref.removeValue();
                    address = dataSnapshot.getValue(String.class);
                }
                else{
                    ref.setValue(getLocalIpAddress());
                    address = getLocalIpAddress();
                    isHost = true;
                }
                ServerUDP();
                msg_client = nickName + ":" + "is IN";
                ClientUDP();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                msg_client = nickName + ":" + transClientText.getText();
                ClientUDP();
                transClientText.setText("");
            }
        });

        if(getLocalIpAddress() == address)
            Toast.makeText(this, "You are Host!",Toast.LENGTH_SHORT).show();

    }

    public void ClientUDP(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    InetAddress serverAddr = InetAddress.getByName(address);
                    Log.d("C", "Connecting...");
                    DatagramSocket socket = new DatagramSocket();
                    byte[] buf = (msg_client).getBytes();

                    DatagramPacket packet = new DatagramPacket(buf, buf.length, serverAddr, port);
                    Log.d("C", "Sending: '" + new String(buf) + "'");

                    socket.send(packet);
                    Log.d("C", "Sent.");

                    socket.receive(packet);
                    if(!isHost){
                        msg = new String(packet.getData(), 0, packet.getLength());
                        handler.sendEmptyMessage(0);
                    }

                } catch (Exception e) {
                    Log.e("C", "Error", e);
                }
            }
        }).start();
    }

    public void ServerUDP() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                    InetAddress serverAddr = null;
                    try {
                        serverAddr = InetAddress.getByName(address);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    }
                    Log.d("S", "Connecting...");
                    DatagramSocket socket = null;
                    try {
                        socket = new DatagramSocket(port, serverAddr);
                    } catch (SocketException e) {
                        e.printStackTrace();
                    }

                    while (isHost) {
                        byte[] buf = new byte[1024];
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        Log.d("S", "Receiving...");
                        try {
                            socket.receive(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        final InetAddress clientAddr = packet.getAddress();
                        int clientPort = packet.getPort();

                        Log.d("S", "Received: '" + new String(packet.getData(), 0, packet.getLength()) + "'");

                        packet = new DatagramPacket(buf, buf.length, clientAddr, clientPort);
                        msg = new String(packet.getData(), 0, packet.getLength());
                        handler.sendEmptyMessage(0);

                        Log.d("UDP", "S: Sending: '"+ "'");
                        try {
                            socket.send(packet);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
        }).start();
    }

    public String getLocalIpAddress() {
        WifiManager wifiMgr = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        String ipAddress = String.format("%d.%d.%d.%d"
                , (ip & 0xff)
                , (ip >> 8 & 0xff)
                , (ip >> 16 & 0xff)
                , (ip >> 24 & 0xff));
        return ipAddress;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        msg_client = nickName + ":" + "is gone.";
        ClientUDP();
        if(isHost){
            ref.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()) {
                        ref.removeValue();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            isHost = false;
        }

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back button pressed.", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
        finish();
    }

}