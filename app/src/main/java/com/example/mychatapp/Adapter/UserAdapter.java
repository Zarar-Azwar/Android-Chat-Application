package com.example.mychatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mychatapp.MessageActivity;
import com.example.mychatapp.Model.Chat;
import com.example.mychatapp.Model.User;
import com.example.mychatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.zip.Inflater;


public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private Context mContext;
    private List<User> mUser;
    Intent intent;
    private boolean ischat;
    public String thelastmessage;
    FirebaseUser firebaseUser;

    public UserAdapter(Context mContext, List<User> mUser,boolean ischat) {
        this.mContext = mContext;
        this.mUser = mUser;
        this.ischat=ischat;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user=mUser.get(position);
        holder.uname.setText(user.getUsername());
        if(user.getImageURL().equals("default")){
            holder.profile_img.setImageResource(R.mipmap.ic_launcher_round);
        }else{
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_img);
        }
        if(ischat){
            lastmessage(user.getId(),holder.last_mesg);
        }else {
            holder.last_mesg.setVisibility(View.GONE);
        }
        if(ischat){
            if(user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }else{
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }

        }else {
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }
        holder.itemView.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        intent=new Intent(mContext,MessageActivity.class);
                        intent.putExtra("Userid",user.getId());
                        mContext.startActivity(intent);
                    }
                }
        );

    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView uname;
        public TextView last_mesg;
        public ImageView profile_img;
        public ImageView img_on;
        public ImageView img_off;

        public ViewHolder(View itemView){
            super(itemView);
            uname=(TextView)itemView.findViewById(R.id.username);
            profile_img=(ImageView)itemView.findViewById(R.id.profile_img);
            img_on=(ImageView)itemView.findViewById(R.id.img_on);
            img_off=(ImageView)itemView.findViewById(R.id.img_off);
            last_mesg=(TextView)itemView.findViewById(R.id.last_mesg);

        }
    }
    private void lastmessage(final String userid, final TextView last_mesg){
        thelastmessage="default";
        firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference= FirebaseDatabase.getInstance().getReference("chats");
        reference.addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                            Chat chat=snapshot.getValue(Chat.class);
                            try {
                                if (chat.getSender().equals(userid) && chat.getReceiver().equals(firebaseUser.getUid()) ||
                                        chat.getSender().equals(firebaseUser.getUid()) && chat.getReceiver().equals(userid)
                                ) {
                                    thelastmessage = chat.getMessage();

                                }
                            } catch (Exception e){
                               // Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                            }

                        }
                        switch (thelastmessage){
                            case "default":
                                last_mesg.setText("No last message");
                                break;
                            default:
                                last_mesg.setText(thelastmessage);
                                break;
                        }
                        thelastmessage="default";
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }
        );
    }
}
