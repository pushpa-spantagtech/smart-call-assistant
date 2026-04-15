package com.mobile.sca.base;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.mobile.sca.R;
import com.mobile.sca.home;
import com.google.android.material.checkbox.MaterialCheckBox;

public class welcome extends AppCompatActivity {

    private void requestDndPermission() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (!notificationManager.isNotificationPolicyAccessGranted()) {
            Intent intent = new Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        }
    }

    MaterialCheckBox privacyCheckBox;
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.welcome);
        SharedPreferences pref = getSharedPreferences("app", MODE_PRIVATE);

        if(pref.getBoolean("permission", false)) {
            startActivity(new Intent(getApplicationContext(), home.class));
            finish();
        }
        else if(pref.getBoolean("welcome", false)) {
            startActivity(new Intent(getApplicationContext(), permissioncheck.class));
            finish();
        }
    }

    public void homenaviagte(View view) {
        SharedPreferences pref = getSharedPreferences("app", MODE_PRIVATE);
        pref.edit().putBoolean("welcome", true).commit();
        startActivity(new Intent(getApplicationContext(), permissioncheck.class));
        finish();

    }
}