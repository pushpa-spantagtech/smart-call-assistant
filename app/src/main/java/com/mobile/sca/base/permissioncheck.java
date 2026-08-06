package com.mobile.sca.base;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.mobile.sca.R;
import com.mobile.sca.home;

public class permissioncheck extends AppCompatActivity {

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

    Button btnStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.permissioncheck);
        SharedPreferences pref = getSharedPreferences("app", MODE_PRIVATE);

        btnStart = findViewById(R.id.btnStart);

        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
                NotificationManager nm =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (am != null && !am.canScheduleExactAlarms()) {
                        Toast.makeText(getApplicationContext(), "Enable the Permission", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        i.setData(Uri.parse("package:" + getPackageName()));
                        startActivity(i);
                    } else if (nm != null && !nm.isNotificationPolicyAccessGranted()) {
                        Toast.makeText(getApplicationContext(), "Find My Scheduler -> Allow", Toast.LENGTH_SHORT).show();
                        startActivity(
                                new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
                    } else {
                        SharedPreferences pref = getSharedPreferences("app", MODE_PRIVATE);
                        pref.edit().putBoolean("permission", true).commit();
                        startActivity(new Intent(getApplicationContext(), home.class));
                        finish();
                    }

                }
            }
        });


    }
}