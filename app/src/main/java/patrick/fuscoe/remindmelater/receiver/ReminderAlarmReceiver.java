package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import patrick.fuscoe.remindmelater.MainActivity;

public class ReminderAlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra(MainActivity.REMINDER_TITLE);

        // TODO: Setup send notification for received alarm
        // need small icon, title
    }
}
