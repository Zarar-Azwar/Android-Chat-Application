package com.example.mychatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mychatapp.Adapter.MessageAdapter;
import com.example.mychatapp.Fragments.APIService;
import com.example.mychatapp.Model.Chat;
import com.example.mychatapp.Model.User;
import com.example.mychatapp.Notification.Client;
import com.example.mychatapp.Notification.Data;
import com.example.mychatapp.Notification.MyResonse;
import com.example.mychatapp.Notification.Sender;
import com.example.mychatapp.Notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {
    private static final String TAG = "MyActivity";
    CircleImageView circleImageView;
    TextView textView;
    MessageAdapter messageAdapter;
    List<Chat> mchat;
    RecyclerView recyclerView;
    ImageButton send_btn;
    EditText edmessage;
    String uid;
    String userid;
    FirebaseUser fuser;
    DatabaseReference reference;
    ValueEventListener seenListner;
    APIService apiService;
    boolean notify=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        circleImageView=(CircleImageView)findViewById(R.id.profile_img);
        textView=(TextView)findViewById(R.id.username);
        send_btn=(ImageButton)findViewById(R.id.sendbutton);
        edmessage=(EditText)findViewById(R.id.txtmessage);

        recyclerView=(RecyclerView)findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(MessageActivity.this,Main2Activity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                }
        );
        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        Intent intent=getIntent();
        userid=intent.getStringExtra("Userid");
        //String uid=userid;
        fuser=FirebaseAuth.getInstance().getCurrentUser();
        reference=FirebaseDatabase.getInstance().getReference("Users").child(userid);
        send_btn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        notify=true;
                        String msg=edmessage.getText().toString();
                        if(!msg.equals("")){
                            sendMessage(fuser.getUid(),userid,msg);
                        }else{
                            Toast.makeText(MessageActivity.this,"You can't send empty message",Toast.LENGTH_SHORT).show();
                        }
                        edmessage.setText("");
                    }
                }
        );
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user=dataSnapshot.getValue(User.class);
                        textView.setText(user.getUsername());
                        if(user.getImageURL().equals("default")){
                            circleImageView.setImageResource(R.mipmap.ic_launcher_round);
                        }
                        else{
                            Glide.with(getApplicationContext()).load(user.getImageURL()).into(circleImageView);
                        }
                        readMessage(fuser.getUid(),userid,user.getImageURL());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
        seenMessage(userid);
    }
    private void seenMessage(final String userid){
        reference=FirebaseDatabase.getInstance().getReference("chats");
        seenListner=reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            Chat chat=snapshot.getValue(Chat.class);
                            if(chat.getReceiver().equals(fuser.getUid())&&chat.getSender().equals(userid)){
                                HashMap<String,Object> hashMap=new HashMap<>();
                                hashMap.put("isseen",true);
                                snapshot.getRef().updateChildren(hashMap);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }
    private void sendMessage(String sender, final String receiver, String message){
        reference=FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap= new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);
        reference.child("chats").push().setValue(hashMap);

        final DatabaseReference chatRef=FirebaseDatabase.getInstance().getReference("chatlist").child(fuser.getUid())
                .child(userid);
        chatRef.addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists()){
                            chatRef.child("id").setValue(userid);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
        final String msg=message;
        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user=dataSnapshot.getValue(User.class);
                        if(notify){
                            sendNotification(receiver,user.getUsername(),msg);
                        }

                        notify=false;
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );

    }

    private void sendNotification(String receiver, final String username, final String message) {
        DatabaseReference tokens=FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Token token=snapshot.getValue(Token.class);
                    Data data=new Data(fuser.getUid(),R.mipmap.ic_launcher,username+":"+message,"New Message",userid);
                    Sender sender=new Sender(data,token.getToken());
                    apiService.sendNotification(sender).enqueue(new Callback<MyResonse>() {
                        @Override
                        public void onResponse(Call<MyResonse> call, Response<MyResonse> response) {
                            if(response.code()==200){
                                if(response.body().success!=1){
                                    Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<MyResonse> call, Throwable t) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void readMessage(final String myid,final String userid ,final  String imgurl){
        mchat=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        mchat.clear();
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            Chat chat=snapshot.getValue(Chat.class);
                            if(chat.getReceiver().equals(myid)&&chat.getSender().equals(userid)||
                                    chat.getSender().equals(myid)&&chat.getReceiver().equals(userid)){
                                mchat.add(chat);
                            }
                            messageAdapter=new MessageAdapter(MessageActivity.this,mchat,imgurl);
                            recyclerView.setAdapter(messageAdapter);

                        }


                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );


    }
    private void currentUser(String userid){
        SharedPreferences.Editor editor=getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser",userid);
        editor.apply();
    }
    private void status(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("status",status);
        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
        currentUser(userid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListner);
        status("offline");
        currentUser("none");
    }
}
