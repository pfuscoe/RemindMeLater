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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.DeleteAccountDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.TimePickerDialogFragment;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;
import patrick.fuscoe.remindmelater.util.ReminderAlarmUtils;

/**
 * Manages UI for setting user preferences. Handles cloud sync when user hits save.
*/
public class UserPreferencesActivity extends AppCompatActivity
        implements TimePickerDialogFragment.OnTimeSetListener,
        DeleteAccountDialogFragment.DeleteAccountDialogListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.UserPreferencesActivity";

    public static final String USER_ACCOUNT_DELETED = "patrick.fuscoe.remindmelater.USER_ACCOUNT_DELETED";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference toDoGroupsRef = db.collection("todogroups");
    private final CollectionReference remindersRef = db.collection("reminders");

    public static FirebaseAuth auth;
    public static String userId;
    public static DocumentReference userDocRef;
    private static String remindersDocId;
    private List<String> toDoGroupIds;
    private List<ToDoGroup> toDoGroupList;

    private UserProfile userProfile;

    private ProgressBar viewProgressBar;
    private TextView viewDisplayNameLabel;
    private EditText viewDisplayName;
    private TextView viewSetTimeLabel;
    private TextView viewTimeDisplay;
    private Button btnSetTime;
    private View viewDividerBottom;
    private TextView viewDeleteAccount;
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

                case R.id.view_user_preferences_delete_account:
                    openConfirmDeleteAccountDialog();
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
        viewDeleteAccount = findViewById(R.id.view_user_preferences_delete_account);

        btnSetTime = findViewById(R.id.button_user_preferences_time);
        btnSetTime.setOnClickListener(btnClickListener);
        btnSave = findViewById(R.id.button_user_preferences_save);
        btnSave.setOnClickListener(btnClickListener);
        btnCancel = findViewById(R.id.button_user_preferences_cancel);
        btnCancel.setOnClickListener(btnClickListener);
        viewDeleteAccount.setOnClickListener(btnClickListener);

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
        Bundle bundle = new Bundle();
        bundle.putInt("hourOfDay", hourOfDay);
        bundle.putInt("minute", minute);
        dialogFragment.setArguments(bundle);
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

    private void openConfirmDeleteAccountDialog()
    {
        DialogFragment dialogFragment = new DeleteAccountDialogFragment(userProfile);
        dialogFragment.show(getSupportFragmentManager(), "deleteAccount");
    }

    private void deleteUserAccount()
    {
        //showProgressBar();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "User account deleted.");
                            Toast.makeText(getApplicationContext(), "User account deleted " +
                                    "for Remind Me Later", Toast.LENGTH_LONG).show();

                            // Get all document id's then clear data from FireStore
                            //getRemindersDocumentId();
                            goBackToMain();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Could not delete user " +
                                    "account: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
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

    private void getRemindersDocumentId()
    {
        showProgressBar();

        final Query remindersQuery = remindersRef.whereEqualTo("userId", MainActivity.userId);
        remindersQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                remindersDocId = document.getId();
                                getToDoGroups();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            hideProgressBar();
                        }
                    }
                });
    }

    private void getToDoGroups()
    {
        toDoGroupList = new ArrayList<>();

        final Query toDoGroupsQuery = toDoGroupsRef.whereArrayContains("subscribers", userId);
        toDoGroupsQuery.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                ToDoGroup toDoGroup = FirebaseDocUtils.createToDoGroupObj(document);
                                toDoGroupList.add(toDoGroup);
                                clearUserDataFromFireStore();
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            hideProgressBar();
                        }
                    }
                });
    }

    private void clearUserDataFromFireStore()
    {
        WriteBatch batch = db.batch();

        // Delete the user's reminders document
        batch.delete(remindersRef.document(remindersDocId));

        // Remove user from any shared to do lists and delete all private lists
        for (ToDoGroup toDoGroup : toDoGroupList)
        {
            if (toDoGroup.getSubscribers().length > 1)
            {
                toDoGroup.removeSubscriber(userProfile.getId());
                Map<String, Object> toDoGroupDoc = FirebaseDocUtils.createToDoGroupDoc(toDoGroup);
                batch.set(toDoGroupsRef.document(toDoGroup.getId()), toDoGroupDoc);
            }
            else
            {
                batch.delete(toDoGroupsRef.document(toDoGroup.getId()));
            }
        }

        // Delete the user profile document
        batch.delete(userDocRef);

        batch.commit()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful())
                        {
                            Log.d(TAG, "Successfully cleared user data from FireStore");
                            //goBackToSignIn();
                            deleteUserAccount();
                        }
                        else
                        {
                            Log.d(TAG, "Error deleting data from FireStore: " +
                                    task.getException().getMessage());
                            hideProgressBar();
                            Toast.makeText(getApplicationContext(), "Error deleting data" +
                                            " from FireStore: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void goBackToSignIn()
    {
        Intent intent = new Intent(getApplicationContext(), FirebaseSignInActivity.class);
        intent.putExtra(USER_ACCOUNT_DELETED, true);
        startActivity(intent);
        finishAndRemoveTask();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment) {
        if (dialogFragment instanceof DeleteAccountDialogFragment)
        {
            //deleteUserAccount();
            getRemindersDocumentId();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialogFragment) {
        if (dialogFragment instanceof DeleteAccountDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Delete account cancelled.",
                    Toast.LENGTH_SHORT).show();
        }
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
        viewDeleteAccount.setVisibility(View.INVISIBLE);
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
        viewDeleteAccount.setVisibility(View.VISIBLE);
        btnSave.setVisibility(View.VISIBLE);
        btnCancel.setVisibility(View.VISIBLE);
    }

}
