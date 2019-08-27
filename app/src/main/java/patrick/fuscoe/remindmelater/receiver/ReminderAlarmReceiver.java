package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(MainActivity.REMINDER_TITLE);
        int iconId = intent.getIntExtra(MainActivity.REMINDER_ICON_ID, R.drawable.category_note);

        // TODO: Setup send notification for received alarm
        // need small icon, title
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, MainActivity.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(iconId)
                .setContentTitle(title)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }
}
