 package com.example.franciscojavier2.TalkTalk;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

 public class ActivitySignUp extends AppCompatActivity {

     //Layout
    private TextInputLayout mDisplayName, mEmail, mPassword;
    private Button mCreateBtn;
    private Toolbar mToolbar;

    //Instancia de ProgressDialogg
    private ProgressDialog mRegProgress;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

     @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sing_up);

        //Declaración del Toolbar
        mToolbar=(Toolbar)findViewById(R.id.register_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true); //Instrucción para añadir un botón de atrás en el Toolbar que nos
            //llevará al ActivityStart ya que así se lo he indicado yo en el Manifest al poner que es su parent.

        //Inicio instancia del ProgressDialog
        mRegProgress=new ProgressDialog(this  );

        //Instancia para Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Instancias para registrarse
        mDisplayName=(TextInputLayout)findViewById(R.id.reg_display_name);
        mEmail=(TextInputLayout)findViewById(R.id.reg_email);
        mPassword=(TextInputLayout)findViewById(R.id.reg_password);
        mCreateBtn=(Button)findViewById(R.id.reg_create_btn);

        mCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String display_name=mDisplayName.getEditText().getText().toString();
                String email=mEmail.getEditText().getText().toString();
                String password=mPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(display_name)&&!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                    mRegProgress.setTitle("Registering User");
                    mRegProgress.setMessage("Please wait while we create your account!");
                    mRegProgress.setCanceledOnTouchOutside(false);   //Establece que el dialogo no pueda cerrarse si se toca fuera de la ventana.
                    mRegProgress.show();
                    register_user(display_name,email,password);
                }

            }
        });
    }

     private void register_user(final String display_name, final String email, final String password) {
         mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                     @Override
                     public void onComplete(@NonNull Task<AuthResult> task) {
                         if (task.isSuccessful()) {
                             FirebaseUser current_user=FirebaseAuth.getInstance().getCurrentUser();
                             String uid=current_user.getUid();

                             mDatabase=FirebaseDatabase.getInstance().getReference().child("Users").child(uid);

                                //Quiero almacenar un token en la BD cada vez que haglo log in. Para ello, utilizo la clase
                                //FirebaseInstanceId.
                             String device_token= FirebaseInstanceId.getInstance().getToken();

                                //Para almacenar datos en la Base de datos, se puede utilizar un HashMap en lugar de crear una clase.
                             HashMap<String,String>userMap=new HashMap<>();
                             userMap.put("device_token",device_token);
                             userMap.put("name",display_name);
                             userMap.put("status","Hi everyone! Using TalkTalk! :)");
                             userMap.put("image","default");
                             userMap.put("thumb_image","default");

                                //La siguiente línea hacer dos cosas: primero hace setValue y después añade un OnCompleteListener para
                                //ver si la tarea de añadir un setValue se realiza correctamente. OJO: Se hace el setValue. Parece evidente
                                //pero lo remarco.

                             mDatabase.setValue(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                 @Override
                                 public void onComplete(@NonNull Task<Void> task) {

                                     if(task.isSuccessful()){

                                         mRegProgress.dismiss();
                                         Intent mainIntent=new Intent(ActivitySignUp.this,MainActivity.class);
                                         mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); //No lo tengo muy claro pero es algo así. New_task crea un nuevo Task para la Activity que se va a abrir
                                         //(MainActivity) y Clear_Task elimina todas las otras tasks que estaban abiertas hasta ahora dejando la nueva task creada con New_task como la única. Con ésto consigo que activity_main pase
                                         //a ser la actividad arriba de la pila de la nueva task y que, por tanto, al pulsar atrás, regrese al escritorio inicial del Smartphone en lugar de al ActivitySignUp.
                                         startActivity(mainIntent);
                                         finish();
                                     }
                                 }
                             });


                         } else {
                             mRegProgress.hide();
                             // If sign in fails, display a message to the user.
                             Toast.makeText(ActivitySignUp.this, "Cannot Sign in. Please check the form and try again.",Toast.LENGTH_LONG).show();
                         }
                     }
         });
     }


 }
