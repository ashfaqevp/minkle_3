package com.example.minkle3;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import kotlinx.coroutines.Delay;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(user == null)
                {
                    startActivity(new Intent(SplashActivity.this,FragmentReplaceActivity.class));

                }
                else
                {
                    startActivity(new Intent(SplashActivity.this,MainActivity.class));

                }
                finish();

            }
        },2500);
    }
}