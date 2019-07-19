package patrick.fuscoe.remindmelater;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;

public class ReminderDetailsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    private ReminderItem reminderItem;

    private ReminderDetailsClickListener reminderDetailsClickListener = new ReminderDetailsClickListener() {
        @Override
        public void saveDetailsClicked() {
            // TODO: update reminder item
        }
    };

    public interface ReminderDetailsClickListener {
        void saveDetailsClicked();
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
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

        // TODO: setup views, fields, buttons, etc.

        // Setup Recurrence Spinner
        Spinner viewRecurrenceSpinner = (Spinner) findViewById(R.id.view_reminder_details_recurrence_spinner);
        ArrayAdapter<CharSequence> recurrenceAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_recurrence, android.R.layout.simple_spinner_item);
        recurrenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewRecurrenceSpinner.setAdapter(recurrenceAdapter);
        viewRecurrenceSpinner.setOnItemSelectedListener(this);

    }
}
