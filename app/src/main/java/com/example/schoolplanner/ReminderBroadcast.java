package com.example.schoolplanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        String notificationInfo = intent.getExtras().getString(AddAssessmentDialog.ASSESSMENT_INFO,"Hubba Wubs");

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "assessmentAlert")
                .setSmallIcon(R.drawable.alert_png1)
                .setContentTitle("Assessment Alert")
                .setContentText("Alert "+notificationInfo+" has started.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(200, builder.build());
    }
}
