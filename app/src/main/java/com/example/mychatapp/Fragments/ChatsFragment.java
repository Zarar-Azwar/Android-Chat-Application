package com.example.mychatapp.Fragments;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.mychatapp.Adapter.UserAdapter;
import com.example.mychatapp.Model.Chat;
import com.example.mychatapp.Model.Chatlist;
import com.example.mychatapp.Model.User;
import com.example.mychatapp.Notification.Token;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.ArrayList;
import java.util.List;


public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    DatabaseReference reference;
    FirebaseUser fuser;
    private List<Chatlist> chatlists;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_chats, container, false);
        recyclerView =view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatlists=new ArrayList<>();
        fuser= FirebaseAuth.getInstance().getCurrentUser();
        reference=FirebaseDatabase.getInstance().getReference("chatlist").child(fuser.getUid());
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        chatlists.clear();
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            Chatlist chatlist=snapshot.getValue(Chatlist.class);
                            chatlists.add(chatlist);
                        }
                        ChatList();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
        /*reference= FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        userslist.clear();
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            Chat chat=snapshot.getValue(Chat.class);
                            if(chat.getSender().equals(fuser.getUid())){
                                userslist.add(chat.getReceiver());
                            }
                            if(chat.getReceiver().equals(fuser.getUid())){
                                userslist.add(chat.getSender());
                            }
                        }
                        readChats();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );*/
        updateToken(FirebaseInstanceId.getInstance().getToken());
        return view;

    }
    private void updateToken(String token){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Token");
        Token token1=new Token(token);
        reference.child(fuser.getUid()).setValue(token);
    }

    private void ChatList() {
        mUsers=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUsers.clear();
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            User user=snapshot.getValue(User.class);
                            for(Chatlist chatlist:chatlists){
                                if(user.getId().equals(chatlist.getId())){
                                    mUsers.add(user);
                                }
                            }
                        }
                        userAdapter=new UserAdapter(getContext(),mUsers,true);
                        recyclerView.setAdapter(userAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }
/*
    private void readChats() {
        mUsers=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mUsers.clear();
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            User user=snapshot.getValue(User.class);
                            for(String id:userslist){
                                if(user.getId().equals(id)){

                                        if(mUsers.size()!=0){
                                            int flag=0;
                                            for(User u : mUsers) {
                                                if (user.getId().equals(u.getId())) {
                                                    flag = 1;
                                                    break;
                                                }
                                            }
                                            if(flag==0)
                                                mUsers.add(user);
                                    }else {
                                        Log.d("my log info", "onDataChange: "+mUsers.size());
                                        mUsers.add(user);
                                    }
                                }
                            }
                        }
                        userAdapter=new UserAdapter(getContext(),mUsers,true);
                        recyclerView.setAdapter(userAdapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }*/

}
