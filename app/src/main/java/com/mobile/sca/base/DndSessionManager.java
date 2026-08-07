package com.mobile.sca.base;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Keeps DND safe when meetings overlap and restores the user's previous
 * interruption filter after the final meeting ends.
 */
public final class DndSessionManager {

    private static final String PREFS = "sca_dnd_sessions";
    private static final String KEY_ACTIVE_TOKENS = "active_tokens";
    private static final String KEY_PREVIOUS_FILTER = "previous_filter";
    private static final String KEY_PREVIOUS_FILTER_SAVED = "previous_filter_saved";
    private static final String END_PREFIX = "end_";
    private static final String TITLE_PREFIX = "title_";

    private DndSessionManager() {
    }

    public static synchronized boolean startSession(
            Context context,
            String token,
            long endAtMillis,
            String title
    ) {
        NotificationManager nm = getNotificationManager(context);
        if (nm == null || !nm.isNotificationPolicyAccessGranted()) {
            return false;
        }

        SharedPreferences prefs = prefs(context);
        Set<String> active = mutableTokens(prefs);

        if (active.isEmpty()) {
            prefs.edit()
                    .putInt(KEY_PREVIOUS_FILTER, nm.getCurrentInterruptionFilter())
                    .putBoolean(KEY_PREVIOUS_FILTER_SAVED, true)
                    .apply();
        }

        active.add(token);
        prefs.edit()
                .putStringSet(KEY_ACTIVE_TOKENS, active)
                .putLong(END_PREFIX + token, endAtMillis)
                .putString(TITLE_PREFIX + token, title == null ? "Meeting" : title)
                .apply();

        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
        return true;
    }

    public static synchronized boolean endSession(Context context, String token) {
        NotificationManager nm = getNotificationManager(context);
        SharedPreferences prefs = prefs(context);
        Set<String> active = mutableTokens(prefs);

        active.remove(token);
        SharedPreferences.Editor editor = prefs.edit()
                .putStringSet(KEY_ACTIVE_TOKENS, active)
                .remove(END_PREFIX + token)
                .remove(TITLE_PREFIX + token);

        if (!active.isEmpty()) {
            editor.apply();
            // Another meeting is still active, so DND must remain enabled.
            if (nm != null && nm.isNotificationPolicyAccessGranted()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            }
            return true;
        }

        if (nm == null || !nm.isNotificationPolicyAccessGranted()) {
            editor.apply();
            return false;
        }

        int previousFilter = prefs.getBoolean(KEY_PREVIOUS_FILTER_SAVED, false)
                ? prefs.getInt(
                KEY_PREVIOUS_FILTER,
                NotificationManager.INTERRUPTION_FILTER_ALL
        )
                : NotificationManager.INTERRUPTION_FILTER_ALL;

        editor.remove(KEY_PREVIOUS_FILTER)
                .remove(KEY_PREVIOUS_FILTER_SAVED)
                .apply();

        nm.setInterruptionFilter(previousFilter);
        return true;
    }

    /**
     * Removes sessions whose end alarms were missed, for example after a reboot.
     * Active sessions are kept and DND is re-applied.
     */
    public static synchronized Set<SessionInfo> reconcileAfterBoot(Context context) {
        SharedPreferences prefs = prefs(context);
        Set<String> active = mutableTokens(prefs);
        Set<SessionInfo> stillActive = new HashSet<>();
        long now = System.currentTimeMillis();

        for (String token : new HashSet<>(active)) {
            long endAt = prefs.getLong(END_PREFIX + token, 0L);
            String title = prefs.getString(TITLE_PREFIX + token, "Meeting");

            if (endAt > now) {
                stillActive.add(new SessionInfo(token, endAt, title));
            } else {
                active.remove(token);
                prefs.edit()
                        .remove(END_PREFIX + token)
                        .remove(TITLE_PREFIX + token)
                        .apply();
            }
        }

        prefs.edit().putStringSet(KEY_ACTIVE_TOKENS, active).apply();

        NotificationManager nm = getNotificationManager(context);
        if (nm != null && nm.isNotificationPolicyAccessGranted()) {
            if (!active.isEmpty()) {
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
            } else if (prefs.getBoolean(KEY_PREVIOUS_FILTER_SAVED, false)) {
                int previous = prefs.getInt(
                        KEY_PREVIOUS_FILTER,
                        NotificationManager.INTERRUPTION_FILTER_ALL
                );
                nm.setInterruptionFilter(previous);
                prefs.edit()
                        .remove(KEY_PREVIOUS_FILTER)
                        .remove(KEY_PREVIOUS_FILTER_SAVED)
                        .apply();
            }
        }

        return Collections.unmodifiableSet(stillActive);
    }

    public static boolean hasPolicyAccess(Context context) {
        NotificationManager nm = getNotificationManager(context);
        return nm != null && nm.isNotificationPolicyAccessGranted();
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static Set<String> mutableTokens(SharedPreferences prefs) {
        return new HashSet<>(prefs.getStringSet(KEY_ACTIVE_TOKENS, new HashSet<>()));
    }

    public static final class SessionInfo {
        public final String token;
        public final long endAtMillis;
        public final String title;

        public SessionInfo(String token, long endAtMillis, String title) {
            this.token = token;
            this.endAtMillis = endAtMillis;
            this.title = title;
        }
    }
}
