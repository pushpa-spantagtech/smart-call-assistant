package com.mobile.sca;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Calendar;

public class setupActivity extends AppCompatActivity implements OnboardingActionListener {

    SharedPreferences pref;

    private ViewPager2 viewPager;
    private TabLayout dotsIndicator;
    private ImageView btnBack;


    @Override
    public void onPhoneContinueClicked(String phoneNumber, int position) {
        Log.d("PHONE", phoneNumber);
        if(position == 3) {
            SharedPreferences pref = getSharedPreferences("app", MODE_PRIVATE);
            pref.edit().putBoolean("home", true).commit();
            startActivity(new Intent(this, home.class));
            finish();
        } else {
            viewPager.setCurrentItem(position, true);
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);

        pref = getSharedPreferences("app", MODE_PRIVATE);

        viewPager = findViewById(R.id.viewPager);
        dotsIndicator = findViewById(R.id.dotsIndicator);
        btnBack = findViewById(R.id.btnBack);

        if (pref.getBoolean("home", false) == true){
            startActivity(new Intent(this, home.class));
            finish();
        } else {
            OnboardingAdapter adapter = new OnboardingAdapter(this, getApplicationContext());
            viewPager.setUserInputEnabled(false);
            viewPager.setAdapter(adapter);

            new TabLayoutMediator(dotsIndicator, viewPager,
                    (tab, position) -> {
                        // No title needed (dots only)
                    }).attach();
        }

        btnBack.setOnClickListener(v -> {
            if (viewPager.getCurrentItem() > 0) {
                viewPager.setCurrentItem(viewPager.getCurrentItem() - 1);
            } else {
                finish();
            }
        });
    }

    /* -------------------------------------------------------
     * EXACT ALARM PERMISSION (Android 12+ Requirement)
     * ------------------------------------------------------- */
    private void checkDndPermission() {

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm.isNotificationPolicyAccessGranted()) {
            // Permission already granted → do NOT open settings
//            Intent serviceIntent = new Intent(this, com.mobile.sca.DndService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                startForegroundService(serviceIntent);
//            }
            return;
        }

        // Permission NOT granted → open settings
        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
        startActivity(intent);
    }

    /* -------------------------------------------------------
     * DND PERMISSION
     * ------------------------------------------------------- */
    public void requestDndPermission() {
        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (!nm.isNotificationPolicyAccessGranted()) {
            Log.d("DND_TEST", "Requesting DND permission");
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS);
            startActivity(intent);
        } else {
            setupAlarmsOnce();
        }
    }

    /* -------------------------------------------------------
     * SCHEDULE ALARMS ONLY ONCE
     * ------------------------------------------------------- */
    private void setupAlarmsOnce() {
        setupAlarms();
    }

    /* -------------------------------------------------------
     * YOUR FULL DAILY SCHEDULE
     * ------------------------------------------------------- */
    private void setupAlarms() {

        // DND ON TIMES
        scheduleDnd(this, 8, 56);
        scheduleDnd(this, 8, 57);
        scheduleDnd(this, 8, 58);
        scheduleDnd(this, 9, 1);

        // DND OFF TIME
        scheduleDndOff(this, 9, 3);
    }

    /* -------------------------------------------------------
     * SCHEDULE DND ON
     * ------------------------------------------------------- */
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleDnd(Context context, int hour, int minute) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        if (c.getTimeInMillis() < System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        Log.d("DND_TEST", "Scheduling ON at " + hour + ":" + minute);

//        Intent intent = new Intent(context, DndReceiver.class);
//
//        PendingIntent pi = PendingIntent.getBroadcast(
//                context,
//                hour * 60 + minute,
//                intent,
//                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
//        );
//
//        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
    }

    /* -------------------------------------------------------
     * SCHEDULE DND OFF
     * ------------------------------------------------------- */
    @SuppressLint("ScheduleExactAlarm")
    public void scheduleDndOff(Context context, int hour, int minute) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        if (c.getTimeInMillis() < System.currentTimeMillis()) {
            c.add(Calendar.DAY_OF_YEAR, 1);
        }

        Log.d("DND_TEST", "Scheduling OFF at " + hour + ":" + minute);

//        Intent intent = new Intent(context, DndOffReceiver.class);
//
//        PendingIntent pi = PendingIntent.getBroadcast(
//                context,
//                10000 + hour * 60 + minute,
//                intent,
//                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
//        );
//
//        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
//        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, c.getTimeInMillis(), pi);
    }
}
