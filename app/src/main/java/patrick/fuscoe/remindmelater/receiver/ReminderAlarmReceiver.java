package patrick.fuscoe.remindmelater.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.TaskStackBuilder;

import java.util.List;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;
import patrick.fuscoe.remindmelater.ui.main.RemindersViewModel;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    private List<ReminderItem> reminderItemList;

    @Override
    public void onReceive(Context context, Intent intent) {

        int notificationId = (int) System.currentTimeMillis();

        String title = intent.getStringExtra(MainActivity.REMINDER_TITLE);
        int iconId = intent.getIntExtra(MainActivity.REMINDER_ICON_ID, R.drawable.category_note);

        Intent remindersFragmentIntent = new Intent(context, RemindersFragment.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(remindersFragmentIntent);
        PendingIntent remindersFragmentPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // TODO: Setup send notification for received alarm
        // TODO: Setup action buttons
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(remindersFragmentPendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());


    }

    public void loadReminders()
    {
        //RemindersViewModel remindersViewModel = new RemindersViewModel();
        // TODO: use AsyncTask to load reminders...
    }
}
