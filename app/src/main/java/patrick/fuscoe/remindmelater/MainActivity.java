package patrick.fuscoe.remindmelater;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderAlarmItem;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.receiver.BootReceiver;
import patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver;
import patrick.fuscoe.remindmelater.ui.main.SectionsPagerAdapter;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;
import patrick.fuscoe.remindmelater.util.ReminderAlarmUtils;

/**
 * The main entry point after the user signs in, except when the user goes directly to
 * ReminderDetailsActivity from notification tap.
 *
 * Has two fragments that are setup as tabs: ToDoFragment, RemindersFragment.
 *
 * Accessible throughout the application except for in the receiver classes handling alarms and
 * notification actions.
 *
 * Launches in singleTask mode.
*/
public class MainActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.MainActivity";

    public static final String NOTIFICATION_CHANNEL_ID = "patrick.fuscoe.remindmelater.NOTIFICATION_CHANNEL_ID";

    public static final int DEFAULT_REMINDER_TIME_HOUR = 8;
    public static final int DEFAULT_REMINDER_TIME_MINUTE = 0;
    public static final int DEFAULT_HIBERNATE_LENGTH = 14;
    public static final String DEFAULT_REMINDER_CATEGORY_ICON_NAME = "category_alarm";
    public static final String DEFAULT_TO_DO_GROUP_CATEGORY_ICON_NAME = "category_format_list_checkbox";
    public static final int DEFAULT_NOTIFICATION_ID = 100;

    public static final String USER_PROFILE = "patrick.fuscoe.remindmelater.USER_PROFILE";
    public static final String TO_DO_GROUP_LIST = "patrick.fuscoe.remindmelater.TO_DO_GROUP_LIST";
    public static final String REMINDER_ITEM_LIST = "patrick.fuscoe.remindmelater.REMINDER_ITEM_LIST";
    public static final String REMINDERS_DOC_ID = "patrick.fuscoe.remindmelater.REMINDERS_DOC_ID";
    public static final String REMINDER_TITLE = "patrick.fuscoe.remindmelater.REMINDER_TITLE";
    public static final String BACK_PRESSED_FROM_REMINDER_DETAILS = "patrick.fuscoe.remindmelater.BACK_PRESSED_FROM_REMINDER_DETAILS";
    public static final String USER_PREFERENCES_UPDATED = "patrick.fuscoe.remindmelater.USER_PREFERENCES_UPDATED";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    public static FirebaseAuth auth;
    public static String userId;
    public static String deviceToken;
    public static DocumentReference userDocRef;
    public static DocumentReference remindersDocRef;
    public static String remindersDocId;

    private ProgressBar viewMainProgressBar;
    private UserProfile userProfile;

    public static SharedPreferences reminderAlarmStorage;
    public static SharedPreferences reminderIconNames;
    public static SharedPreferences reminderBroadcastIds;
    public static SharedPreferences reminderTimeOfDay;
    public static SharedPreferences reminderNotificationIds;

    private List<ReminderItem> reminderItemList;
    private List<ToDoGroup> toDoGroupList;

    private SectionsPagerAdapter sectionsPagerAdapter;
    private boolean setRemindersTabActive;

    private String newUserDisplayName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewMainProgressBar = findViewById(R.id.view_main_progress_bar);
        viewMainProgressBar.setVisibility(View.VISIBLE);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);

        setRemindersTabActive = false;
        newUserDisplayName = "New User";

        createNotificationChannel();

        Intent intent = getIntent();

        // User entered login info from sign in activity
        if (intent.getBooleanExtra(FirebaseSignInActivity.CHECK_IF_NEW_USER, false))
        {
            // Check if this is a new user by checking if user doc on cloud exists
            if (intent.hasExtra(FirebaseSignInActivity.DISPLAY_NAME))
            {
                newUserDisplayName = intent.getStringExtra(FirebaseSignInActivity.DISPLAY_NAME);
            }

            if (intent.hasExtra(FirebaseSignInActivity.DEVICE_TOKEN))
            {
                deviceToken = intent.getStringExtra(FirebaseSignInActivity.DEVICE_TOKEN);
                Log.d(TAG, "deviceToken retrieved: " + deviceToken);
            }

            checkIfNewUser();
        }
        // User already signed-in
        else
        {
            if (intent.hasExtra(BACK_PRESSED_FROM_REMINDER_DETAILS))
            {
                setRemindersTabActive = true;
            }
            else
            {
                setRemindersTabActive = false;
            }

            loadUserProfileFromCloud();
        }

        Toolbar toolbarMain = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbarMain);

        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconNames = getSharedPreferences(getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        reminderNotificationIds = getSharedPreferences(getString(R.string.reminder_notification_ids_file_key), Context.MODE_PRIVATE);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(BACK_PRESSED_FROM_REMINDER_DETAILS))
        {
            sectionsPagerAdapter.getItem(1);
        }

        if (intent.hasExtra(USER_PREFERENCES_UPDATED))
        {
            Log.d(TAG, "Returned from user prefs which were updated.");

            Gson gson = new Gson();
            Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
            String userProfileString = intent.getStringExtra(USER_PROFILE);
            userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);
            Log.d(TAG, "userProfile Gson String: " + userProfileString);

            //updateReminderTimeOfDay();
            //loadReminderAlarms();
            //setReminderAlarms();
        }

        super.onNewIntent(intent);
    }

    private void setupTabs()
    {
        sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        if (setRemindersTabActive)
        {
            sectionsPagerAdapter.getItem(1);
        }

        viewMainProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        //menu.removeItem(R.id.menu_main_friends);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_logout:
                Log.d(TAG, "Menu: Logout clicked");
                logoutUser();
                return true;

            case R.id.menu_main_friends:
                openFriends();
                return true;

            case R.id.menu_main_user_settings:
                Log.d(TAG, "Menu: User Settings clicked");
                openUserSettings();
                return true;

            case R.id.menu_main_privacy:
                openPrivacyPolicy();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void logoutUser()
    {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        ReminderAlarmUtils.cancelAllReminderAlarms(getApplicationContext());
                        clearAllSharedPreferences();
                        Intent intent = new Intent(MainActivity.this, FirebaseSignInActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });
    }

    public void openUserSettings()
    {
        Intent intent = new Intent(MainActivity.this, UserPreferencesActivity.class);
        Gson gson = new Gson();

        String userProfileString = gson.toJson(userProfile);
        Log.d(TAG, "userProfileString: " + userProfileString);
        intent.putExtra(USER_PROFILE, userProfileString);
        startActivity(intent);
    }

    private void openFriends()
    {
        Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
        Gson gson = new Gson();

        String userProfileString = gson.toJson(userProfile);
        Log.d(TAG, "userProfileString: " + userProfileString);

        String toDoGroupListString = gson.toJson(toDoGroupList);
        Log.d(TAG, "toDoGroupListString: " + toDoGroupListString);

        String reminderItemListString = gson.toJson(reminderItemList);
        Log.d(TAG, "reminderItemListString: " + reminderItemListString);

        intent.putExtra(USER_PROFILE, userProfileString);
        intent.putExtra(TO_DO_GROUP_LIST, toDoGroupListString);
        intent.putExtra(REMINDER_ITEM_LIST, reminderItemListString);
        intent.putExtra(REMINDERS_DOC_ID, remindersDocId);
        startActivity(intent);
    }

    private void openPrivacyPolicy()
    {
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

    public void clearAllSharedPreferences()
    {
        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconNames = getSharedPreferences(getString(R.string.reminder_icon_names_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        reminderTimeOfDay = getSharedPreferences(getString(R.string.reminder_time_of_day_file_key), Context.MODE_PRIVATE);
        reminderNotificationIds = getSharedPreferences(getString(R.string.reminder_notification_ids_file_key), Context.MODE_PRIVATE);

        SharedPreferences.Editor reminderAlarmStorageEditor = reminderAlarmStorage.edit();
        reminderAlarmStorageEditor.clear().apply();

        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        reminderIconNamesEditor.clear().apply();

        SharedPreferences.Editor reminderBroadcastIdsEditor = reminderBroadcastIds.edit();
        reminderBroadcastIdsEditor.clear().apply();

        SharedPreferences.Editor reminderTimeOfDayEditor = reminderTimeOfDay.edit();
        reminderTimeOfDayEditor.clear().apply();

        SharedPreferences.Editor reminderNotificationIdsEditor = reminderNotificationIds.edit();
        reminderNotificationIdsEditor.clear().apply();

        Log.d(TAG, "All SharedPreferences cleared");
    }

    public void checkIfNewUser()
    {
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            // User already exists
                            if (documentSnapshot.exists())
                            {
                                userProfile = FirebaseDocUtils.createUserProfileObj(
                                        documentSnapshot);

                                saveRemindersToStorage();
                                checkFirebaseDeviceToken();
                                //setupTabs();
                            }
                            // New user was created
                            else
                            {
                                createNewReminderDoc();
                            }
                        }
                    }
                });
    }

    public void loadUserProfileFromCloud()
    {
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();

                            userProfile = FirebaseDocUtils.createUserProfileObj(documentSnapshot);
                            ReminderAlarmUtils.setReminderTimeOfDay(getApplicationContext(),
                                    userProfile.getReminderHour(), userProfile.getReminderMinute());

                            Log.d(TAG, "User Profile loaded from cloud");
                            //setupTabs();
                            checkFirebaseDeviceToken();
                        }
                    }
                });
    }

    private void checkFirebaseDeviceToken()
    {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (task.isSuccessful()) {
                            deviceToken = task.getResult().getToken();
                            Log.d(TAG, "Device token received: " + deviceToken);

                            if (!deviceToken.equals(userProfile.getDeviceToken()))
                            {
                                Log.d(TAG, "Device token has changed. Updating FireStore");
                                updateDeviceTokenInDatabase();
                            }
                            else
                            {
                                Log.d(TAG, "Device token has not changed.");
                                setupTabs();
                            }
                        }
                        else
                        {
                            Log.w(TAG, "getInstanceId failed", task.getException());
                            Toast.makeText(MainActivity.this, "Failed to " +
                                    "retrieve device token from Firebase. Please try logging out " +
                                    "and logging in again or contact support.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void updateDeviceTokenInDatabase()
    {
        userDocRef.update("deviceToken", deviceToken)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "deviceToken successfully updated in cloud: " +
                                deviceToken);
                        setupTabs();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failed to update deviceToken in cloud: " + e.getMessage());
                        Toast.makeText(MainActivity.this, "Failed to update " +
                                "deviceToken in cloud. . Please try logging out and logging in " +
                                "again or contact support to enable Friends List.",
                                Toast.LENGTH_LONG).show();
                        setupTabs();
                    }
                });
    }

    public void createNewReminderDoc()
    {
        Map<String, Object> reminderDocMap = new HashMap<>();
        reminderDocMap.put("userId", userId);

        remindersCollectionRef.add(reminderDocMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        remindersDocRef = documentReference;
                        remindersDocId = documentReference.getId();
                        Log.d(TAG, "Reminders document written with ID: " + remindersDocId);
                        createNewUserDoc();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding new reminders document", e);
                    }
                });
    }

    public void createNewUserDoc()
    {
        Map<String, Object> userProfileDoc = new HashMap<>();

        userProfileDoc.put("displayName", newUserDisplayName);

        Map<String, String> reminderCategoriesMap = new HashMap<>();
        reminderCategoriesMap.put("Main", DEFAULT_REMINDER_CATEGORY_ICON_NAME);
        userProfileDoc.put("reminderCategories", reminderCategoriesMap);

        ArrayList<String> subscriptionsList = new ArrayList<>();
        userProfileDoc.put("subscriptions", subscriptionsList);

        userProfileDoc.put("reminderHour", DEFAULT_REMINDER_TIME_HOUR);
        userProfileDoc.put("reminderMinute", DEFAULT_REMINDER_TIME_MINUTE);

        userProfileDoc.put("hibernateLength", DEFAULT_HIBERNATE_LENGTH);

        ArrayList<String> friendsList = new ArrayList<>();
        userProfileDoc.put("friends", friendsList);

        userProfileDoc.put("deviceToken", deviceToken);

        // Set new user profile
        String[] subscriptions = new String[0];
        subscriptions = subscriptionsList.toArray(subscriptions);

        /*
        String[] friends;
        friends = friendsList.toArray(new String[0]);
        */

        Map<String, Object> friendListMap = new HashMap<>();
        userProfileDoc.put("friendListMap", friendListMap);

        userProfile = new UserProfile(userId, newUserDisplayName, subscriptions,
                reminderCategoriesMap, DEFAULT_REMINDER_TIME_HOUR, DEFAULT_REMINDER_TIME_MINUTE,
                DEFAULT_HIBERNATE_LENGTH, deviceToken, friendListMap);

        userDocRef.set(userProfileDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User DocumentSnapshot successfully written!");
                        setupTabs();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding new User document", e);
                    }
                });
    }

    public void setActionBarTitle(String title)
    {
        getSupportActionBar().setTitle(title);
    }

    public void saveRemindersToStorage()
    {
        remindersCollectionRef.whereEqualTo("userId", userId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult())
                            {
                                remindersDocRef = document.getReference();
                                remindersDocId = document.getId();
                                Log.d(TAG, "remindersDocId: " + remindersDocId);

                                ReminderAlarmUtils.setReminderTimeOfDay(getApplicationContext(),
                                        userProfile.getReminderHour(),
                                        userProfile.getReminderMinute());

                                reminderItemList = ReminderAlarmUtils.buildReminderItemList(
                                        document);
                                ReminderAlarmUtils.writeRemindersToDisk(getApplicationContext(),
                                        reminderItemList);

                                //buildReminderItemList(document);
                                //writeRemindersToDisk();

                                ReminderAlarmUtils.updateReminderAlarmsOnTimeSet(
                                        getApplicationContext());

                            }
                        }
                        else
                        {
                            Log.d(TAG, "Error getting documents: " + task.getException());
                        }
                    }
                });

    }

    /*
    public void buildReminderItemList(QueryDocumentSnapshot document)
    {
        reminderItemList = new ArrayList<>();

        Map<String, Object> docMap = document.getData();

        for (Map.Entry<String, Object> entry : docMap.entrySet())
        {
            if (!entry.getKey().equals("userId"))
            {
                ReminderItem reminderItem = FirebaseDocUtils.createReminderItemObj(entry);

                reminderItemList.add(reminderItem);
            }
        }
    }

    public void writeRemindersToDisk()
    {
        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key),
                Context.MODE_PRIVATE);
        reminderIconNames = getSharedPreferences(getString(R.string.reminder_icon_names_file_key),
                Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(
                R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        reminderTimeOfDay = getSharedPreferences(getString(R.string.reminder_time_of_day_file_key),
                Context.MODE_PRIVATE);

        SharedPreferences.Editor reminderAlarmEditor = reminderAlarmStorage.edit();
        SharedPreferences.Editor reminderIconNamesEditor = reminderIconNames.edit();
        SharedPreferences.Editor reminderBroadcastIdEditor = reminderBroadcastIds.edit();
        SharedPreferences.Editor reminderTimeOfDayEditor = reminderTimeOfDay.edit();

        for (ReminderItem reminderItem : reminderItemList)
        {
            reminderAlarmEditor.putString(reminderItem.getTitle(), reminderItem.getNextOccurrence());
            reminderIconNamesEditor.putString(reminderItem.getTitle(), reminderItem.getCategoryIconName());

            int broadcastId = generateUniqueInt();
            reminderBroadcastIdEditor.putInt(reminderItem.getTitle(), broadcastId);
        }

        reminderTimeOfDayEditor.putInt(ReminderAlarmUtils.REMINDER_TIME_HOUR,
                userProfile.getReminderHour());
        reminderTimeOfDayEditor.putInt(ReminderAlarmUtils.REMINDER_TIME_MINUTE,
                userProfile.getReminderMinute());

        // Using commit() because alarms are loaded immediately after write to disk from cloud
        reminderAlarmEditor.commit();
        reminderIconNamesEditor.commit();
        reminderBroadcastIdEditor.commit();
        reminderTimeOfDayEditor.commit();

        Log.d(TAG, "Reminders written to storage");
    }
    */

    private void createNotificationChannel()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.notification_channel_name);
            String description = getString(R.string.notification_channel_description);
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static int generateUniqueInt()
    {
        return (int) (Math.random() * 1000000);
    }

    public List<ToDoGroup> getToDoGroupList() {
        return toDoGroupList;
    }

    public void setToDoGroupList(List<ToDoGroup> toDoGroupList) {
        this.toDoGroupList = toDoGroupList;
    }

    public static DocumentReference getRemindersDocRef() {
        return remindersDocRef;
    }

    public static void setRemindersDocRef(DocumentReference remindersDocRef) {
        MainActivity.remindersDocRef = remindersDocRef;
    }

    public String getRemindersDocId() {
        return remindersDocId;
    }

    public static void setRemindersDocId(String remindersDocId) {
        MainActivity.remindersDocId = remindersDocId;
    }

    public List<ReminderItem> getReminderItemList() {
        return reminderItemList;
    }

    public void setReminderItemList(List<ReminderItem> reminderItemList) {
        this.reminderItemList = reminderItemList;
    }
}