package com.android.voicenote.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.util.DebugUtils;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.android.voicenote.edit.NoteActivity;
import com.android.voicenote.MainActivity;

/**
 * Created by lvjinhua on 6/9/2016.
 */
public class MyService extends Service {
    static Timer timer;
    public final static int NOTIFICATION_ID = 0xfff;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        timer = new Timer();
        final Bundle bundle = intent.getExtras();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification notification = new Notification.Builder(MainActivity.getContext())
                        .setTicker("时间到啦")
                        .setContentTitle("主人，您又有新提醒啦!")
                        .setContentText(bundle.getString("caption"))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setShowWhen(true)
                        .setWhen(System.currentTimeMillis())
                        .build();
                nm.notify(NOTIFICATION_ID, notification);
            }
        }, new Date(bundle.getLong("when")));

        return super.onStartCommand(intent, flags, startId);
    }

    //清除通知
    public static void cleanAllNotification() {
        NotificationManager mn = (NotificationManager) MainActivity.getContext().getSystemService(NOTIFICATION_SERVICE);
        mn.cancel(NOTIFICATION_ID);
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
