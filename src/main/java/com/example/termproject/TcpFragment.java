package com.example.termproject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TcpFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TcpFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private postAdapter mAdapter;

    private DatabaseReference ref1;
    private DatabaseReference ref2;
    private ArrayList<post> mPosts;
    private String username = null;

    private Bundle bundle;
    private Activity activity;


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TcpFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PersonalFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TcpFragment newInstance(String param1, String param2) {
        TcpFragment fragment = new TcpFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        if(context instanceof Activity){
            activity = (Activity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup rootView = (ViewGroup)inflater.inflate(R.layout.fragment_tcpserver,container,false);
        mRecyclerView = (RecyclerView)rootView.findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mPosts = new ArrayList<>();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        ref1 = FirebaseDatabase.getInstance().getReference("User");

        ref1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull final DataSnapshot dataSnapshot) {
                int cnt = 0;
                for(DataSnapshot postSnapshot : dataSnapshot.getChildren()){
                    post post = postSnapshot.getValue(post.class);
                    post.setTime("port: 700"+Integer.toString(cnt));
                    mPosts.add(post);
                    cnt += 1;
                }
                mAdapter = new postAdapter(getActivity(),mPosts);

                mAdapter.setOnItemClickListener(new postAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int pos, String name){
                        int count = 0;
                        String s = null;
                        for(DataSnapshot data : dataSnapshot.getChildren()){
                            if (count == pos)
                                s = data.getKey();
                            count +=1 ;
                        }
                        Intent chatIntent = new Intent(activity, chat_room.class);
                        chatIntent.putExtra("portNumber", pos);
                        chatIntent.putExtra("Username", name);
                        chatIntent.putExtra("UserId", s);
                        startActivity(chatIntent);
                    }
                });
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



        // Inflate the layout for this fragment
        return rootView;
    }
}
