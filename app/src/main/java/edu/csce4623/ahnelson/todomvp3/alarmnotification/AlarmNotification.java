package edu.csce4623.ahnelson.todomvp3.alarmnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;

import edu.csce4623.ahnelson.todomvp3.R;
import edu.csce4623.ahnelson.todomvp3.addcreateeditactivity.AddCreateEditActivity;
import edu.csce4623.ahnelson.todomvp3.data.ToDoItem;
import edu.csce4623.ahnelson.todomvp3.todolistactivity.ToDoListActivity;

import static android.provider.Settings.System.getString;
import static androidx.core.content.ContextCompat.getSystemService;

public class AlarmNotification extends BroadcastReceiver {
    String ALARM_CHANNEL_ID = "alarm_channel";
    String ALARM_CHANNEL_NAME = "alarm";
    String ALARM_CHANNEL_DESCRIPTION = "Alarm for ToDoItems";

    @Override
    public void onReceive(Context context, Intent intent) {

        ToDoItem toDoItem = null;

        // Extras from Intent
        Bundle bundle = intent.getBundleExtra("bundle");
        if (bundle != null) {
            toDoItem = (ToDoItem) bundle.getSerializable("ToDoItem");
        }

        createNotificationChannel(context, ALARM_CHANNEL_ID, ALARM_CHANNEL_NAME, ALARM_CHANNEL_DESCRIPTION);
        NotificationCompat.Builder alarm_builder = new NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("Task Is Due!")
                .setContentText(toDoItem.getTitle())
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        Intent resultIntent = new Intent(context, AddCreateEditActivity.class);
        resultIntent.putExtra("ToDoItem", toDoItem);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Sets Main activity as what you can go back to, from the AddCreateEditActivity
        stackBuilder.addParentStack(ToDoListActivity.class);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        alarm_builder.setContentIntent(resultPendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, alarm_builder.build());
    }

    private void createNotificationChannel(Context context, String channel_id, String channel_name, String channel_description) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channel_id, channel_name, importance);
            channel.setDescription(channel_description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
