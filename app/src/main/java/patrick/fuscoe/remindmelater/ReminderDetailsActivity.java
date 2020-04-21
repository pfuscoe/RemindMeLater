package patrick.fuscoe.remindmelater;

import android.app.Dialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderCategory;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.AddCategoryDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.DatePickerDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.DeleteReminderDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;
import patrick.fuscoe.remindmelater.ui.reminder.ReminderCategorySpinnerAdapter;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;
import patrick.fuscoe.remindmelater.util.ReminderAlarmUtils;
import patrick.fuscoe.remindmelater.util.ReminderTimeUtils;

/**
 * Manages UI for editing reminder details. Also handles cloud sync when saving changes or
 * deleting a reminder.
 *
 * Can enter activity from MainActivity through RemindersFragment, or directly from
 * notification tap.
*/
public class ReminderDetailsActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener, DatePickerDialogFragment.OnDateSetListener,
        AddCategoryDialogFragment.AddCategoryDialogListener,
        DeleteReminderDialogFragment.DeleteReminderDialogListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderDetailsActivity";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef =
            db.collection("reminders");
    private final String userId = auth.getUid();
    private final DocumentReference userDocRef =
            db.collection("users").document(userId);

    private String remindersDocId;
    private DocumentReference remindersDocRef;

    private ReminderItem reminderItem;
    private UserProfile userProfile;

    private ProgressBar viewProgressBar;
    private ConstraintLayout viewContentConstraintLayout;
    private EditText viewTitle;
    private ImageView viewCategoryIcon;
    private Spinner viewCategorySpinner;
    private TextView viewAddNewCategory;
    private CheckBox viewRecurringCheckbox;
    private EditText viewRecurrenceNum;
    private Spinner viewRecurrenceSpinner;
    private Button btnSyncNext;
    private TextView viewDateDisplay;
    private Button btnSetDate;
    private CheckBox viewSnoozedCheckbox;
    private ImageView viewSnoozedIcon;
    private CheckBox viewHibernatingCheckbox;
    private ImageView viewHibernatingIcon;
    private EditText viewDescription;
    private Button btnCancel;
    private Button btnSave;

    private boolean reminderTitleChanged;
    private String oldReminderTitle;

    private boolean enteredActivityFromNotification;


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

                case R.id.view_reminder_details_checkbox_recurring:
                    toggleRecurring();
                    return;

                case R.id.view_reminder_details_sync_next:
                    syncNextOccurrence();
                    return;

                case R.id.view_reminder_details_checkbox_snoozed:
                    toggleSnoozed();
                    return;

                case R.id.view_reminder_details_checkbox_hibernating:
                    toggleHibernating();
                    return;

                case R.id.view_reminder_details_button_cancel:
                    Log.d(TAG, ": Add/Edit Reminder Cancelled");
                    Toast.makeText(getApplicationContext(), "Add/Edit Reminder Cancelled",
                            Toast.LENGTH_LONG).show();
                    onBackPressed();
                    return;

                case R.id.view_reminder_details_button_save:
                    Log.d(TAG, "Save Clicked");

                    // Calls onBackPressed
                    checkIfTitleChanged();

                    return;
            }
        }
    };

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
    {
        // User selected recurrence in spinner
        if (parent.getId() == R.id.view_reminder_details_recurrence_spinner)
        {
            reminderItem.setRecurrenceInterval((String) parent.getItemAtPosition(pos));
            Log.d(TAG, ": Recurrence Interval changed.");
        }
        // User selected reminder category in spinner
        else if (parent.getId() == R.id.view_reminder_details_category_spinner)
        {
            ReminderCategory reminderCategory = (ReminderCategory) parent.getItemAtPosition(pos);
            reminderItem.setCategory(reminderCategory.getCategoryName());
            reminderItem.setCategoryIconName(reminderCategory.getIconName());
            viewCategoryIcon.setImageResource(getResources().getIdentifier(
                    reminderItem.getCategoryIconName(), "drawable", getPackageName()));
            Log.d(TAG, ": Reminder Category Changed.");
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
    }

    @Override
    public void onBackPressed() {
        Intent backIntent = new Intent(getApplicationContext(), MainActivity.class);
        backIntent.putExtra(MainActivity.BACK_PRESSED_FROM_REMINDER_DETAILS, TAG);

        if (enteredActivityFromNotification)
        {
            backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        startActivity(backIntent);
        finish();
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
        reminderTitleChanged = false;

        remindersDocId = intent.getStringExtra(RemindersFragment.REMINDERS_DOC_ID);
        remindersDocRef = remindersCollectionRef.document(remindersDocId);

        viewProgressBar = findViewById(R.id.view_reminder_details_progress_bar);
        viewContentConstraintLayout = findViewById(
                R.id.view_reminder_details_content_constraint_layout);
        viewCategorySpinner = findViewById(R.id.view_reminder_details_category_spinner);

        // Entered activity from Reminders Fragment
        if (intent.hasExtra(RemindersFragment.USER_PROFILE))
        {
            enteredActivityFromNotification = false;

            Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
            String userProfileString = intent.getStringExtra(RemindersFragment.USER_PROFILE);
            userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);
            Log.d(TAG, "User Profile obtained from intent");
            Log.d(TAG, " userProfile Gson String: " + userProfileString);

            updateCategorySelectSpinner();
            setupRecurrenceSpinner();
        }
        // Entered activity from notification tap
        else
        {
            enteredActivityFromNotification = true;

            // Fills views after user profile loaded from cloud
            loadUserProfile();
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (reminderItem.getTitle().equals(""))
        {
            getSupportActionBar().setTitle(R.string.new_reminder);
        }
        else
        {
            getSupportActionBar().setTitle(reminderItem.getTitle());
        }

        // Setup views and fields
        viewTitle = findViewById(R.id.view_reminder_details_title);
        viewCategoryIcon = findViewById(R.id.view_reminder_details_category_icon);
        viewRecurringCheckbox = findViewById(R.id.view_reminder_details_checkbox_recurring);
        viewRecurrenceNum = findViewById(R.id.view_reminder_details_recurrence_num);
        viewDateDisplay = findViewById(R.id.view_reminder_details_date_display);
        viewSnoozedCheckbox = findViewById(R.id.view_reminder_details_checkbox_snoozed);
        viewSnoozedIcon = findViewById(R.id.view_reminder_details_snoozed_icon);
        viewSnoozedIcon.setColorFilter(getColor(R.color.red));
        viewHibernatingCheckbox = findViewById(R.id.view_reminder_details_checkbox_hibernating);
        viewHibernatingIcon = findViewById(R.id.view_reminder_details_hibernating_icon);
        viewDescription = findViewById(R.id.view_reminder_details_description);

        viewRecurringCheckbox.setOnClickListener(btnClickListener);
        viewSnoozedCheckbox.setOnClickListener(btnClickListener);
        viewHibernatingCheckbox.setOnClickListener(btnClickListener);

        // Setup Buttons and View Click Listeners
        viewAddNewCategory = findViewById(R.id.view_reminder_details_category_add);
        viewAddNewCategory.setOnClickListener(btnClickListener);
        btnSyncNext = findViewById(R.id.view_reminder_details_sync_next);
        btnSyncNext.setOnClickListener(btnClickListener);
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

    private void toggleRecurring()
    {
        if (viewRecurringCheckbox.isChecked())
        {
            viewRecurrenceNum.setVisibility(View.VISIBLE);
            viewRecurrenceSpinner.setVisibility(View.VISIBLE);
            btnSyncNext.setVisibility(View.VISIBLE);
        }
        else
        {
            viewRecurrenceNum.setVisibility(View.INVISIBLE);
            viewRecurrenceSpinner.setVisibility(View.INVISIBLE);
            btnSyncNext.setVisibility(View.GONE);
        }
    }

    private void toggleSnoozed()
    {
        if (viewSnoozedCheckbox.isChecked())
        {
            viewSnoozedIcon.setVisibility(View.VISIBLE);
            viewHibernatingCheckbox.setChecked(false);
            viewHibernatingIcon.setVisibility(View.INVISIBLE);
        }
        else
        {
            viewSnoozedIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void toggleHibernating()
    {
        if (viewHibernatingCheckbox.isChecked())
        {
            viewHibernatingIcon.setVisibility(View.VISIBLE);
            viewSnoozedCheckbox.setChecked(false);
            viewSnoozedIcon.setVisibility(View.INVISIBLE);
        }
        else
        {
            viewHibernatingIcon.setVisibility(View.INVISIBLE);
        }
    }

    private void showSnoozed()
    {
        viewSnoozedCheckbox.setVisibility(View.VISIBLE);
        viewSnoozedIcon.setVisibility(View.VISIBLE);
    }

    private void hideSnoozed()
    {
        viewSnoozedCheckbox.setVisibility(View.GONE);
        viewSnoozedIcon.setVisibility(View.GONE);
    }

    private void showHibernating()
    {
        viewHibernatingCheckbox.setVisibility(View.VISIBLE);
        viewHibernatingIcon.setVisibility(View.VISIBLE);
    }

    private void hideHibernating()
    {
        viewHibernatingCheckbox.setVisibility(View.GONE);
        viewHibernatingIcon.setVisibility(View.GONE);
    }

    private void updateCategorySelectSpinner()
    {
        ReminderCategorySpinnerAdapter reminderCategorySpinnerAdapter =
                new ReminderCategorySpinnerAdapter(
                getApplicationContext(), userProfile.getReminderCategories());
        viewCategorySpinner.setAdapter(reminderCategorySpinnerAdapter);
        setCategorySpinnerSelection(reminderCategorySpinnerAdapter);
        viewCategorySpinner.setOnItemSelectedListener(this);
    }

    private void setCategorySpinnerSelection(
            ReminderCategorySpinnerAdapter reminderCategorySpinnerAdapter)
    {
        List<ReminderCategory> reminderCategoryList =
                reminderCategorySpinnerAdapter.getReminderCategories();

        int mainCategoryPosition = 0;
        int categoryPosition = 0;
        int counter = 0;

        for (ReminderCategory reminderCategory : reminderCategoryList)
        {
            if (reminderCategory.getIconName().equals(reminderItem.getCategoryIconName()))
            {
                categoryPosition = counter;
                viewCategorySpinner.setSelection(categoryPosition);
                return;
            }
            else if (reminderCategory.getCategoryName().equals("Main"))
            {
                mainCategoryPosition = counter;
            }

            counter++;
        }

        // No match found - Must be new reminder. Set default category to 'Main'
        viewCategorySpinner.setSelection(mainCategoryPosition);
    }

    private void setupRecurrenceSpinner()
    {
        viewRecurrenceSpinner = findViewById(R.id.view_reminder_details_recurrence_spinner);
        ArrayAdapter<CharSequence> recurrenceAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_recurrence, android.R.layout.simple_spinner_item);
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewRecurrenceSpinner.setAdapter(recurrenceAdapter);
        viewRecurrenceSpinner.setOnItemSelectedListener(this);
    }

    private void updateFields()
    {
        if (reminderItem.getTitle().equals(""))
        {
            viewTitle.setHint(R.string.new_reminder);
        }
        else
        {
            viewTitle.setText(reminderItem.getTitle());
        }

        viewRecurrenceNum.setText(String.valueOf(reminderItem.getRecurrenceNum()));
        Log.d(TAG, ": nextOccurrence.toString: " + reminderItem.getNextOccurrence());
        setViewDateDisplay(reminderItem.getNextOccurrence());
        viewRecurringCheckbox.setChecked(reminderItem.isRecurring());
        viewSnoozedCheckbox.setChecked(reminderItem.isSnoozed());
        viewHibernatingCheckbox.setChecked(reminderItem.isHibernating());
        viewDescription.setText(reminderItem.getDescription());

        if (reminderItem.isSnoozed())
        {
            showSnoozed();
            showHibernating();
        }

        if (reminderItem.isHibernating())
        {
            showSnoozed();
            showHibernating();
        }

        toggleRecurring();
        toggleSnoozed();
        toggleHibernating();

        if (reminderItem.getCategoryIconName().equals("Main"))
        {
            viewCategoryIcon.setImageResource(getResources().getIdentifier(
                    MainActivity.DEFAULT_REMINDER_CATEGORY_ICON_NAME, "drawable",
                    getPackageName()));
        }
        else
        {
            viewCategoryIcon.setImageResource(getResources().getIdentifier(
                    reminderItem.getCategoryIconName(), "drawable", getPackageName()));
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

        menu.removeItem(R.id.menu_main_add);
        menu.removeItem(R.id.menu_main_logout);
        menu.removeItem(R.id.menu_main_friends);
        menu.removeItem(R.id.menu_main_user_settings);
        menu.removeItem(R.id.menu_main_reorder);
        menu.removeItem(R.id.menu_main_edit_reminder_categories);
        menu.removeItem(R.id.menu_main_tips);
        menu.removeItem(R.id.menu_main_feedback);
        menu.removeItem(R.id.menu_main_privacy);

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

            case R.id.menu_main_privacy:
                openPrivacyPolicy();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openAddReminderCategoryDialog()
    {
        DialogFragment dialogFragment = new AddCategoryDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "addReminderCategory");
    }

    private void openDatePicker()
    {
        DialogFragment dialogFragment = new DatePickerDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void openDeleteReminderDialog()
    {
        DialogFragment dialogFragment = new DeleteReminderDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", reminderItem.getTitle());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "deleteReminder");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        LocalDate localDate = LocalDate.of(year, month + 1, dayOfMonth);
        setNextOccurrenceDate(localDate);
        viewSnoozedCheckbox.setChecked(false);
        viewSnoozedIcon.setVisibility(View.INVISIBLE);
        viewHibernatingCheckbox.setChecked(false);
        viewHibernatingIcon.setVisibility(View.INVISIBLE);
    }

    private void setNextOccurrenceDate(LocalDate localDate)
    {
        String date = localDate.toString();
        reminderItem.setNextOccurrence(date);
        setViewDateDisplay(date);
    }

    private void syncNextOccurrence()
    {
        String recurrenceNumString = viewRecurrenceNum.getText().toString();
        int recurrenceNum = Integer.parseInt(recurrenceNumString);
        String recurrenceInterval = viewRecurrenceSpinner.getSelectedItem().toString();

        Period recurrence = ReminderTimeUtils.createRecurrencePeriod(recurrenceNum,
                recurrenceInterval);

        LocalDate nextOccurrence = ReminderTimeUtils.calcNextOccurrenceFromRecurrence(recurrence);

        setNextOccurrenceDate(nextOccurrence);
    }

    private void setViewDateDisplay(String localDateString)
    {
        LocalDate localDate = LocalDate.parse(localDateString);

        String monthString = localDate.getMonth().toString();
        monthString = monthString.charAt(0) + monthString.substring(1).toLowerCase();
        int day = localDate.getDayOfMonth();
        int year = localDate.getYear();

        String dateDisplayString = monthString + " " + day + ", " + year;

        viewDateDisplay.setText(dateDisplayString);
    }

    private void checkIfTitleChanged()
    {
        String title = viewTitle.getText().toString();

        if (!title.equals(reminderItem.getTitle()))
        {
            Log.d(TAG, "Title has changed.  Old title: " + reminderItem.getTitle() +
                    ". New title: " + title);
            reminderTitleChanged = true;
            oldReminderTitle = reminderItem.getTitle();

            if (oldReminderTitle.equals(""))
            {
                saveReminder();
            }
            else
            {
                deleteReminder(oldReminderTitle);
            }
        }
        else
        {
            saveReminder();
        }
    }

    // Get values from fields and update reminderItem object
    private void updateReminderItemObject()
    {
        String title = viewTitle.getText().toString();
        boolean isRecurring = viewRecurringCheckbox.isChecked();
        String recurrenceNumString = viewRecurrenceNum.getText().toString();
        int recurrenceNum = Integer.parseInt(recurrenceNumString);
        String recurrenceInterval = viewRecurrenceSpinner.getSelectedItem().toString();
        String nextOccurrence = reminderItem.getNextOccurrence();
        String description = viewDescription.getText().toString();
        boolean isSnoozed = viewSnoozedCheckbox.isChecked();
        boolean isHibernating = viewHibernatingCheckbox.isChecked();

        reminderItem.setTitle(title);
        reminderItem.setRecurring(isRecurring);
        reminderItem.setRecurrenceNum(recurrenceNum);
        reminderItem.setRecurrenceInterval(recurrenceInterval);
        reminderItem.setNextOccurrence(nextOccurrence);
        reminderItem.setDescription(description);
        reminderItem.setSnoozed(isSnoozed);
        reminderItem.setHibernating(isHibernating);

        reminderItem.updateRecurrencePeriod();
        reminderItem.updateDaysAway(nextOccurrence);
    }

    private void saveReminder()
    {
        final String title = viewTitle.getText().toString();

        if (title.equals(""))
        {
            Toast.makeText(this, "Cannot Save Reminder: You must enter a title",
                    Toast.LENGTH_LONG).show();
            return;
        }

        updateReminderItemObject();

        Map<String, Object> reminderItemMap = FirebaseDocUtils.createReminderItemMap(reminderItem);

        showProgressBar();

        remindersDocRef.update(title, reminderItemMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Reminders DocumentSnapshot successfully updated: "
                                + reminderItem.getTitle());

                        ReminderAlarmUtils.cancelReminderAlarm(getApplicationContext(),
                                reminderItem.getTitle());
                        ReminderAlarmUtils.cancelNotification(getApplicationContext(),
                                reminderItem.getTitle());

                        ReminderAlarmUtils.saveReminderToSharedPreferences(getApplicationContext(),
                                reminderItem);
                        ReminderAlarmUtils.setReminderAlarm(getApplicationContext(), reminderItem);

                        Toast.makeText(getApplicationContext(), "Reminder Item Saved: " +
                                title, Toast.LENGTH_SHORT).show();

                        onBackPressed();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        Log.w(TAG, "Error updating reminders document", e);
                        Toast.makeText(getApplicationContext(), "Action failed due to " +
                                "network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void deleteReminder(String reminderTitle)
    {
        final String title = reminderTitle;
        final boolean isReminderTitleChanged = reminderTitleChanged;

        Map<String, Object> removeReminderUpdate = new HashMap<>();
        removeReminderUpdate.put(title, FieldValue.delete());

        showProgressBar();

        remindersDocRef.update(removeReminderUpdate)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Successfully deleted reminder on cloud: " + title);

                        ReminderAlarmUtils.cancelReminderAlarm(getApplicationContext(), title);
                        ReminderAlarmUtils.cancelNotification(getApplicationContext(), title);
                        ReminderAlarmUtils.deleteReminderFromSharedPreferences(
                                getApplicationContext(), title);

                        if (isReminderTitleChanged)
                        {
                            saveReminder();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "Reminder Item Deleted: "
                                    + title, Toast.LENGTH_SHORT).show();
                            onBackPressed();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressBar();
                        Log.w(TAG, "Error updating reminders document", e);
                        Toast.makeText(getApplicationContext(), "Error syncing delete " +
                                "action to cloud. Delete cancelled.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserProfile()
    {
        Map<String, Object> userProfileDoc = FirebaseDocUtils.createUserProfileDoc(userProfile);

        userDocRef.set(userProfileDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "UserProfile DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void loadUserProfile()
    {
        showProgressBar();

        userDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            userProfile = FirebaseDocUtils.createUserProfileObj(documentSnapshot);
                            Log.d(TAG, "userProfile loaded from cloud");

                            updateCategorySelectSpinner();
                            setupRecurrenceSpinner();
                            updateFields();

                            hideProgressBar();
                        }
                    }
                });
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof AddCategoryDialogFragment)
        {
            Dialog dialogView = dialog.getDialog();
            EditText viewCategoryName = dialogView.findViewById(R.id.dialog_category_edit_name);
            String categoryName = viewCategoryName.getText().toString();

            if (categoryName.equals(""))
            {
                Toast.makeText(this, "Add Reminder Category Failed: Category Name " +
                        "Must Not Be Blank", Toast.LENGTH_LONG).show();
                return;
            }

            reminderItem.setCategory(categoryName);

            String selectedIconName = ((AddCategoryDialogFragment) dialog).getSelectedIconName();

            if (selectedIconName.equals("default"))
            {
                reminderItem.setCategoryIconName(MainActivity.DEFAULT_REMINDER_CATEGORY_ICON_NAME);
            }
            else
            {
                reminderItem.setCategoryIconName(selectedIconName);
            }

            viewCategoryIcon.setImageResource(getResources().getIdentifier(
                    reminderItem.getCategoryIconName(), "drawable", getPackageName()));

            userProfile.addReminderCategory(categoryName, selectedIconName);

            saveUserProfile();
            updateCategorySelectSpinner();

            Toast.makeText(getApplicationContext(), "New Reminder Category Added: "
                    + categoryName, Toast.LENGTH_SHORT).show();
        }
        else if (dialog instanceof DeleteReminderDialogFragment)
        {
            deleteReminder(reminderItem.getTitle());
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof AddCategoryDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Add Reminder Category Cancelled",
                    Toast.LENGTH_SHORT).show();
        }
        else if (dialog instanceof DeleteReminderDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Delete Reminder Cancelled",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void openPrivacyPolicy()
    {
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

    private void showProgressBar()
    {
        viewProgressBar.setVisibility(View.VISIBLE);
        viewContentConstraintLayout.setVisibility(View.INVISIBLE);
    }

    private void hideProgressBar()
    {
        viewProgressBar.setVisibility(View.INVISIBLE);
        viewContentConstraintLayout.setVisibility(View.VISIBLE);
    }
}
