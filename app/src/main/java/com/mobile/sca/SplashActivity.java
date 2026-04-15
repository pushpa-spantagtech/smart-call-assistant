package com.mobile.sca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.mobile.sca.base.welcome;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences pref;
    private static final int REQ_NOTIFICATION = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //Do something after 100ms
                startActivity(new Intent(getApplicationContext(), welcome.class));
                finish();
            }
        }, 500);
    }
}
