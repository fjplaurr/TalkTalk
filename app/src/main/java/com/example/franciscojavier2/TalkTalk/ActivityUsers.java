package com.example.franciscojavier2.TalkTalk;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import com.example.franciscojavier2.TalkTalk.Model.Users;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.squareup.picasso.Picasso;
import de.hdodenhof.circleimageview.CircleImageView;

public class ActivityUsers extends AppCompatActivity {
    private Toolbar mToolbar;
    private RecyclerView mUsersList;
    private DatabaseReference mUsersDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        //Toolbar
        mToolbar=(Toolbar)findViewById(R.id.users_appBar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.allUsers);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Inicialización DatabaseRefence
        mUsersDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        //RecyclerView
        mUsersList=(RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);   //Esto se usa en los RecyclerView opcionalmente cuando sabemos que
                    //los cambios en el contenido no van a afectar al tamaño del layout para mejorar rendimiento.
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

    }

    protected void onStart(){
        super.onStart();
        //Instancio un objeto de la clase FirebaseRecyclerAdapter que puedo utilizar con la librería FirebaseUI.
        //Tiene 2 parámetros: el primero es la clase Users y el segundo es la clase UsersViewHolder que hereda
        //de RecyclerView.ViewHolder ya que es necesario como segundo argumento.
        //Además, tengo que poner los 4 argumentos al new FirebaseRecyclerAdapter como hago abajo.
        FirebaseRecyclerAdapter<Users, UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users,
                UsersViewHolder>(
                Users.class,
                R.layout.users_layout,
                UsersViewHolder.class,
                mUsersDatabase) {
            @Override
            protected void populateViewHolder(UsersViewHolder usersViewHolder, Users users, int position) {
                usersViewHolder.setDisplayName(users.getName());
                usersViewHolder.setDisplayStatus(users.getStatus());
                usersViewHolder.setUserImage(users.getThumb_image(),getApplicationContext());  //Tengo que pasar
                        //el Context ya que lo necesito para Picasso. UsersViewHolder no tiene un contexto en sí
                        //dado que es una subclase.

                final String user_id=getRef(position).getKey();  //Esta línea devuelve el ID del user que pulse.

                usersViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent profileIntent=new Intent(ActivityUsers.this,ActivityProfile.class);
                        profileIntent.putExtra("user_id",user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);

        //Código para el valor online de la BD.
        mUsersDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUsersDatabase.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("online").setValue(ServerValue.TIMESTAMP);
    }



    public static class UsersViewHolder extends RecyclerView.ViewHolder{  //Creo una clase que herede de RecyclerView.ViewHolder.
                            //Tiene que ser static ya que es una subclase.
        View mView;   //view es una vista que he creado ya que quiero q tome el valor de la imagen q pulse para trabajar con ella.
        public UsersViewHolder(View itemView) {
            super(itemView);
            mView=itemView;
        }

        public void setDisplayName(String name) {
            TextView userNameView=(TextView)mView.findViewById(R.id.user_single_name);
            userNameView.setText(name);
        }

        public void setDisplayStatus(String status) {
            TextView userStatusView=(TextView)mView.findViewById(R.id.user_single_status);
            userStatusView.setText(status);
        }

        public void setUserImage(String thumb_image, Context ctx) {
           CircleImageView userImageView=(CircleImageView)mView.findViewById(R.id.user_single_image);
           Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.default_avatar).into(userImageView);
        }
    }

}
