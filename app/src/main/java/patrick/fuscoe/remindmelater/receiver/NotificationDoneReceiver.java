package patrick.fuscoe.remindmelater.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import patrick.fuscoe.remindmelater.models.ReminderItem;

public class NotificationDoneReceiver extends BroadcastReceiver {

    public static final String TAG = "patrick.fuscoe.remindmelater.NotificationDoneReceiver";
    public static final int DEFAULT_NOTIFICATION_ID = 100;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    private DocumentReference remindersDocRef;

    private Context context;
    private int notificationId;

    private ReminderItem reminderItem;
    private String remindersDocId;


    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        this.notificationId = intent.getIntExtra(ReminderAlarmReceiver.EXTRA_NOTIFICATION_ID, DEFAULT_NOTIFICATION_ID);

        Gson gson = new Gson();
        Type dataTypeReminderItem = new TypeToken<ReminderItem>(){}.getType();
        String reminderItemString = intent.getStringExtra(ReminderAlarmReceiver.REMINDER_ITEM);
        reminderItem = gson.fromJson(reminderItemString, dataTypeReminderItem);

        remindersDocId = intent.getStringExtra(ReminderAlarmReceiver.REMINDERS_DOC_ID);
        remindersDocRef = remindersCollectionRef.document(remindersDocId);

    }
}
