package com.mobile.sca;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

/** Only fixed event metadata belongs here; never send credentials or schedule titles. */
public final class AppAnalytics {
    public static final String LOGIN = "login";
    public static final String SIGNUP = "signup";
    public static final String HOME = "home";
    public static final String SCHEDULER = "scheduler";
    public static final String SUPPORT = "support";

    public static final String ADD_SCHEDULER = "add_scheduler_click";
    public static final String SCHEDULE_NOW = "schedule_now_click";
    public static final String DELETE_SCHEDULE = "delete_schedule";
    public static final String TURN_OFF_SCHEDULE = "turn_off_schedule";
    public static final String LOGOUT = "logout_click";

    private AppAnalytics() {}

    public static void screen(Context context, String name, String screenClass) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.SCREEN_NAME, name);
        params.putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass);
        FirebaseAnalytics.getInstance(context.getApplicationContext())
                .logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params);
    }

    public static void click(Context context, String event, String sourceScreen) {
        Bundle params = new Bundle();
        params.putString("source_screen", sourceScreen);
        FirebaseAnalytics.getInstance(context.getApplicationContext()).logEvent(event, params);
    }

    /** Called only in the API success branch, never on the initial button tap. */
    public static void authSuccess(Context context, boolean signup) {
        Bundle params = new Bundle();
        params.putString(FirebaseAnalytics.Param.METHOD, "password");
        FirebaseAnalytics.getInstance(context.getApplicationContext()).logEvent(
                signup ? FirebaseAnalytics.Event.SIGN_UP : FirebaseAnalytics.Event.LOGIN, params);
    }
}
