package com.example.schoolplanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){
        try {  //is this an assessment reminder?
            String notificationInfo = intent.getExtras().getString(AddAssessmentDialog.ASSESSMENT_NOTIFICATION_INFO, "Hubba Wubs");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "dueAlert")
                    .setSmallIcon(R.drawable.alert_png1)
                    .setContentTitle("Alert!")
                    .setContentText(notificationInfo)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            //random number for ID so that more than one notification will display at same time
            Random r = new Random();
            int randomID = r.nextInt(10000 - 1);
            notificationManager.notify(randomID, builder.build());
        }
        catch (Exception e){  //if we're here, it must be a course reminder
            String notificationInfo = intent.getExtras().getString(AddCourseDialog.COURSE_NOTIFICATION_INFO, "Hubba Wubs");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "dueAlert")
                    .setSmallIcon(R.drawable.alert_png1)
                    .setContentTitle("Alert!")
                    .setContentText(notificationInfo)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            //random number for ID so that more than one notification will display at same time
            Random r = new Random();
            int randomID = r.nextInt(10000 - 1);
            notificationManager.notify(randomID, builder.build());
        }
    }
}
