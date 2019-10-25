package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class ReminderCategoriesActivity extends AppCompatActivity {


    private ReminderCategoryClickListener reminderCategoryClickListener = new ReminderCategoryClickListener() {
        @Override
        public void reminderCategoryClicked(View v, int position) {

        }
    };

    public interface ReminderCategoryClickListener {
        void reminderCategoryClicked(View v, int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_categories);


    }
}
