package patrick.fuscoe.remindmelater;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.TimePickerDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;

public class UserPreferencesActivity extends AppCompatActivity
        implements TimePickerDialogFragment.OnTimeSetListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.UserPreferencesActivity";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static FirebaseAuth auth;
    public static String userId;
    public static DocumentReference userDocRef;

    private UserProfile userProfile;

    private EditText viewDisplayName;
    private TextView viewTimeDisplay;
    private Button btnSetTime;
    private Button btnClearEmptyReminderCategories;
    private Button btnSave;
    private Button btnCancel;

    private List<ReminderItem> reminderItemList;


    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO: Add button click behavior
            switch (v.getId())
            {
                case R.id.button_user_preferences_time:
                    openTimePicker();
                    return;

                case R.id.button_user_preferences_clear_empty_reminder_categories:
                    clearEmptyReminderCategories();
                    return;

                case R.id.button_user_preferences_save:
                    saveUserPrefs();
                    //Toast.makeText(getApplicationContext(), userProfile.getDisplayName() + ": User Settings Updated", Toast.LENGTH_LONG).show();
                    onBackPressed();
                    return;

                case R.id.button_user_preferences_cancel:
                    Toast.makeText(getApplicationContext(), "Edit User Settings Cancelled", Toast.LENGTH_LONG).show();
                    onBackPressed();
                    return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preferences);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);

        Intent intent = getIntent();
        Gson gson = new Gson();

        Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
        String userProfileString = intent.getStringExtra(MainActivity.USER_PROFILE);
        Log.d(TAG, "userProfileString in intent: " + userProfileString);
        userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);
        Log.d(TAG, "User Profile obtained from intent");

        viewDisplayName = findViewById(R.id.view_user_preferences_display_name);
        viewDisplayName.setText(userProfile.getDisplayName());
        viewTimeDisplay = findViewById(R.id.view_user_preferences_time_display);
        setTimeDisplay();

        btnSetTime = findViewById(R.id.button_user_preferences_time);
        btnSetTime.setOnClickListener(btnClickListener);
        btnClearEmptyReminderCategories = findViewById(R.id.button_user_preferences_clear_empty_reminder_categories);
        btnClearEmptyReminderCategories.setOnClickListener(btnClickListener);
        btnSave = findViewById(R.id.button_user_preferences_save);
        btnSave.setOnClickListener(btnClickListener);
        btnCancel = findViewById(R.id.button_user_preferences_cancel);
        btnCancel.setOnClickListener(btnClickListener);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(userProfile.getDisplayName() + ":  Settings");

        reminderItemList = new ArrayList<>();
    }

    private void setTimeDisplay()
    {
        String timeToDisplay;
        String minutesString;

        if (userProfile.getReminderMinute() == 0)
        {
            minutesString = "00";
        }
        else
        {
            minutesString = "0";
        }

        if (userProfile.getReminderHour() < 12)
        {
            timeToDisplay = userProfile.getReminderHour() + " : " +
                    minutesString + " AM";
        }
        else
        {
            timeToDisplay = userProfile.getReminderHour() + " : " +
                    minutesString + " PM";
        }

        viewTimeDisplay.setText(timeToDisplay);
    }

    private void openTimePicker()
    {
        DialogFragment dialogFragment = new TimePickerDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "timePicker");
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

        Log.d(TAG, "onTimeSet: hourOfDay = " + hourOfDay + ". minute = " + minute);

        userProfile.setReminderHour(hourOfDay);
        userProfile.setReminderMinute(minute);

        setTimeDisplay();
    }

    public void clearEmptyReminderCategories()
    {
        loadReminders();
        Map<String, String> reminderCategories = new HashMap<>();
        reminderCategories.putAll(userProfile.getReminderCategories());

        for (Map.Entry<String, String> reminderCategory : reminderCategories.entrySet())
        {
            int numReminders = 0;

            for (ReminderItem reminderItem : reminderItemList)
            {
                if (reminderCategory.getKey().equals(reminderItem.getCategory()))
                {
                    numReminders++;
                }
            }

            if (numReminders == 0)
            {
                userProfile.removeReminderCategory(reminderCategory.getKey());
                Log.d(TAG, reminderCategory.getKey() + " - Reminder Category Removed");
            }
        }
    }

    public void saveUserPrefs()
    {
        String displayName = viewDisplayName.getText().toString();

        Map<String, Object> userProfileDoc = new HashMap<>();

        userProfileDoc.put("displayName", displayName);
        userProfileDoc.put("reminderCategories", userProfile.getReminderCategories());
        userProfileDoc.put("subscriptions", Arrays.asList(userProfile.getSubscriptions()));
        userProfileDoc.put("reminderHour", userProfile.getReminderHour());
        userProfileDoc.put("reminderMinute", userProfile.getReminderMinute());

        userDocRef.set(userProfileDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Toast.makeText(getApplicationContext(), "User Settings for " +
                                userProfile.getDisplayName() + " Saved", Toast.LENGTH_LONG).show();
                        Log.d(TAG, userProfile.getDisplayName() + " User Profile Updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    public void loadReminders()
    {
        MainActivity.remindersDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            Map<String, Object> docMap = documentSnapshot.getData();

                            for (Map.Entry<String, Object> entry : docMap.entrySet())
                            {
                                if (!entry.getKey().contentEquals("userId"))
                                {
                                    String title = entry.getKey();
                                    HashMap<String, Object> reminderItemMap = (HashMap<String, Object>) entry.getValue();

                                    int recurrenceNum = Math.toIntExact((long) reminderItemMap.get("recurrenceNum"));
                                    String recurrenceInterval = (String) reminderItemMap.get("recurrenceInterval");
                                    String nextOccurrence = (String) reminderItemMap.get("nextOccurrence");
                                    String category = (String) reminderItemMap.get("category");
                                    String categoryIconName = (String) reminderItemMap.get("categoryIconName");
                                    String description = (String) reminderItemMap.get("description");
                                    boolean isSnoozed = (boolean) reminderItemMap.get("isSnoozed");

                                    ReminderItem reminderItem = new ReminderItem(title, recurrenceNum,
                                            recurrenceInterval, nextOccurrence, category,
                                            categoryIconName, description, isSnoozed);

                                    reminderItemList.add(reminderItem);
                                }

                            }

                        }
                    }
                });
    }

}
