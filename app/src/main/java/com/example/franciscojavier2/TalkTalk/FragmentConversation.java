package com.example.franciscojavier2.TalkTalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.franciscojavier2.TalkTalk.Model.Friends;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 */

public class FragmentConversation extends Fragment {
    private RecyclerView recyclerView;
    private View vista;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mMessages;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    public FragmentConversation() {}// Required empty public constructor

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_conversation, container, false);

        //RecyclerView
        recyclerView =(RecyclerView) vista.findViewById(R.id.conversation_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Firebase Database
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id= mAuth.getCurrentUser().getUid();
        mFriendsDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);

        mMessages=FirebaseDatabase.getInstance().getReference().child("messages").child(mCurrent_user_id);

        return vista;
    }

    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends, ConversationViewholder> conversationRecyclerViewAdapter =
                new FirebaseRecyclerAdapter<Friends, FragmentConversation.ConversationViewholder>(
                        Friends.class,
                        R.layout.conversation_layout,  //Reutilizado este layout.
                        FragmentConversation.ConversationViewholder.class,
                        mFriendsDatabase) {
                    @Override
                    protected void populateViewHolder(final FragmentConversation.ConversationViewholder conversationViewholder,
                                                      Friends friends, int position) {

                        final String list_user_id=getRef(position).getKey();
                        mMessages.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnapshot: dataSnapshot.getChildren()){
                                    String lastMessage=postSnapshot.child("message").getValue().toString();
                                    String from=postSnapshot.child("from").getValue().toString();
                                    Boolean seen=(Boolean)postSnapshot.child("seen").getValue();
                                    if(from.equals(list_user_id)) {
                                        conversationViewholder.setMessage(lastMessage,true,seen);
                                    }else{
                                        conversationViewholder.setMessage(lastMessage,false,seen);
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final String userName=dataSnapshot.child("name").getValue().toString();
                                final String userThumb=dataSnapshot.child("thumb_image").getValue().toString();
                                if(dataSnapshot.hasChild("online")){  //Le pongo esta condición para evitar que mire
                                    //usuarios que no tienen la clave online en la BD. Lo cierto es que ésto
                                    //nunca debería pasar porque los usuarios al registrarse ya se les asigna
                                    //un valor para la clave online en el MainActivity.class.
                                    String userOnline=dataSnapshot.child("online").getValue().toString();
                                    conversationViewholder.setUserOnline(userOnline);
                                }
                                conversationViewholder.setName(userName);
                                conversationViewholder.setThumbImage(userThumb,getContext());
                                conversationViewholder.view.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                                    Intent chatIntent=new Intent(getContext(),ActivityConversation.class);
                                                    chatIntent.putExtra("user_id",list_user_id);
                                                    chatIntent.putExtra("user_name",userName);
                                                    chatIntent.putExtra("user_image",userThumb);
                                                    startActivity(chatIntent);
                                    }
                                });
                                conversationViewholder.view.setOnLongClickListener(new View.OnLongClickListener() {
                                    @Override
                                    public boolean onLongClick(View view) {
                                        CharSequence options[]=new CharSequence[]{getString(R.string.openProfile),
                                                getString(R.string.sendMessage),getString(R.string.deleteChat)};
                                        final AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                                        builder.setTitle("Select Options");
                                        builder.setItems(options,   new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                //Evento click para cada item.
                                                if(i==0){
                                                    Intent profileIntent=new Intent(getContext(),ActivityProfile.class);
                                                    profileIntent.putExtra("user_id",list_user_id);
                                                    startActivity(profileIntent);
                                                }
                                                if(i==1){
                                                    Intent chatIntent=new Intent(getContext(),ActivityConversation.class);
                                                    chatIntent.putExtra("user_id",list_user_id);
                                                    chatIntent.putExtra("user_name",userName);
                                                    chatIntent.putExtra("user_image",userThumb);

                                                    startActivity(chatIntent);
                                                }
                                                if(i==2){
                                                    DeleteChat();
                                                }
                                            }
                                        });
                                        builder.show();
                                        return false;
                                    }
                                });
                            }

                            @Override public void onCancelled(DatabaseError databaseError) {}
                        });

                    }

                };

        recyclerView.setAdapter(conversationRecyclerViewAdapter);

    }

    public void DeleteChat() {
        AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
        alert.setTitle("Delete chat");
        alert.setMessage("Are you sure you want to delete this chat?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alert.show();
    }

    public static class ConversationViewholder extends RecyclerView.ViewHolder{  //Creo una clase que herede de RecyclerView.ViewHolder.
        //Tiene que ser static ya que es una subclase.

        View view;   //view es una vista que he creado ya que quiero q tome el valor de la imagen q pulse para trabajar con ella.

        public ConversationViewholder(View itemView) {
            super(itemView);
            view =itemView;
        }


        public void setName(String name){
            TextView userName=(TextView) view.findViewById(R.id.user_single_name);
            userName.setText(name);
        }

        public void setThumbImage(String thumb_image, Context ctx){
            CircleImageView userPicture=(CircleImageView) view.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userPicture);

        }

        public void setUserOnline(String online_status){
            ImageView userOnlineView=(ImageView) view.findViewById(R.id.user_sing_online_icon);
            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }

        }

        public void setMessage(String message, Boolean messageReceived,Boolean seen){
            TextView userMessage=(TextView) view.findViewById(R.id.user_message);
            if(messageReceived){
                //userMessage.setTextColor(Color.RED);
            }else{
                if(seen){
                    userMessage.setText("\u2713"+"\u2713"+" "+message);
                }else{
                    userMessage.setText("\u2713"+" "+message);
                }
            }
            userMessage.setText(message);

        }

    }

}
