package com.example.franciscojavier2.TalkTalk;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

public class FragmentFriends extends Fragment {
    private RecyclerView mFriendList;
    private View mMainView;
    private DatabaseReference mFriendsDatabase;
    private DatabaseReference mUsersDatabase;
    private FirebaseAuth mAuth;
    private String mCurrent_user_id;

    public FragmentFriends() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_friends, container, false);

        //RecyclerView
        mFriendList=(RecyclerView)mMainView.findViewById(R.id.friends_list);
        mFriendList.setHasFixedSize(true);
        mFriendList.setLayoutManager(new LinearLayoutManager(getContext()));

        //Firebase Database
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id= mAuth.getCurrentUser().getUid();
        mFriendsDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mFriendsDatabase.keepSynced(true);
        mUsersDatabase.keepSynced(true);

        return mMainView;
    }

    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> friendsRecyclerViewAdapter =
                new FirebaseRecyclerAdapter<Friends, FragmentFriends.FriendsViewHolder>(
                Friends.class,
                R.layout.users_layout,  //Reutilizado este layout.
                FragmentFriends.FriendsViewHolder.class,
                mFriendsDatabase) {
            @Override
            protected void populateViewHolder(final FragmentFriends.FriendsViewHolder friendsViewHolder,
                                              Friends friends, int position) {

                friendsViewHolder.setDate(friends.getDate());

                final String list_user_id=getRef(position).getKey();

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
                            friendsViewHolder.setUserOnline(userOnline);
                        }

                        friendsViewHolder.setName(userName);
                        friendsViewHolder.setThumbImage(userThumb,getContext());

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CharSequence options[]=new CharSequence[]{"Open Profile","Send message"};
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
                                    }
                                });
                                builder.show();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {}
                });

            }

        };

        mFriendList.setAdapter(friendsRecyclerViewAdapter);

    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder{  //Creo una clase que herede de RecyclerView.ViewHolder.
        //Tiene que ser static ya que es una subclase.

        View mView;   //mView es una vista que he creado ya que quiero q tome el valor de la imagen q pulse para trabajar con ella.

        public FriendsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setDate(String date) {
            TextView userNameView=(TextView)mView.findViewById(R.id.user_single_status);  //Pongo la fecha en un Textview
                        //que inicialmente estaba creado para el status y lo reciclo.
            userNameView.setText(date);
        }

        public void setName(String name){
            TextView userName=(TextView)mView.findViewById(R.id.user_single_name);
            userName.setText(name);
        }

        public void setThumbImage(String thumb_image, Context ctx){
            CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);

        }

        public void setUserOnline(String online_status){
            ImageView userOnlineView=(ImageView)mView.findViewById(R.id.user_sing_online_icon);
            if(online_status.equals("true")){
                userOnlineView.setVisibility(View.VISIBLE);
            }else{
                userOnlineView.setVisibility(View.INVISIBLE);
            }

        }

    }

}
