package com.example.franciscojavier2.TalkTalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.example.franciscojavier2.TalkTalk.Model.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityAddContacts extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;
    private ArrayList<String> phoneContacts;
    private ArrayList<String> result = new ArrayList<String>();
    private Button buttonSendRequests;
    private DatabaseReference mFriendreqDatabase;
    private String mCurrent_user_id;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contacts);
        mToolbar  = (Toolbar) findViewById(R.id.addContactsBar);

            //Recieve the ArrayList of users that also have a TalkTalk account.
        phoneContacts = getIntent().getStringArrayListExtra("phoneContacts");

            //Toolbar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.contactsUsingTalkTalk);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            //Authentication
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();

        //Inicialización DatabaseRefence
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendreqDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequest").child("mCurrent_user_id");

        //RecyclerView
        mUsersList=(RecyclerView) findViewById(R.id.contacts_list);
            //Esto se usa en los RecyclerView opcionalmente cuando sabemos que
            //los cambios en el contenido no van a afectar al tamaño del layout para mejorar rendimiento.
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        //Button
        buttonSendRequests = (Button)findViewById(R.id.buttonSendRequests);
        buttonSendRequests.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra("result",result);
                setResult(Activity.RESULT_OK,returnIntent);
                finish();
            }
        });
    }

    public void onStart(){
        super.onStart();
        FirebaseRecyclerAdapter<Users, ContactsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, ContactsViewHolder>(
                Users.class,
                R.layout.users_layout,
                ContactsViewHolder.class,
                mUsersDatabase.orderByChild("name")) {
            @Override
            protected void populateViewHolder(ContactsViewHolder contactsViewHolder, final Users users, int position) {
              String phoneFirebase = users.getPhone();//.replace(" ","");
                boolean found = false;
                for(int i=0; i< phoneContacts.size(); i++){
                       //phoneContacts has the users' phones from my contacts that have a TalkTalk account. I have to compare them with all the phone numbers in Firebase.
                    if(phoneContacts.get(i).equals(phoneFirebase)) {
                        contactsViewHolder.setDisplayName(users.getName());
                        contactsViewHolder.setDisplayStatus(users.getStatus());
                        contactsViewHolder.setUserImage(users.getThumb_image(), getApplicationContext());
                        found = true;
                        contactsViewHolder.ShowView();
                   }else{
                        if(!found){
                            contactsViewHolder.HideView();
                        }
                    }
                }
                final String user_id=getRef(position).getKey();  //Esta línea devuelve el ID del user que pulse.
                contactsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override public void onClick(View view) {
                        result.add(user_id);
                    }
                });
            }
        };
        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }


    public static class ContactsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        TextView userNameView;
        TextView userStatusView;
        CircleImageView userImageView;
        ConstraintLayout userConstraintLayout;
        ImageView userStatusIcon;
        public ContactsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
            userConstraintLayout=(ConstraintLayout)mView.findViewById(R.id.usersConstraintLayout);
            userImageView=(CircleImageView)mView.findViewById(R.id.user_single_image);
            userNameView=(TextView)mView.findViewById(R.id.user_single_name);
            userStatusView=(TextView)mView.findViewById(R.id.user_single_status);
            userStatusIcon=(ImageView)mView.findViewById(R.id.user_sing_online_icon);
        }

        public void HideView(){
            mView.setVisibility(View.GONE);
            userConstraintLayout.setVisibility(View.GONE);
            userImageView.setVisibility(View.GONE);
            userNameView.setVisibility(View.GONE);
            userStatusView.setVisibility(View.GONE);
            userStatusIcon.setVisibility(View.GONE);
        }

        public void setDisplayName(String name) {
            userNameView.setText(name);
        }

        public void setDisplayStatus(String status) {
            userStatusView.setText(status);
        }

        public void setUserImage(String thumb_image, Context ctx) {
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }

        public void ShowView() {
            mView.setVisibility(View.VISIBLE);
            userConstraintLayout.setVisibility(View.VISIBLE);
            userImageView.setVisibility(View.VISIBLE);
            userNameView.setVisibility(View.VISIBLE);
            userStatusView.setVisibility(View.VISIBLE);
        }
    }
}


