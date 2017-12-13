package com.example.franciscojavier2.lapitchat;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.google.firebase.auth.*;
import com.google.firebase.database.DatabaseReference;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;

    private ViewPager mViewPager;
    private SectionsPagerAdapter mSectionsPagerAdapter; //Necesito una clase adaptadora para el mViewPager que herede de FragmentPagerAdapter.

    private TabLayout mTabLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mToolbar=(Toolbar)findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Lapit Chat");

        mAuth = FirebaseAuth.getInstance();



        //ViewPager y adaptador
        mViewPager=(ViewPager)findViewById(R.id.main_tabPager);
        mSectionsPagerAdapter=new SectionsPagerAdapter(getSupportFragmentManager());   //La clase adaptadora FragmentPagerAdapter sólo tiene un constructor que admite
                    //obligatoriamente un FragmentManager como argumento. Lo obtengo mediante getSupportFragmentManager() de la clase Fragment Activity que devuelve un
                    //Fragment Manager para interactura con fragments asociados con esta actividad.
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mTabLayout=(TabLayout)findViewById(R.id.main_tabs);
        mTabLayout.setupWithViewPager(mViewPager);  //Necesario para establecer un link entre el TabLayout y el ViewPager para que "se actualicen conjuntamente". Lo he probado
                //y aparece la barrita horizontal que indica en qué Fragment te encuentras y además el propio TabLayout tiene funcionalidad para cambiar de un Fragment a otro.
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.

        FirebaseUser currentUser=mAuth.getCurrentUser();
        if(currentUser==null){
            sendToStart();//Para que el user no pueda volver al Main desde el StartActivity si pulsa el botón atrás.
        }
    }


    private void sendToStart() {
        Intent startIntent=new Intent(MainActivity.this, StartActivity.class);
        startActivity(startIntent);
        finish();
    }

    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_btn){   //Al pulsar en main_logout_btn, haremos sign out y eso nos llevará a la StartActivity.
            FirebaseAuth.getInstance().signOut();
            sendToStart();
        }

        if(item.getItemId()==R.id.main_settings_btn){
            Intent settingsIntent=new Intent(this,SettingsActivity.class);
            startActivity(settingsIntent);
        }

        if(item.getItemId()==R.id.main_all_btn){
            Intent usersIntent=new Intent(MainActivity.this, UsersActivity.class);
            startActivity(usersIntent);
        }

        return true;
    }
}
