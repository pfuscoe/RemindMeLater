package patrick.fuscoe.remindmelater;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderCategory;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.receiver.ReminderAlarmReceiver;
import patrick.fuscoe.remindmelater.ui.dialog.AddCategoryDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.DatePickerDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.DeleteReminderDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;
import patrick.fuscoe.remindmelater.ui.reminder.ReminderCategorySpinnerAdapter;

public class ReminderDetailsActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener, DatePickerDialogFragment.OnDateSetListener,
        AddCategoryDialogFragment.AddCategoryDialogListener,
        DeleteReminderDialogFragment.DeleteReminderDialogListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderDetailsActivity";

    private static SharedPreferences reminderAlarmStorage;
    private static SharedPreferences reminderIconIds;
    private static SharedPreferences reminderBroadcastIds;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");
    private final String userId = auth.getUid();
    private final DocumentReference userDocRef = db.collection("users").document(userId);

    private String remindersDocId;
    private DocumentReference remindersDocRef;

    private ReminderItem reminderItem;
    private UserProfile userProfile;
    private LocalDate dateShown;

    private String recurrenceInterval;
    private int recurrenceNum;

    private EditText viewTitle;
    private ImageView viewCategoryIcon;
    private Spinner viewCategorySpinner;
    private TextView viewAddNewCategory;
    private EditText viewRecurrenceNum;
    private Spinner viewRecurrenceSpinner;
    private TextView viewDateDisplay;
    private Button btnSetDate;
    private EditText viewDescription;
    private Button btnCancel;
    private Button btnSave;


    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.view_reminder_details_category_add:
                    openAddReminderCategoryDialog();
                    return;

                case R.id.view_reminder_details_date_button:
                    openDatePicker();
                    return;

                case R.id.view_reminder_details_button_cancel:
                    Log.d(TAG, ": Add/Edit Reminder Cancelled");
                    Toast.makeText(getApplicationContext(), "Add/Edit Reminder Cancelled", Toast.LENGTH_LONG).show();
                    onBackPressed();
                    return;

                case R.id.view_reminder_details_button_save:
                    Log.d(TAG, ": Reminder " + reminderItem.getTitle() + " saved");
                    Toast.makeText(getApplicationContext(), "Saved Reminder: " + reminderItem.getTitle(), Toast.LENGTH_LONG).show();
                    saveReminder();
                    return;

            }
        }
    };

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        if (parent.getId() == R.id.view_reminder_details_recurrence_spinner)
        {
            recurrenceInterval = (String) parent.getItemAtPosition(pos);
            Log.d(TAG, ": Recurrence Interval changed.");
        }
        else if (parent.getId() == R.id.view_reminder_details_category_spinner)
        {
            ReminderCategory reminderCategory = (ReminderCategory) parent.getItemAtPosition(pos);
            reminderItem.setCategory(reminderCategory.getCategoryName());
            reminderItem.setCategoryIcon(reminderCategory.getIconId());
            viewCategoryIcon.setImageResource(reminderItem.getCategoryIcon());
            Log.d(TAG, ": Reminder Category Changed.");
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_reminder_details);
        Toolbar toolbar = findViewById(R.id.toolbar_reminder_details);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        Gson gson = new Gson();

        Type dataTypeReminderItem = new TypeToken<ReminderItem>(){}.getType();
        String reminderItemString = intent.getStringExtra(RemindersFragment.REMINDER_ITEM);
        reminderItem = gson.fromJson(reminderItemString, dataTypeReminderItem);

        remindersDocId = intent.getStringExtra(RemindersFragment.REMINDERS_DOC_ID);
        remindersDocRef = remindersCollectionRef.document(remindersDocId);

        viewCategorySpinner = findViewById(R.id.view_reminder_details_category_spinner);

        if (intent.hasExtra(RemindersFragment.USER_PROFILE))
        {
            Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
            String userProfileString = intent.getStringExtra(RemindersFragment.USER_PROFILE);
            userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);
            Log.d(TAG, "User Profile obtained from intent");
            Log.d(TAG, " userProfile Gson String: " + userProfileString);

            updateCategorySelectSpinner();
        }
        else
        {
            loadUserProfile();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(reminderItem.getTitle());

        // Setup views, fields, buttons, etc.
        viewTitle = findViewById(R.id.view_reminder_details_title);
        viewCategoryIcon = findViewById(R.id.view_reminder_details_category_icon);
        viewRecurrenceNum = findViewById(R.id.view_reminder_details_recurrence_num);
        viewDateDisplay = findViewById(R.id.view_reminder_details_date_display);
        viewDescription = findViewById(R.id.view_reminder_details_description);

        /*
        // Setup category select spinner
        ReminderCategorySpinnerAdapter reminderCategorySpinnerAdapter = new ReminderCategorySpinnerAdapter(
                getApplicationContext(), userProfile.getReminderCategories());
        viewCategorySpinner.setAdapter(reminderCategorySpinnerAdapter);
        setCategorySpinnerSelection(reminderCategorySpinnerAdapter);
        viewCategorySpinner.setOnItemSelectedListener(this);
        //reminderCategorySpinnerAdapter.notifyDataSetChanged();
        */

        // Setup Recurrence Spinner
        viewRecurrenceSpinner = findViewById(R.id.view_reminder_details_recurrence_spinner);
        ArrayAdapter<CharSequence> recurrenceAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_recurrence, android.R.layout.simple_spinner_item);
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewRecurrenceSpinner.setAdapter(recurrenceAdapter);
        viewRecurrenceSpinner.setOnItemSelectedListener(this);

        // Setup Buttons and View Click Listeners
        viewAddNewCategory = findViewById(R.id.view_reminder_details_category_add);
        viewAddNewCategory.setOnClickListener(btnClickListener);
        btnSetDate = findViewById(R.id.view_reminder_details_date_button);
        btnSetDate.setOnClickListener(btnClickListener);
        btnCancel = findViewById(R.id.view_reminder_details_button_cancel);
        btnCancel.setOnClickListener(btnClickListener);
        btnSave = findViewById(R.id.view_reminder_details_button_save);
        btnSave.setOnClickListener(btnClickListener);

        if (intent.hasExtra(RemindersFragment.USER_PROFILE)) {
            updateFields();
        }
    }

    public void updateCategorySelectSpinner()
    {
        ReminderCategorySpinnerAdapter reminderCategorySpinnerAdapter = new ReminderCategorySpinnerAdapter(
                getApplicationContext(), userProfile.getReminderCategories());
        viewCategorySpinner.setAdapter(reminderCategorySpinnerAdapter);
        setCategorySpinnerSelection(reminderCategorySpinnerAdapter);
        viewCategorySpinner.setOnItemSelectedListener(this);
    }

    public void setCategorySpinnerSelection(ReminderCategorySpinnerAdapter reminderCategorySpinnerAdapter)
    {
        List<ReminderCategory> reminderCategoryList = reminderCategorySpinnerAdapter.getReminderCategories();

        //ReminderCategory reminderCategory = new ReminderCategory("Main", R.drawable.category_note);
        int categoryPosition = 0;
        int counter = 0;

        for (ReminderCategory item : reminderCategoryList)
        {
            if (item.getIconId() == reminderItem.getCategoryIcon())
            {
                categoryPosition = counter;
                //reminderCategory = item;
            }
            counter++;
        }

        viewCategorySpinner.setSelection(categoryPosition);
    }

    public void updateFields()
    {
        viewTitle.setText(reminderItem.getTitle());
        viewRecurrenceNum.setText(String.valueOf(reminderItem.getRecurrenceNum()));
        Log.d(TAG, ": nextOccurrence.toString: " + reminderItem.getNextOccurrence());
        viewDateDisplay.setText(reminderItem.getNextOccurrence());
        viewDescription.setText(reminderItem.getDescription());

        if (reminderItem.getCategoryIcon() == -1)
        {
            viewCategoryIcon.setImageResource(R.drawable.category_note);
        }
        else
        {
            viewCategoryIcon.setImageResource(reminderItem.getCategoryIcon());
        }


        switch (reminderItem.getRecurrenceInterval())
        {
            case "Days":
                viewRecurrenceSpinner.setSelection(0);
                break;

            case "Weeks":
                viewRecurrenceSpinner.setSelection(1);
                break;

            case "Months":
                viewRecurrenceSpinner.setSelection(2);
                break;

            case "Years":
                viewRecurrenceSpinner.setSelection(3);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        // Swap edit icon for trash can
        MenuItem editIcon = menu.findItem(R.id.menu_main_edit);
        editIcon.setIcon(R.drawable.action_delete);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                Log.d(TAG, ": Add Button pressed");
                return true;

            case R.id.menu_main_edit:
                Log.d(TAG, "Delete menu icon pressed");
                openDeleteReminderDialog();
                return true;

            case R.id.menu_main_user_settings:
                Log.d(TAG, ": Menu item selected: " + item.getItemId());
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void openAddReminderCategoryDialog()
    {
        DialogFragment dialogFragment = new AddCategoryDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "addReminderCategory");
    }

    public void openDatePicker()
    {
        DialogFragment dialogFragment = new DatePickerDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    public void openDeleteReminderDialog()
    {
        DialogFragment dialogFragment = new DeleteReminderDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "deleteReminder");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        LocalDate localDate = LocalDate.of(year, month + 1, dayOfMonth);
        setNextOccurrenceDate(localDate);
    }

    public void setNextOccurrenceDate(LocalDate localDate)
    {
        String date = localDate.toString();
        reminderItem.setNextOccurrence(date);
        viewDateDisplay.setText(date);
    }

    // Get values from fields and update reminderItem object
    public void updateReminderItemObject()
    {
        String title = viewTitle.getText().toString();
        String recurrenceNumString = viewRecurrenceNum.getText().toString();
        int recurrenceNum = Integer.parseInt(recurrenceNumString);
        String recurrenceInterval = viewRecurrenceSpinner.getSelectedItem().toString();
        String nextOccurrence = viewDateDisplay.getText().toString();
        //LocalDate nextOccurrence = LocalDate.parse(nextOccurrenceString);
        String description = viewDescription.getText().toString();

        reminderItem.setTitle(title);
        reminderItem.setRecurrenceNum(recurrenceNum);
        reminderItem.setRecurrenceInterval(recurrenceInterval);
        reminderItem.setNextOccurrence(nextOccurrence);
        reminderItem.setDescription(description);

        reminderItem.updateRecurrencePeriod();
        reminderItem.updateDaysAway(nextOccurrence);
    }

    public void saveReminder()
    {
        updateReminderItemObject();

        HashMap<String, Object> reminderItemMap = new HashMap<>();
        reminderItemMap.put("recurrence", reminderItem.getRecurrenceString());
        reminderItemMap.put("recurrenceNum", reminderItem.getRecurrenceNum());
        reminderItemMap.put("recurrenceInterval", reminderItem.getRecurrenceInterval());
        reminderItemMap.put("nextOccurrence", reminderItem.getNextOccurrence());
        reminderItemMap.put("category", reminderItem.getCategory());
        reminderItemMap.put("categoryIcon", reminderItem.getCategoryIcon());
        reminderItemMap.put("description", reminderItem.getDescription());

        saveReminderToSharedPreferences();

        remindersDocRef.update(reminderItem.getTitle(), reminderItemMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Reminders DocumentSnapshot successfully updated!");
                        Toast.makeText(getApplicationContext(), "Reminder Item Updated: " + reminderItem.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating reminders document", e);
                        // TODO: handle local storage of reminder when cloud sync fails
                    }
                });

        onBackPressed();
    }

    public void saveReminderToSharedPreferences()
    {
        //SharedPreferences reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderAlarmStorage = MainActivity.reminderAlarmStorage;
        SharedPreferences.Editor reminderAlarmEditor = reminderAlarmStorage.edit();

        reminderAlarmEditor.putString(reminderItem.getTitle(), reminderItem.getNextOccurrence());
        reminderAlarmEditor.apply();

        SharedPreferences reminderIconIds = MainActivity.reminderIconIds;
        SharedPreferences.Editor reminderIconIdEditor = reminderIconIds.edit();

        reminderIconIdEditor.putInt(reminderItem.getTitle(), reminderItem.getCategoryIcon());
        reminderIconIdEditor.apply();

        //SharedPreferences reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);
        SharedPreferences reminderBroadcastIds = MainActivity.reminderBroadcastIds;
        SharedPreferences.Editor reminderBroadcastIdEditor = reminderBroadcastIds.edit();

        int broadcastId = (int) System.currentTimeMillis();
        // TODO: Add check for existing id
        reminderBroadcastIdEditor.putInt(reminderItem.getTitle(), broadcastId);
        reminderBroadcastIdEditor.apply();

        // TODO: using apply() for async saving. Check if commit() needed
    }

    public void deleteReminder()
    {
        Map<String, Object> removeReminderUpdate = new HashMap<>();
        removeReminderUpdate.put(reminderItem.getTitle(), FieldValue.delete());

        remindersDocRef.update(removeReminderUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Delete Reminder Success: Reminders DocumentSnapshot successfully updated!");
                        cancelReminderAlarm();
                        removeReminderLocalStorage();
                        Toast.makeText(getApplicationContext(), "Reminder Item Deleted: " + reminderItem.getTitle(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating reminders document", e);
                        // TODO: handle local storage of reminder when cloud sync fails
                    }
                });

        onBackPressed();
    }

    public void cancelReminderAlarm()
    {
        Context context = getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        reminderAlarmStorage = getSharedPreferences(getString(R.string.reminders_file_key), Context.MODE_PRIVATE);
        reminderIconIds = getSharedPreferences(getString(R.string.reminder_icon_ids_file_key), Context.MODE_PRIVATE);
        reminderBroadcastIds = getSharedPreferences(getString(R.string.reminder_broadcast_ids_file_key), Context.MODE_PRIVATE);

        int broadcastId = reminderBroadcastIds.getInt(reminderItem.getTitle(), 0);

        Intent intent = new Intent(context, ReminderAlarmReceiver.class);
        intent.setAction(MainActivity.ACTION_ALARM_RECEIVER);

        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, broadcastId, intent, 0);

        alarmManager.cancel(alarmIntent);
    }

    public void removeReminderLocalStorage()
    {
        SharedPreferences.Editor reminderAlarmStorageEditor = reminderAlarmStorage.edit();
        reminderAlarmStorageEditor.remove(reminderItem.getTitle()).commit();

        SharedPreferences.Editor reminderIconIdsEditor = reminderIconIds.edit();
        reminderIconIdsEditor.remove(reminderItem.getTitle()).commit();

        SharedPreferences.Editor reminderBroadcastIdsEditor = reminderBroadcastIds.edit();
        reminderBroadcastIdsEditor.remove(reminderItem.getTitle()).commit();

        Log.d(TAG, "Reminder removed from local storage: " + reminderItem.getTitle());
    }

    public void saveUserProfile()
    {
        Map<String, Object> userProfileDoc = new HashMap<>();
        userProfileDoc.put("displayName", userProfile.getDisplayName());
        userProfileDoc.put("subscriptions", Arrays.asList(userProfile.getSubscriptions()));
        userProfileDoc.put("reminderCategories", userProfile.getReminderCategories());

        userDocRef.set(userProfileDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });

        Log.d(TAG, userProfile.getDisplayName() + " User Profile Updated");
    }

    public void loadUserProfile()
    {
        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            buildUserProfileObj(documentSnapshot);
                            updateCategorySelectSpinner();
                            updateFields();
                        }
                    }
                });
    }

    public void buildUserProfileObj(DocumentSnapshot documentSnapshot)
    {
        Map<String, Object> docMap = documentSnapshot.getData();

        String id = documentSnapshot.getId();
        String displayName = documentSnapshot.getString("displayName");

        ArrayList<String> subscriptionsList = (ArrayList<String>) docMap.get("subscriptions");

        String[] subscriptions = new String[subscriptionsList.size()];
        subscriptions = subscriptionsList.toArray(subscriptions);

        Map<String, Integer> reminderCategories =
                (Map<String, Integer>) documentSnapshot.get("reminderCategories");

        userProfile = new UserProfile(id, displayName, subscriptions, reminderCategories);

        Log.d(TAG, ": userProfile loaded from cloud");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof AddCategoryDialogFragment)
        {
            Dialog dialogView = dialog.getDialog();
            EditText viewCategoryName = dialogView.findViewById(R.id.dialog_category_edit_name);
            String categoryName = viewCategoryName.getText().toString();
            int selectedIconId = ((AddCategoryDialogFragment) dialog).getSelectedIconId();

            reminderItem.setCategory(categoryName);
            reminderItem.setCategoryIcon(selectedIconId);

            viewCategoryIcon.setImageResource(selectedIconId);

            userProfile.addReminderCategory(categoryName, selectedIconId);
            saveUserProfile();
            updateCategorySelectSpinner();
            Toast.makeText(getApplicationContext(), "New Reminder Category Added: " + categoryName, Toast.LENGTH_SHORT).show();
        }
        else if (dialog instanceof DeleteReminderDialogFragment)
        {
            deleteReminder();
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof AddCategoryDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Add Reminder Category Cancelled", Toast.LENGTH_SHORT).show();
        }
        else if (dialog instanceof DeleteReminderDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Delete Reminder Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
