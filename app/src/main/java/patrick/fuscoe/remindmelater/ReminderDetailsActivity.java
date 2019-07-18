package patrick.fuscoe.remindmelater;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;

public class ReminderDetailsActivity extends AppCompatActivity {

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
    }
}
