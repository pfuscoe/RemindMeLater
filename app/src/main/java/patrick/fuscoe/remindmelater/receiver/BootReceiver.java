package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;

import patrick.fuscoe.remindmelater.util.ReminderAlarmUtils;

/**
 * Notifies MainActivity when phone is rebooted so that reminder alarms can be set.
*/
public class BootReceiver extends BroadcastReceiver {

    public static String TAG = "patrick.fuscoe.remindmelater.BootReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            FirebaseAuth auth = FirebaseAuth.getInstance();

            Log.d(TAG, "onReceive called");

            if (auth.getUid() != null)
            {
                Log.d(TAG, "auth.getUid is not null: " + auth.getUid());

                ReminderAlarmUtils.updateReminderAlarmsOnTimeSet(context);
            }
        }
    }
}
