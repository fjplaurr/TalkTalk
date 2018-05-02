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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class ActivityLogIn extends AppCompatActivity {

    //Layout
    private Toolbar mToolbar;
    private TextInputLayout mLoginEmail, mLoginPassword;
    private Button mLogin_btn;

    //ProgressDialog
    private ProgressDialog mLoginProgress;

    //Firebase
    private FirebaseAuth mAuth;
    private DatabaseReference mUserDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mAuth=FirebaseAuth.getInstance();

        mToolbar=(Toolbar)findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Log in");

        //ProgressDialog
        mLoginProgress=new ProgressDialog(this);

        mUserDatabase= FirebaseDatabase.getInstance().getReference().child("Users");

        mLoginEmail=(TextInputLayout)findViewById(R.id.login_email);
        mLoginPassword=(TextInputLayout)findViewById(R.id.login_password);
        mLogin_btn=(Button)findViewById(R.id.login_btn);

        mLogin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email=mLoginEmail.getEditText().getText().toString();
                String password=mLoginPassword.getEditText().getText().toString();

                if(!TextUtils.isEmpty(email)&&!TextUtils.isEmpty(password)){
                    //ProgressDialog
                    mLoginProgress.setTitle("Logging In");
                    mLoginProgress.setMessage("Please wait while we check your credentials.");
                    mLoginProgress.setCanceledOnTouchOutside(false);

                    loginUser(email,password);
                }
            }
        });
    }

    private void loginUser(final String email, final String password) {
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //ProgressDialog
                    mLoginProgress.dismiss();

                    String current_user_id=mAuth.getCurrentUser().getUid();
                    String deviceToken= FirebaseInstanceId.getInstance().getToken(); //Quiero almacenar un token
                                //en la BD cada vez que haglo log in.
                    mUserDatabase.child(current_user_id).child("device_token").setValue(deviceToken).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Intent mainIntent=new Intent(ActivityLogIn.this,MainActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);  //No lo tengo muy claro pero es algo así. New_task crea un nuevo Task para la Activity que se va a abrir
                                //(ActivityLogIn) y Clear_Task elimina todas las otras tasks que estaban abiertas hasta ahora dejando la nueva task creada con New_task como la única. Con ésto consigo que activity_main pase
                                //a ser la actividad arriba de la pila de la nueva task y que, por tanto, al pulsar atrás, regrese al escritorio inicial del Smartphone en lugar de al activity_log_in.
                                startActivity(mainIntent);
                                finish();
                            }
                        }
                    });

                }else{
                    String error = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseAuthInvalidUserException e) {
                        error = "Invalid Email!";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        error = "Invalid Password!";
                    } catch (Exception e) {
                        error = "Default error!";
                        e.printStackTrace();
                    }
                    mLoginProgress.hide();
                    Toast.makeText(ActivityLogIn.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
