package com.example.franciscojavier2.TalkTalk;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.franciscojavier2.TalkTalk.Model.FriendRequest;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class FragmentFriendshipReq extends Fragment {
    private View vista;
    private DatabaseReference mUsersDatabase;
    private RecyclerView recyclerView;
    private FirebaseAuth mAuth;
    private DatabaseReference mFriendreqDatabase,mRootRef;
    private ValueEventListener valueEventListener;
    private String mCurrent_user_id;
    private List<String> phoneContactsWithApp = new ArrayList<String>();
    private String[] tlfContacts;
    private Intent openActivityAddContacts;
        //Constant to give permissions when requested. It could be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private FloatingActionButton floatingActionButton;


    public FragmentFriendshipReq() {// Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        vista = inflater.inflate(R.layout.fragment_friendship_req, container, false);

        //RecyclerView
        recyclerView =(RecyclerView) vista.findViewById(R.id.requests_list);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        //Firebase
        mRootRef=FirebaseDatabase.getInstance().getReference();
        mAuth=FirebaseAuth.getInstance();
        mCurrent_user_id=mAuth.getCurrentUser().getUid();
        mFriendreqDatabase = FirebaseDatabase.getInstance().getReference().child("FriendRequest").child(mCurrent_user_id);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");
        mUsersDatabase.keepSynced(true);

        floatingActionButton = vista.findViewById(R.id.floatingActionButtonFragmentFriendship);
        return vista;
    }

    public void onStart() {
        super.onStart();

//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Here's a", Snackbar.LENGTH_LONG).setAction("Action", null).show();
//            }
//        });



        FirebaseRecyclerAdapter<FriendRequest,RequestsViewHolder> requestsRecyclerViewAdapter =
                new FirebaseRecyclerAdapter<FriendRequest, FragmentFriendshipReq.RequestsViewHolder>(
                        FriendRequest.class,
                        R.layout.users_layout,  //Reutilizado este layout.
                        FragmentFriendshipReq.RequestsViewHolder.class,
                        mFriendreqDatabase) {

            @Override
            protected void populateViewHolder(final FragmentFriendshipReq.RequestsViewHolder requestsViewHolder,
                                              final FriendRequest requests, final int position) {

                final String list_user_id=getRef(position).getKey().toString();
                final DatabaseReference DataBaseUser=mRootRef.child("Users").child(list_user_id);
                mFriendreqDatabase.child(list_user_id).child("request_type").addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.getValue()!=null){
                            String request_type=dataSnapshot.getValue().toString();
                            requestsViewHolder.setRequestType("Friend request "+request_type);
                            DataBaseUser.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    requestsViewHolder.setFrom(dataSnapshot.child("name").getValue().toString());
                                    requestsViewHolder.setThumbImage(dataSnapshot.child("thumb_image").getValue().toString(),getContext());
                                }
                                @Override public void onCancelled(DatabaseError databaseError) {}
                            });
                        }
                    }
                    @Override public void onCancelled(DatabaseError databaseError) {}
                });



                requestsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent=new Intent(getContext(),ActivityProfile.class);
                        profileIntent.putExtra("user_id",list_user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };
        recyclerView.setAdapter(requestsRecyclerViewAdapter);
    }



    private void getContactList() {
        ContentResolver cr = getContext().getContentResolver();
        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        tlfContacts = new String[phones != null ? phones.getCount() : 0];
        if(phones !=null){
            while (phones.moveToNext()) {
                String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                tlfContacts[phones.getPosition()] = phoneNumber;
            }
        }
        if(!phones.isClosed())
            phones.close();

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    String phoneNumberUser = postSnapshot.child("phone").getValue().toString();
                    for (int i = 0; i < tlfContacts.length ; i++) {
                        if (tlfContacts[i].equals(phoneNumberUser)) {
                            phoneContactsWithApp.add(phoneNumberUser);
                        }
                    }
                }
                openActivityAddContacts = new Intent(getContext(), ActivityAddContacts.class);
                openActivityAddContacts.putStringArrayListExtra("phoneContacts", (ArrayList<String>) phoneContactsWithApp);
                startActivityForResult(openActivityAddContacts,1);
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        };
        mUsersDatabase.addListenerForSingleValueEvent(valueEventListener);
    }


    private void RequestPermissions() {
        //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && ContextCompat.checkSelfPermission(getContext(),
        //          Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
        //   }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        mUsersDatabase.removeEventListener(valueEventListener);
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                ArrayList <String> result=data.getStringArrayListExtra("result");
                for (final String idUserToSendRequest : result){
                    mFriendreqDatabase.child(mCurrent_user_id).child(idUserToSendRequest).child("request_type").setValue("sent");
                    mFriendreqDatabase.child(idUserToSendRequest).child(mCurrent_user_id).child("request_type").setValue("received");
                }
            }
        }
    }

    public void onPause(){
        super.onPause();
        if(valueEventListener!=null)
            mUsersDatabase.removeEventListener(valueEventListener);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                getContactList();
            } else {
                Toast.makeText(getContext(), "Until you grant the permission, we cannot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static class RequestsViewHolder extends RecyclerView.ViewHolder{
        View mView;
        public RequestsViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setRequestType(String request_type){
            TextView requestDate=(TextView)mView.findViewById(R.id.user_single_status);
            requestDate.setText(request_type);
        }

        public void setFrom (String from){
            TextView userName=(TextView)mView.findViewById(R.id.user_single_name);  //Pongo la fecha en un Textview
                         //que inicialmente estaba creado para el status y lo reciclo.
            userName.setText(from);
        }

        public void setThumbImage(String thumb_image, Context ctx){
            CircleImageView userPicture=(CircleImageView)mView.findViewById(R.id.user_single_image);
            Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userPicture);
        }
    }

}
