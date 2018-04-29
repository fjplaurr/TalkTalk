package com.example.franciscojavier2.TalkTalk;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ActivityStart extends AppCompatActivity {

    private Button mRegBtn,mLoginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        mRegBtn=(Button)findViewById(R.id.start_reg_btn);
        mRegBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent reg_intent=new Intent(ActivityStart.this,ActivitySignUp.class);
                startActivity(reg_intent);
            }
        });

        mLoginBtn=(Button)findViewById(R.id.start_login_btn);
        mLoginBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent log_intent=new Intent(ActivityStart.this,ActivityLogIn.class);
                startActivity(log_intent);
            }
        });
    }
}
