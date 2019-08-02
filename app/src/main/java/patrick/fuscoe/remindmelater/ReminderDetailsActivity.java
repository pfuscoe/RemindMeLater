package patrick.fuscoe.remindmelater;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.ui.dialog.AddReminderCategoryDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.DatePickerDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;

public class ReminderDetailsActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener, DatePickerDialogFragment.OnDateSetListener,
        AddReminderCategoryDialogFragment.AddReminderCategoryDialogListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderDetailsActivity";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");
    private final String userId = auth.getUid();
    private String remindersDocId;
    private DocumentReference remindersDocRef;

    private ReminderItem reminderItem;
    private LocalDate dateShown;

    private String recurrenceInterval;
    private int recurrenceNum;

    private EditText viewTitle;
    private ImageView viewCategoryIcon;
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
        if (view.getId() == R.id.view_reminder_details_recurrence_spinner)
        {
            recurrenceInterval = (String) parent.getItemAtPosition(pos);
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
        Type dataType = new TypeToken<ReminderItem>(){}.getType();

        String reminderItemString = intent.getStringExtra(RemindersFragment.REMINDER_ITEM);
        reminderItem = gson.fromJson(reminderItemString, dataType);

        remindersDocId = intent.getStringExtra(RemindersFragment.REMINDERS_DOC_ID);
        remindersDocRef = remindersCollectionRef.document(remindersDocId);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(reminderItem.getTitle());

        // Setup views, fields, buttons, etc.
        viewTitle = findViewById(R.id.view_reminder_details_title);
        viewCategoryIcon = findViewById(R.id.view_reminder_details_category_icon);
        viewRecurrenceNum = findViewById(R.id.view_reminder_details_recurrence_num);
        viewDateDisplay = findViewById(R.id.view_reminder_details_date_display);
        viewDescription = findViewById(R.id.view_reminder_details_description);

        // Setup Recurrence Spinner
        viewRecurrenceSpinner = findViewById(R.id.view_reminder_details_recurrence_spinner);
        ArrayAdapter<CharSequence> recurrenceAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_recurrence, android.R.layout.simple_spinner_item);
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewRecurrenceSpinner.setAdapter(recurrenceAdapter);
        viewRecurrenceSpinner.setOnItemSelectedListener(this);

        // Setup Buttons
        btnSetDate = findViewById(R.id.view_reminder_details_date_button);
        btnSetDate.setOnClickListener(btnClickListener);
        btnCancel = findViewById(R.id.view_reminder_details_button_cancel);
        btnCancel.setOnClickListener(btnClickListener);
        btnSave = findViewById(R.id.view_reminder_details_button_save);
        btnSave.setOnClickListener(btnClickListener);

        updateFields();

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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                Log.d(TAG, ": Add Button pressed");
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
        DialogFragment dialogFragment = new AddReminderCategoryDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "addReminderCategory");
    }

    public void openDatePicker()
    {
        DialogFragment dialogFragment = new DatePickerDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        LocalDate localDate = LocalDate.of(year, month, dayOfMonth);
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
    }

    public void saveReminder()
    {
        updateReminderItemObject();

        HashMap<String, Object> reminderItemMap = new HashMap<>();
        reminderItemMap.put("recurrence", reminderItem.getRecurrence().toString());
        reminderItemMap.put("recurrenceNum", reminderItem.getRecurrenceNum());
        reminderItemMap.put("recurrenceInterval", reminderItem.getRecurrenceInterval());
        reminderItemMap.put("nextOccurrence", reminderItem.getNextOccurrence());
        reminderItemMap.put("category", reminderItem.getCategory());
        reminderItemMap.put("description", reminderItem.getDescription());

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
                    }
                });

        onBackPressed();
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof AddReminderCategoryDialogFragment)
        {
            Dialog dialogView = dialog.getDialog();
            EditText viewCategoryName = dialogView.findViewById(R.id.dialog_category_edit_name);
            String categoryName = viewCategoryName.getText().toString();
            int selectedIcon = ((AddReminderCategoryDialogFragment) dialog).getSelectedIcon();

            reminderItem.setCategory(categoryName);
            reminderItem.setCategoryIcon(selectedIcon);

            // TODO: save new category to object / user profile
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof AddReminderCategoryDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Add Reminder Category Cancelled", Toast.LENGTH_SHORT).show();
        }
    }
}
