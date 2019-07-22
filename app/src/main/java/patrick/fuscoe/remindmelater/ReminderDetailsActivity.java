package patrick.fuscoe.remindmelater;

import android.content.Intent;
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
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.time.LocalDate;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;

public class ReminderDetailsActivity extends AppCompatActivity
        implements AdapterView.OnItemSelectedListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderDetailsActivity";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");
    private final String userId = auth.getUid();

    private ReminderItem reminderItem;
    private LocalDate dateShown;

    private String recurrenceInterval;
    private int recurrenceNum;

    private EditText viewTitle;
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

        String reminderItemString = intent.getStringExtra(RemindersFragment.REMINDERS);
        reminderItem = gson.fromJson(reminderItemString, dataType);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(reminderItem.getTitle());

        // Setup views, fields, buttons, etc.
        viewTitle = findViewById(R.id.view_reminder_details_title);
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
        viewRecurrenceNum.setText(reminderItem.getRecurrenceNum());
        viewDateDisplay.setText(reminderItem.getNextOccurrence().toString());
        viewDescription.setText(reminderItem.getDescription());

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

    public void openDatePicker()
    {
        // TODO: Setup DatePickerDialog
    }

    public void saveReminder()
    {
        // TODO: commit and close activity
    }

}
