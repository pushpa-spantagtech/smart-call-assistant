package com.mobile.sca.base;

import android.app.NotificationManager;
import android.content.Context;

public class DndUtils {

    public static void setDnd(Context context, boolean enable) {

        NotificationManager nm =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (!nm.isNotificationPolicyAccessGranted()) return;

        if (enable) {
            nm.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_NONE
            );
        } else {
            nm.setInterruptionFilter(
                    NotificationManager.INTERRUPTION_FILTER_ALL
            );
        }
    }
}
