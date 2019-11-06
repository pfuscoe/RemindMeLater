package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Notifies MainActivity when phone is rebooted so that reminder alarms can be set.
*/
public class BootReceiver extends BroadcastReceiver {

    private BootReceiverCallback bootListener;

    public BootReceiver(BootReceiverCallback bootListener)
    {
        this.bootListener = bootListener;
    }

    public interface BootReceiverCallback {
        void bootReceived();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            bootListener.bootReceived();
        }
    }
}
