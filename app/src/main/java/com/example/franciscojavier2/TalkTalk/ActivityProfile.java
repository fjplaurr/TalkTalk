package com.example.franciscojavier2.TalkTalk;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ActivityProfile extends AppCompatActivity {

    //Layout
    private ImageView mProfileImage;
    private TextView mProfileName,mProfileStatus;
    private Button mProfileSendReqBtn;
    private Button mDeclineBtn;

    //Firebase
    private DatabaseReference mUsersDatabase;
    private DatabaseReference mFriendRequestDatabase;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;  //Referencia para notificaciones
    private DatabaseReference mRootRef;
    private DatabaseReference mUserRef;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mAuth;

    //ProgressDialog
    private ProgressDialog mProgressDialog;

    private String mCurrent_state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //Obtengo el user ID desde ActivityUsers.
        final String user_id=getIntent().getStringExtra("user_id");

        //Database reference Firebase
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child(user_id);
        mFriendRequestDatabase=FirebaseDatabase.getInstance().getReference().child("FriendRequest");
        mFriendDatabase=FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase=FirebaseDatabase.getInstance().getReference().child("notifications");
        mRootRef=FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        mUserRef= FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());

        //UI Firebase
        mCurrentUser=FirebaseAuth.getInstance().getCurrentUser();

        //Layout
        mProfileImage=(ImageView)findViewById(R.id.profile_image);
        mProfileName=(TextView)findViewById(R.id.profile_displayName);
        mProfileStatus=(TextView)findViewById(R.id.profile_status);
        mProfileSendReqBtn=(Button)findViewById(R.id.profile_send_req_btn);
        mDeclineBtn =(Button)findViewById(R.id.decline_req_btn);

        mCurrent_state="not_friends";

        //ProgressDialog
        mProgressDialog=new ProgressDialog(ActivityProfile.this);
        mProgressDialog.setTitle(getString(R.string.loadingUserData));
        mProgressDialog.setMessage(getString(R.string.waitWhileLoadingUserData));
        mProgressDialog.setCanceledOnTouchOutside(false);
        mProgressDialog.show();


        mDeclineBtn.setVisibility(View.INVISIBLE); //El segundo botón debe ser invisible por defecto. Solo será visible
                //cuando recibamos una solicitud para poder declinarla.
        //if para no poder enviarme invitaciones a mí mismo.
        if(user_id.equals(mCurrentUser.getUid())){
            mProfileSendReqBtn.setVisibility(View.INVISIBLE);
        }

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //Datos obtenidos desde el DataSnapshot
              String display_name=dataSnapshot.child("name").getValue().toString();
              String status=dataSnapshot.child("status").getValue().toString();
              String image=dataSnapshot.child("image").getValue().toString();

              //Layout
              mProfileName.setText(display_name);
              mProfileStatus.setText(status);
              Picasso.with(ActivityProfile.this).load(image).placeholder(R.drawable.default_avatar).into(mProfileImage);

              //Friends List / Request Feature
                mFriendRequestDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(user_id)){  //Si hemos recibido o enviado un request a user_id:
                            String req_type=dataSnapshot.child(user_id).child("request_type").getValue().toString();
                            if(req_type.equals("received")){   //En el caso de que lo hayamos recibido:
                                mCurrent_state="req_received";
                                mProfileSendReqBtn.setText("Accept Friend Request");
                                mDeclineBtn.setVisibility(View.VISIBLE); //Hacemos visible el otro  botón para poder denegar la solicitud que nos han enviado.
                            }else if(req_type.equals("sent")){
                                mCurrent_state="req_sent";
                                mProfileSendReqBtn.setText("Cancel Friend Request");
                            }
                            mProgressDialog.dismiss();
                        }else{
                            mFriendDatabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(user_id)){  //Si esto pasa es porque el user_id es mi friend.
                                        mCurrent_state="friends";
                                        mProfileSendReqBtn.setText("Unfriend this person");
                                    }
                                    mProgressDialog.dismiss();
                                }
                                @Override public void onCancelled(DatabaseError databaseError) {
                                    mProgressDialog.dismiss();
                                }
                            });
                        }
                        //ProgressDialog
                    }
                    @Override public void onCancelled(DatabaseError databaseError) {}
                });
            }
            @Override public void onCancelled(DatabaseError databaseError) {}
        });

        mProfileSendReqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProfileSendReqBtn.setEnabled(false);

                //--------------NOT FRIENDS STATE-------------------
                //PRIMERA FORMA


                if(mCurrent_state.equals("not_friends")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                    //Para el friend hacemos lo mismo pero al revés. Abrimos un request_type en la BD y añado received.
                                mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).child("request_type").setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override   //OnSuccessListener es una interfaz similar a OnCompleteListener.
                                                //La ventaja q tiene es q se ejecuta si el task ha ocurrido correctamente
                                                //así q no tngo q checkear el task con un if.
                                    public void onSuccess(Void aVoid) {

                                        HashMap<String,String>notificationData=new HashMap<>();
                                        notificationData.put("from",mCurrentUser.getUid());;
                                        notificationData.put("type","request");
                                        final String currentDate= DateFormat.getDateTimeInstance().format(new Date());
                                        notificationData.put("date",currentDate);

                                        mNotificationDatabase.child(user_id).push().setValue(notificationData).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    //push() lo que hace es generar una clave única aleatoria para la notificación.
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                mProfileSendReqBtn.setEnabled(true);
                                                mCurrent_state="req_sent";
                                                mProfileSendReqBtn.setText("Cancel Friend Request"); //El mismo botón se va a utilizar para cancelar la solicitud de amistad

                                            }
                                        });
                                    }
                                });
                            }else{
                                mProfileSendReqBtn.setEnabled(true);
                                Toast.makeText(ActivityProfile.this,"Faild sending request", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                    //--------------BOTÓN 1 - OPCIÓN CANCELAR ENVÍO DE SOLICITUD------------------
                if(mCurrent_state.equals("req_sent")){
                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileSendReqBtn.setText("Send friend request"); //El mismo botón se va a utilizar para cancelar la solicitud de amistad
                                }
                            });
                        }
                    });
                }

                //---------------BOTÓN 1 - ACEPTO UNA SOLICITUD--------------------
                if(mCurrent_state.equals("req_received")){
                    final String currentDate= DateFormat.getDateTimeInstance().format(new Date());  //String de día y hora que usaré como valor en la BD.
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("date").setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    //Las siguientes líneas son iguales que cuando hago CANCELAR ENVÍO DE SOLICITUD. Es decir, elimino la solicitud de la BD de ambos users.
                                    mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    mProfileSendReqBtn.setEnabled(true);
                                                    mCurrent_state="friends";   //Ésto sí es diferente. Ahora cambio el state a friends.
                                                    mProfileSendReqBtn.setText("Unfriend this person"); //Ésto tb es diferente. Ahora el botón da la opción de eliminar de amigo.
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                }

                //--------------BOTÓN 1 - OPCIÓN UNFRIEND THIS PERSON----------------
                if(mCurrent_state.equals("friends")){
                    mFriendDatabase.child(mCurrentUser.getUid()).child(user_id).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(user_id).child(mCurrentUser.getUid()).child("date").removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mProfileSendReqBtn.setEnabled(true);
                                    mCurrent_state="not_friends";
                                    mProfileSendReqBtn.setText("Send friend request");
                                }
                            });
                        }
                    });
                }
            }
        });



        //-------- BOTÓN 2 - DECLINAR UNA SOLICITUD DE AMISTAD---------------
        mDeclineBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFriendRequestDatabase.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mFriendRequestDatabase.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                mDeclineBtn.setVisibility(View.INVISIBLE);
                                mCurrent_state="not_friends";
                                mProfileSendReqBtn.setEnabled(true);
                                mProfileSendReqBtn.setText("Send friend request"); //Ésto tb es diferente. Ahora el botón da la opción de eliminar de amigo.
                                Toast.makeText(ActivityProfile.this,"Request successfully declined", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    //Código para actualizar la clave online en la BD.
    @Override
    public void onStart() {
        super.onStart();
        mUserRef.child("online").setValue(true);
    }


}
