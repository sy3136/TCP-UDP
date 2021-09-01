package com.example.termproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    EditText username;
    EditText password;
    private DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        username = (EditText)findViewById(R.id.userid);
        password = (EditText)findViewById(R.id.password);

        ref = FirebaseDatabase.getInstance().getReference().child("User");

        if(getIntent().getExtras() != null){
            Intent signupIntent = getIntent();
            username.setText(signupIntent.getStringExtra("Username"));
        }
        /*
        Toast.makeText(this,"Auto login to 1",Toast.LENGTH_SHORT).show();
        Intent loginIntent = new Intent(MainActivity.this, postPage.class);
        loginIntent.putExtra("Username", "1");
        startActivity(loginIntent);
         */

        Button login = (Button)findViewById(R.id.loginButton);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String userID = username.getText().toString();
                if(TextUtils.isEmpty(userID)){
                    Toast.makeText(MainActivity.this,"Wrong Username",Toast.LENGTH_SHORT).show();
                    return;
                }
                final String pass = password.getText().toString();

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!(dataSnapshot.hasChild(userID))){
                            Toast.makeText(MainActivity.this,"Wrong Username",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        User user = dataSnapshot.child(userID).getValue(User.class);
                        if(!(pass.equals(user.getPassword()))){
                            Toast.makeText(MainActivity.this,"Wrong Password",Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SharedPreferences registerInfo = getSharedPreferences("registerUserName", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = registerInfo.edit();
                        editor.putString("Username", userID);
                        editor.commit();

                        Intent loginIntent = new Intent(MainActivity.this, postPage.class);
                        loginIntent.putExtra("Username", userID);
                        startActivity(loginIntent);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        });

        TextView signup = (TextView)findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signupIntent = new Intent(MainActivity.this, SignUp.class);
                startActivity(signupIntent);
            }
        });


    }
}
