package com.example.schoolplanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.util.Random;

public class AssessmentReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent){

        //test for whether detail or dialog is setting a reminder
        String action = intent.getAction();

        if(action.equals("dialogReminder")) {  //this is dialog calling
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
        else if(action.equals("detailReminder")){  //must be detail calling
            String notificationInfo = intent.getExtras().getString(AssessmentDetail.ASSESSMENT_NOTIFICATION_INFO, "Chubba Wubs");
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
