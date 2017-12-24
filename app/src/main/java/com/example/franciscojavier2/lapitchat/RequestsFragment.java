package com.example.franciscojavier2.lapitchat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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

public class RequestsFragment extends Fragment {

    private View mMainView;
    private RecyclerView mRequestList;
    private FirebaseAuth mAuth;
    private DatabaseReference mFriend_reqDatabase,mRootRef;
    private String mCurrent_user_id;

    public RequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_requests, container, false);

        //RecyclerView
        mRequestList=(RecyclerView)mMainView.findViewById(R.id.requests_list);
        mRequestList.setHasFixedSize(true);
        mRequestList.setLayoutManager(new LinearLayoutManager(getContext()));

        //Firebase
        mRootRef=FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mFriend_reqDatabase = FirebaseDatabase.getInstance().getReference().child("Friend_req").child(mCurrent_user_id);

        return mMainView;
    }

    public void onStart() {
        super.onStart();
        FirebaseRecyclerAdapter<Friend_req,RequestsViewHolder> requestsRecyclerViewAdapter =
                new FirebaseRecyclerAdapter<Friend_req, RequestsFragment.RequestsViewHolder>(
                        Friend_req.class,
                        R.layout.users_single_layout,  //Reutilizado este layout.
                        RequestsFragment.RequestsViewHolder.class,
                        mFriend_reqDatabase) {

            @Override
            protected void populateViewHolder(final RequestsFragment.RequestsViewHolder requestsViewHolder,
                                              final Friend_req requests, final int position) {

                final String list_user_id=getRef(position).getKey().toString();
                DatabaseReference DataBaseUser=mRootRef.child("Users").child(list_user_id);

                System.out.println("---------------"+list_user_id);

                DataBaseUser.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        requestsViewHolder.setFrom(dataSnapshot.child("name").getValue().toString());
                        requestsViewHolder.setThumbImage(dataSnapshot.child("thumb_image").getValue().toString(),getContext());

                    }
                    @Override public void onCancelled(DatabaseError databaseError) {}
                });

                requestsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent=new Intent(getContext(),ProfileActivity.class);
                        profileIntent.putExtra("user_id",list_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        mRequestList.setAdapter(requestsRecyclerViewAdapter);
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public RequestsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setDate(String date){
            TextView requestDate=(TextView)mView.findViewById(R.id.user_single_status);
            requestDate.setText(date);
        }

        public void setFrom (String from){
            TextView userName=(TextView)mView.findViewById(R.id.user_single_name);  //Pongo la fecha en un Textview
                         //que inicialmente estaba creado para el status y lo reciclo.
            userName.setText(from);
        }

        public void setThumbImage(String thumb_image, Context ctx){
            CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);

        }

    }

}
