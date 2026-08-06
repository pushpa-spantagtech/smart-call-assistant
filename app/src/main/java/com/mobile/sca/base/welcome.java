package com.mobile.sca.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.mobile.sca.R;
import com.mobile.sca.home;

public class welcome extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(
                getWindow(),
                true
        );

        // Keep the existing Welcome UI
        setContentView(R.layout.welcome);

        SharedPreferences pref =
                getSharedPreferences("app", MODE_PRIVATE);

        boolean permissionCompleted =
                pref.getBoolean("permission", false);

        boolean loginCompleted =
                pref.getBoolean("logged_in", false);

        /*
         * Login and permission are already completed.
         * Open Home.
         */
        if (loginCompleted && permissionCompleted) {
            startActivity(
                    new Intent(
                            welcome.this,
                            home.class
                    )
            );

            finish();
            return;
        }

        /*
         * Login is completed, but permission setup is pending.
         */
        if (loginCompleted) {
            startActivity(
                    new Intent(
                            welcome.this,
                            permissioncheck.class
                    )
            );

            finish();
        }
    }

    /**
     * Called by the existing Get Started button in welcome.xml.
     */
    public void homenaviagte(View view) {
        SharedPreferences pref =
                getSharedPreferences("app", MODE_PRIVATE);

        pref.edit()
                .putBoolean("welcome", true)
                .apply();

        startActivity(
                new Intent(
                        welcome.this,
                        login.class
                )
        );

        finish();
    }
}