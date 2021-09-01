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
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

public class chat_room extends AppCompatActivity implements View.OnClickListener {

    ListView listView;
    ArrayList<MessageItem> messageItems=new ArrayList<>();
    ChatAdapter adapter;
    public static Activity chatActivity;
    private Thread serverThread;
    boolean isHost = false;

    private static final int SERVER_TEXT_UPDATE = 100;
    private static final int CLIENT_TEXT_UPDATE = 200;

    private Button serverJoinButton;//서버접속
    private Button serverTransButton;//서버텍스트전송
    private Button clientTransButton;//클라전송

    private TextView serverIpText;//서버아이피확인
    private TextView serverText;//서버채팅창
    private TextView clientText;//클라채팅창
    private EditText transServerText;
    private EditText transClientText;

    private ScrollView scrollView1;
    private ScrollView scrollView2;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msgg) {
            super.handleMessage(msgg);

            if(serverSocket != null){
                switch (msgg.what) {
                    case SERVER_TEXT_UPDATE: {
                    }
                    break;
                    case CLIENT_TEXT_UPDATE: {
                        String[] split_msg = clientMsg.split(":");

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
                    break;
                }
            }
        }
    };

    //서버세팅
    private ServerSocket serverSocket;
    private Socket socket;
    DataInputStream in;
    DataOutputStream out;
    private String msg;
    private StringBuilder serverMsg = new StringBuilder();
    private StringBuilder clientMsgBuilder = new StringBuilder();
    private Map<String, DataOutputStream> clientsMap = new HashMap<String, DataOutputStream>();

    private String save_nick;

    //클라세팅
    private Socket clientSocket;
    private DataInputStream clientIn;
    private DataOutputStream clientOut;
    private String clientMsg;
    private String nickName;

    // 포트
    private int port;
    private String username;
    private String userId;
    private String address;
    private DatabaseReference ref;
    private DatabaseReference ref2;

    private boolean is_server = false;
    private boolean is_called = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startService(new Intent(this, TerminateService.class));
        setContentView(R.layout.activity_chat_room);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        chatActivity = chat_room.this;
        listView=findViewById(R.id.listview);
        adapter=new ChatAdapter(messageItems,getLayoutInflater());
        listView.setAdapter(adapter);

        if(nickName==null){
            nickName = new Random().nextInt(100) + "";
        }

        if(getIntent().getExtras() != null){
            Intent chatIntent = getIntent();
            port = chatIntent.getIntExtra("portNumber",7777);
            username = chatIntent.getStringExtra("Username");
            userId = chatIntent.getStringExtra("UserId");
            if (port < 7000){
                port += 7000;
            }
        }

        ref = FirebaseDatabase.getInstance().getReference().child("TCP").child("IPADDRESS"+username);

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
                serverCreate();
                joinServer();
                is_called = false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if(getLocalIpAddress() == address)
            Toast.makeText(this, "You are Host!",Toast.LENGTH_SHORT).show();


        clientTransButton = (Button) findViewById(R.id.trans_client_button);
        transClientText = (EditText) findViewById(R.id.trans_client_text);

        clientTransButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.trans_client_button: {
                if(isHost){
                    String msg = nickName + ":" + transClientText.getText();
                    sendMessage(msg);
                    transClientText.setText("");
                }
                else{
                    String msg = nickName + ":" + transClientText.getText();
                    try {
                        if(clientOut != null)
                            clientOut.writeUTF(msg);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    transClientText.setText("");
                }
            }
            break;
        }
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

    public void joinServer() {

        SharedPreferences registerInfo = getSharedPreferences("registerUserName", Context.MODE_PRIVATE);
        nickName = registerInfo.getString("Username", "NULL");

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    clientSocket = new Socket(address, port);
                    clientOut = new DataOutputStream(clientSocket.getOutputStream());
                    clientIn = new DataInputStream(clientSocket.getInputStream());

                    clientOut.writeUTF(nickName);

                    while (clientIn != null) {
                        try {
                            clientMsg = clientIn.readUTF();
                            Log.d("asdf", "client message received");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d("asdf", "client message update");
                        handler.sendEmptyMessage(CLIENT_TEXT_UPDATE);
                    }
                } catch (UnknownHostException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }).start();
    }

    public void serverCreate() {
        Collections.synchronizedMap(clientsMap);
        try {
            if(serverSocket == null){
                serverSocket = new ServerSocket(port);
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (serverSocket != null) {
                        /** XXX 01. 첫번째. 서버가 할일 분담. 계속 접속받는것. */
                        Log.v("", "서버 대기중...");
                        try {
                            socket = serverSocket.accept();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if(socket != null){
                            msg = socket.getInetAddress() + "에서 접속했습니다.\n";
                            String msg = socket.getInetAddress() + ":" + "in";
                            try {
                                if (clientOut != null)
                                    clientOut.writeUTF(msg);
                            } catch (IOException e){
                                e.printStackTrace();
                            }
                        }
                        handler.sendEmptyMessage(SERVER_TEXT_UPDATE);

                        new Thread(new Runnable() {
                            //private DataInputStream in;
                            //private DataOutputStream out;
                            private String nick;

                            @Override
                            public void run() {
                                try {
                                    if(socket != null){
                                        out = new DataOutputStream(socket.getOutputStream());
                                        in = new DataInputStream(socket.getInputStream());
                                        nick = in.readUTF();
                                        addClient(nick, out);
                                        save_nick = nick;
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                try {// 계속 듣기만!!
                                    while (in != null) {
                                        msg = in.readUTF();
                                        sendMessage(msg);
                                        handler.sendEmptyMessage(SERVER_TEXT_UPDATE);
                                        Log.d("asdf", "server message send");
                                    }
                                } catch (IOException e) {
                                    // 사용접속종료시 여기서 에러 발생. 그럼나간거에요.. 여기서 리무브 클라이언트 처리 해줍니다.
                                    Log.v("", "나감.");
                                    removeClient(nick);
                                }
                            }
                        }).start();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addClient(String nick, DataOutputStream out) throws IOException {
        clientsMap.put(nick, out);
    }

    public void removeClient(String nick) {
        clientsMap.remove(nick);
    }

    // 메시지 내용 전파
    public void sendMessage(String msg) {
        Iterator<String> it = clientsMap.keySet().iterator();
        String key = "";
        while (it.hasNext()) {
            key = it.next();
            try {
                clientsMap.get(key).writeUTF(msg);
                Log.v("c","clients"+clientsMap.get(key).size());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        String msg = nickName + ":" + "out";
        try {
            if(clientOut != null)
                clientOut.writeUTF(msg);
            clientOut = null;
            clientIn = null;
            if(!serverSocket.isClosed()){
                serverSocket.close();
                serverSocket = null;
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

                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Back button pressed.", Toast.LENGTH_SHORT).show();
        super.onBackPressed();
        finish();
    }
}