package com.example.franciscojavier2.lapitchat;

import android.app.Application;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Created by FranciscoJavier2 on 11/12/2017.
 */

//--------------------------CLASE PARA TRABAJAR OFFLINE---------------------------

    //Esta clase es instanciada desde el AndroidManifest antes que cualquier otro componente de la App.

public class LapitChat extends Application {

    private DatabaseReference mUserDatabase;
    private FirebaseAuth mAuth;

    private FirebaseUser mUser;

    public void onCreate(){
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
                    //setPersistenceEnabled permite trabajar offline en memoria caché.

        /*Para poder usar Picasso offline, uso las siguientes líneas*/
        Picasso.Builder picassoBuilder = new Picasso.Builder(this);
            //OkHttpDownloader es una clase importada mediante librería okhttp.
        picassoBuilder.downloader(new OkHttpDownloader(this,Integer.MAX_VALUE));
        Picasso built = picassoBuilder.build();
        built.setIndicatorsEnabled(true);
        built.setLoggingEnabled(true);
        Picasso.setSingletonInstance(built);


        mAuth=FirebaseAuth.getInstance();
        mUser=mAuth.getCurrentUser();

        if(mUser!=null){  //if para evitar que siga el código cuando no hay un user, es decir, cuando
                    //se ejecuta la App por primera vez.
            mUserDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid());
            mUserDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null) {
                        mUserDatabase.child("online").onDisconnect().setValue(false);
                        mUserDatabase.child("online").setValue(true);
                    }
                }

                @Override public void onCancelled(DatabaseError databaseError) {}
            });

        }
    }
}
