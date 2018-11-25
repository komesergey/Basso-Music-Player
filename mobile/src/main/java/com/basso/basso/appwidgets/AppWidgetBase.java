package com.basso.basso.appwidgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.basso.basso.MusicPlaybackService;

public abstract class AppWidgetBase extends AppWidgetProvider {

    protected PendingIntent buildPendingIntent(Context context, final String action,
            final ComponentName serviceName) {
        Intent intent = new Intent(action);
        intent.setComponent(serviceName);
        intent.putExtra(MusicPlaybackService.NOW_IN_FOREGROUND, false);
        return PendingIntent.getService(context, 0, intent, 0);
    }

}
