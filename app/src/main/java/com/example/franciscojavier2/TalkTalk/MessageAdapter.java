package com.example.franciscojavier2.TalkTalk;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.franciscojavier2.TalkTalk.Model.Messages;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by FranciscoJavier2 on 16/12/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> mMessageList;
    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
    }

    @Override
    public  MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v= LayoutInflater.from(parent.getContext()).inflate(R.layout.messages_layout,parent,false);
       return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView messageText;
        public CircleImageView profileImage;
        public ImageView messageImage;
        public TextView time_text_layout;

        public MessageViewHolder(View view) {
            super(view);
            messageText=(TextView)view.findViewById(R.id.message_text_layout);
            profileImage=(CircleImageView) view.findViewById(R.id.message_profile_layout);
            messageImage=(ImageView)view.findViewById(R.id.message_image_layout);
            time_text_layout=(TextView)view.findViewById(R.id.time_text_layout);
        }
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder viewHolder, int i) {
        Messages c=mMessageList.get(i);
        String from_user=c.getFrom();
        String message_type=c.getType();

        mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mAuth=FirebaseAuth.getInstance();

        String current_user_id=mAuth.getCurrentUser().getUid();

        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String image= dataSnapshot.child("image").getValue().toString();
                String name=dataSnapshot.child("name").getValue().toString();

                Picasso.with(viewHolder.profileImage.getContext()).load(image).placeholder(R.drawable.default_avatar).
                        into(viewHolder.profileImage);
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        if(message_type.equals("text")){
            viewHolder.messageText.setText(c.getMessage());
            viewHolder.messageImage.setVisibility(View.INVISIBLE);
        }else{
            viewHolder.messageText.setVisibility(View.INVISIBLE);
            Picasso.with(viewHolder.profileImage.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.default_avatar).into(viewHolder.messageImage);
        }

        if(from_user.equals(current_user_id)){
            viewHolder.messageText.setTextColor(Color.WHITE);
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background);

        }else{
            viewHolder.messageText.setTextColor(Color.BLACK);
            viewHolder.messageText.setBackgroundResource(R.drawable.message_text_background_ii);
        }
        viewHolder.messageText.setText(c.getMessage());
        SimpleDateFormat sfd=new SimpleDateFormat("dd/MMMM HH:mm");
        viewHolder.time_text_layout.setText( sfd.format(new Date(c.getTime())));

    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

}
