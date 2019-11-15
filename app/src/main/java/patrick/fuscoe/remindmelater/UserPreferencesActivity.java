package patrick.fuscoe.remindmelater;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.TimePickerDialogFragment;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;
import patrick.fuscoe.remindmelater.util.ReminderAlarmUtils;

/**
 * Manages UI for setting user preferences. Handles cloud sync when user hits save.
*/
public class UserPreferencesActivity extends AppCompatActivity
        implements TimePickerDialogFragment.OnTimeSetListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.UserPreferencesActivity";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static FirebaseAuth auth;
    public static String userId;
    public static DocumentReference userDocRef;

    private UserProfile userProfile;

    private ProgressBar viewProgressBar;
    private TextView viewDisplayNameLabel;
    private EditText viewDisplayName;
    private TextView viewSetTimeLabel;
    private TextView viewTimeDisplay;
    private Button btnSetTime;
    private View viewDividerBottom;
    private Button btnSave;
    private Button btnCancel;

    private List<ReminderItem> reminderItemList;

    private boolean hasReminderTimeOfDayChanged;
    private int hourOfDay;
    private int minute;


    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.button_user_preferences_time:
                    openTimePicker();
                    return;

                case R.id.button_user_preferences_save:
                    saveUserPrefs();
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

        hasReminderTimeOfDayChanged = false;
        hourOfDay = userProfile.getReminderHour();
        minute = userProfile.getReminderMinute();

        viewProgressBar = findViewById(R.id.view_user_preferences_progress_bar);
        viewDisplayNameLabel = findViewById(R.id.view_user_preferences_display_name_label);
        viewSetTimeLabel = findViewById(R.id.view_user_preferences_time_label);
        viewDividerBottom = findViewById(R.id.view_user_preferences_divider_bottom);

        viewDisplayName = findViewById(R.id.view_user_preferences_display_name);
        viewDisplayName.setText(userProfile.getDisplayName());
        viewTimeDisplay = findViewById(R.id.view_user_preferences_time_display);
        setTimeDisplay();

        btnSetTime = findViewById(R.id.button_user_preferences_time);
        btnSetTime.setOnClickListener(btnClickListener);
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

        if (minute == 0)
        {
            minutesString = "00";
        }
        else if (minute < 10)
        {
            minutesString = "0" + (minute % 10);
        }
        else
        {
            minutesString = String.valueOf(minute);
        }

        if (hourOfDay == 0)
        {
            timeToDisplay = "12 : " + minutesString + " AM";
        }
        else if (hourOfDay < 12)
        {
            timeToDisplay = hourOfDay + " : " + minutesString + " AM";
        }
        else if (hourOfDay == 12)
        {
            timeToDisplay = hourOfDay + " : " + minutesString + " PM";
        }
        else
        {
            timeToDisplay = (hourOfDay - 12) + " : " + minutesString + " PM";
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

        if (hourOfDay == userProfile.getReminderHour() && minute == userProfile.getReminderMinute())
        {
            hasReminderTimeOfDayChanged = false;
        }
        else
        {
            hasReminderTimeOfDayChanged = true;
            userProfile.setReminderHour(hourOfDay);
            userProfile.setReminderMinute(minute);
            this.hourOfDay = hourOfDay;
            this.minute = minute;
            setTimeDisplay();
        }
    }

    public void saveUserPrefs()
    {
        showProgressBar();

        final String displayName = viewDisplayName.getText().toString();

        final boolean hasDisplayNameChanged;

        if (!displayName.equals(userProfile.getDisplayName()))
        {
            hasDisplayNameChanged = true;
            userProfile.setDisplayName(displayName);
        }
        else
        {
            hasDisplayNameChanged = false;
        }

        Map<String, Object> userProfileDoc = FirebaseDocUtils.createUserProfileDoc(userProfile);

        userDocRef.set(userProfileDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Log.d(TAG, userProfile.getDisplayName() + " User Profile Updated");
                        Toast.makeText(getApplicationContext(), "User Settings Saved", Toast.LENGTH_LONG).show();

                        if (hasDisplayNameChanged)
                        {
                            updateFirebaseUserDisplayName(displayName);
                        }
                        else
                        {
                            goBackToMain();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(getApplicationContext(), "Failed to save user " +
                                "settings to cloud", Toast.LENGTH_LONG).show();
                        hideProgressBar();
                    }
                });
    }

    private void updateFirebaseUserDisplayName(String displayName)
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName).build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Display Name set in Firebase user profile.");

                            goBackToMain();
                        }
                        else
                        {
                            Log.d(TAG, "Failed to set display name in Firebase user profile " +
                                    "(userProfile doc was updated though)");
                            Toast.makeText(getApplicationContext(), "Failed to save user " +
                                    "settings to cloud", Toast.LENGTH_LONG).show();
                            hideProgressBar();
                        }
                    }
                });
    }

    private void goBackToMain()
    {
        if (hasReminderTimeOfDayChanged)
        {
            ReminderAlarmUtils.setReminderTimeOfDay(getApplicationContext(),
                    userProfile.getReminderHour(), userProfile.getReminderMinute());

            ReminderAlarmUtils.updateReminderAlarmsOnTimeSet(getApplicationContext());
        }

        Gson gson = new Gson();
        String userProfileString = gson.toJson(userProfile);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(MainActivity.USER_PREFERENCES_UPDATED, true);
        intent.putExtra(MainActivity.USER_PROFILE, userProfileString);
        startActivity(intent);
        finish();
    }

    private void showProgressBar()
    {
        viewProgressBar.setVisibility(View.VISIBLE);

        viewDisplayNameLabel.setVisibility(View.INVISIBLE);
        viewDisplayName.setVisibility(View.INVISIBLE);
        viewSetTimeLabel.setVisibility(View.INVISIBLE);
        viewTimeDisplay.setVisibility(View.INVISIBLE);
        btnSetTime.setVisibility(View.INVISIBLE);
        viewDividerBottom.setVisibility(View.INVISIBLE);
        btnSave.setVisibility(View.INVISIBLE);
        btnCancel.setVisibility(View.INVISIBLE);
    }

    private void hideProgressBar()
    {
        viewProgressBar.setVisibility(View.INVISIBLE);

        viewDisplayNameLabel.setVisibility(View.VISIBLE);
        viewDisplayName.setVisibility(View.VISIBLE);
        viewSetTimeLabel.setVisibility(View.VISIBLE);
        viewTimeDisplay.setVisibility(View.VISIBLE);
        btnSetTime.setVisibility(View.VISIBLE);
        viewDividerBottom.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
    }

}
