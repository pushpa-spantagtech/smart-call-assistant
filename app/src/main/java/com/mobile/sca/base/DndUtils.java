package com.mobile.sca.base;

import android.app.NotificationManager;
import android.content.Context;

/**
 * Low-level DND utility. Meeting code should use DndSessionManager so
 * overlapping meetings and previous DND state are handled correctly.
 */
public final class DndUtils {

    private DndUtils() {
    }

    public static boolean hasAccess(Context context) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        return nm != null && nm.isNotificationPolicyAccessGranted();
    }

    public static boolean setDnd(Context context, boolean enable) {
        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm == null || !nm.isNotificationPolicyAccessGranted()) {
            return false;
        }

        nm.setInterruptionFilter(
                enable
                        ? NotificationManager.INTERRUPTION_FILTER_NONE
                        : NotificationManager.INTERRUPTION_FILTER_ALL
        );
        return true;
    }
}
