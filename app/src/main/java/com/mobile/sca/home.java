package com.mobile.sca;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import com.mobile.sca.base.AlwaysOnService;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class home extends AppCompatActivity {

    BottomNavigationView bottomNavigation;

    public static home instance;

    private boolean permissionAskedOnce = false;
    private static final int REQ_POST_NOTIF = 1001;

    /* ===================== PERMISSION & SERVICE ===================== */

    private void ensurePermissionsAndStartService() {

        AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (am != null && !am.canScheduleExactAlarms()) {
                Toast.makeText(this, "Enable the Permission", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                i.setData(Uri.parse("package:" + getPackageName()));
                startActivity(i);
                return;
            }
        }

        NotificationManager nm =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null && !nm.isNotificationPolicyAccessGranted()) {
            Toast.makeText(this, "Find My Scheduler -> Allow", Toast.LENGTH_SHORT).show();
            startActivity(
                    new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
            return;
        }

        if (Build.VERSION.SDK_INT >= 33) {

            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED) {
                startAlwaysOnService();
                return;
            }

            if (!permissionAskedOnce) {
                permissionAskedOnce = true;
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        REQ_POST_NOTIF);
                return;
            }

            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.POST_NOTIFICATIONS)) {
                showPermissionDialog();
                return;
            }

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    REQ_POST_NOTIF);
        } else {
            startAlwaysOnService();
        }
    }

    private void showPermissionDialog() {
        new AlertDialog.Builder(this)
                .setMessage("Go to app settings → enable notification permission")
                .setPositiveButton("Yes", (d, i) -> {
                    Intent intent = new Intent(
                            Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(
                            Uri.fromParts("package", getPackageName(), null));
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void startAlwaysOnService() {
        Intent svc = new Intent(this, AlwaysOnService.class);
        if (Build.VERSION.SDK_INT >= 26) {
            ContextCompat.startForegroundService(this, svc);
        } else {
            startService(svc);
        }
    }

    /* ===================== ACTIVITY ===================== */

    @Override
    protected void onResume() {
        super.onResume();
        ensurePermissionsAndStartService();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        setContentView(R.layout.activity_home);

        instance = this;
        bottomNavigation = findViewById(R.id.bottomNavigation);

        bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment;

            if (item.getItemId() == R.id.menu_home) {
                fragment = new HomeFrag();
            } else if (item.getItemId() == R.id.menu_schedule) {
                fragment = new ScheduleFrag();
            } else if (item.getItemId() == R.id.menu_support) {
                fragment = new SupportUsFrag();
            } else {
                return false;
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.container, fragment)
                    .commit();

            return true;
        });

        // Default fragment
        bottomNavigation.setSelectedItemId(R.id.menu_home);
    }

    /* ===================== OPTIONAL: CHANGE TAB PROGRAMMATICALLY ===================== */

    public void moveTab(int tab) {
        switch (tab) {
            case 1:
                bottomNavigation.setSelectedItemId(R.id.menu_home);
                break;
            case 2:
                bottomNavigation.setSelectedItemId(R.id.menu_schedule);
                break;
            case 3:
                bottomNavigation.setSelectedItemId(R.id.menu_support);
                break;
        }
    }
}
