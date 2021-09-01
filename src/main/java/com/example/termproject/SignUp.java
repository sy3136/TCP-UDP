package com.example.termproject;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class SignUp extends AppCompatActivity {

    private User user;
    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        Button login = (Button)findViewById(R.id.signupButton);
        user = new User();
        ref = FirebaseDatabase.getInstance().getReference().child("User");

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                EditText username = (EditText)findViewById(R.id.signupUsername);
                EditText password = (EditText)findViewById(R.id.signupPassword);
                EditText fullname = (EditText)findViewById(R.id.signupFullname);
                EditText birth = (EditText)findViewById(R.id.signupBirthday);
                EditText email = (EditText)findViewById(R.id.signupEmail);

                final String sUsername = username.getText().toString();
                final String sPassword = password.getText().toString();
                final String sFullname = fullname.getText().toString();
                final String sBirth = birth.getText().toString();
                final String sEmail = email.getText().toString();
                if(TextUtils.isEmpty(sUsername)) {
                    Toast.makeText(SignUp.this, "Please fill all blanks", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(sPassword)) {
                    Toast.makeText(SignUp.this,"Please fill all blanks", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(sFullname)){
                    Toast.makeText(SignUp.this,"Please fill all blanks", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(sBirth)){
                    Toast.makeText(SignUp.this,"Please fill all blanks", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(sEmail)){
                    Toast.makeText(SignUp.this,"Please fill all blanks", Toast.LENGTH_SHORT).show();
                    return;
                }
                ref.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(sUsername)){
                            Toast.makeText(SignUp.this,"Please use another username",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        user.setUserid(sUsername);
                        user.setPassword(sPassword);
                        user.setFullname(sFullname);
                        user.setBirth(sBirth);
                        user.setEmail(sEmail);
                        user.setOnoff("off");
                        ref.child(user.getUserid()).setValue(user);

                        Intent signupIntent = new Intent(SignUp.this, MainActivity.class);

                        signupIntent.putExtra("Username", sUsername);

                        startActivity(signupIntent);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

            }
        });

    }
}
