package com.example.termproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;


public class postPage extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawerLayout;
    private DatabaseReference ref;
    private StorageReference ref2;
    private static final int PICK_IMAGE = 777;
    Uri currentImageUri;
    boolean check;
    boolean check2;
    private String userName;
    StorageReference ref3;
    DatabaseReference ref4;
    DatabaseReference ref5;
    Uri imageUri = null;
    private Uri downloadUrl = null;
    private Uri profile_url = null;

    private post publicPost;
    private post personalPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_page);
        check = false;
        check2 = false;
        publicPost = new post();
        personalPost = new post();

        Intent postPageIntent = getIntent();
        final String username = postPageIntent.getStringExtra("Username");
        userName = username;

        ref = FirebaseDatabase.getInstance().getReference().child("User");
        ref2 = FirebaseStorage.getInstance().getReference("Images");

        ref3 = FirebaseStorage.getInstance().getReference("Posts");
        ref4 = FirebaseDatabase.getInstance().getReference().child("publicPost");
        ref5 = FirebaseDatabase.getInstance().getReference().child("personalPost"+username);

        Toolbar tb = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(tb);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        Bundle bundle = new Bundle();
        bundle.putString("param1",userName);
        ViewPager2 viewPager2 = findViewById(R.id.viewpager);
        viewPager2.setAdapter(new myFragmentStateAdapter(this,bundle));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.TabLayout);
        TabLayoutMediator tabLayoutMediator = new TabLayoutMediator(tabLayout, viewPager2, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                switch (position){
                    case 0:
                        tab.setText("TCP");
                        break;
                    case 1:
                        tab.setText("UDP");
                        break;
                }
            }
        });
        tabLayoutMediator.attach();

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        final NavigationView navigationView = (NavigationView) findViewById(R.id.drawer);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        final ImageButton image = (ImageButton)header.findViewById(R.id.drawer_image);
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(gallery,PICK_IMAGE);
            }
        });
        final TextView text = (TextView)header.findViewById(R.id.drawer_username);

        Menu menu = navigationView.getMenu();
        final MenuItem fullname = menu.findItem(R.id.navigationFullname);
        final MenuItem birth = menu.findItem(R.id.navigationBirthday);
        final MenuItem email = menu.findItem(R.id.navigationEmail);

        ref.child(username).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                fullname.setTitle("Name: "+user.getFullname());
                birth.setTitle("Birth: "+user.getBirth());
                email.setTitle("E-mail: "+user.getEmail());
                text.setText(user.getUserid());

                StorageReference islandRef = ref2.child("profileImage"+userName);
                final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Uri downloadUr2 = uri;
                        profile_url = downloadUr2;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        image.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, tb, R.string.app_name, R.string.app_name);
        drawerToggle.syncState();

//        ImageButton newPost = (ImageButton)findViewById(R.id.newPost);
//        newPost.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                check2 = true;
//                EditText text = (EditText)findViewById(R.id.editText);
//                final String sText = text.getText().toString();
//                if(TextUtils.isEmpty(sText)) {
//                    Toast.makeText(postPage.this, "Please fill text", Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                long now = System.currentTimeMillis();
//                Date date = new Date(now);
//                SimpleDateFormat sNow = new SimpleDateFormat("HH:mm");
//                final String formatDate = sNow.format(date);
//
//                ref4.addListenerForSingleValueEvent(new ValueEventListener() {
//                    @Override
//                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                        publicPost.setUsername(userName);
//                        publicPost.setContent(sText);
//                        publicPost.setTime(formatDate);
//                        if(profile_url != null){
//                            publicPost.setProfile(profile_url.toString());
//                        }
//                        ref4.push().setValue(publicPost);
//                    }
//                    @Override
//                    public void onCancelled(@NonNull DatabaseError databaseError) {
//                    }
//                });
//                Intent createPostIntent = new Intent(postPage.this, postPage.class);
//                createPostIntent.putExtra("Username",username);
//                startActivity(createPostIntent);
//            }
//        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && data != null && data.getData() != null){
            ImageButton img = (ImageButton)findViewById(R.id.drawer_image);
            currentImageUri = data.getData();
            check = true;
            img.setImageURI(data.getData());
            if(check == true){
                StorageReference stref = ref2.child("profileImage"+userName);
                UploadTask uploadTask = stref.putFile(currentImageUri);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    }
                });
                check = false;
            }
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);

        switch (item.getItemId()){
            case R.id.navigationBirthday:
                break;

            case R.id.navigationEmail:
                break;

            case R.id.navigationFullname:
                break;
        }


        return false;
    }

}
