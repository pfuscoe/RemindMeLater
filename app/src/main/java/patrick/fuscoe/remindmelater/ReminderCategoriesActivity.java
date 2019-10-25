package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderCategory;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.main.ReminderCategoriesAdapter;

public class ReminderCategoriesActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderCategoriesActivity";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    //private final CollectionReference remindersCollectionRef = db.collection("reminders");

    public static FirebaseAuth auth;
    public static String userId;
    public static DocumentReference userDocRef;
    public static DocumentReference remindersDocRef;

    private RecyclerView viewReminderCategoriesRecycler;
    private RecyclerView.Adapter reminderCategoriesRecyclerAdapter;
    private RecyclerView.LayoutManager reminderCategoriesRecyclerLayoutManager;
    private ReminderCategoriesAdapter reminderCategoriesAdapter;

    private UserProfile userProfile;

    public List<ReminderCategory> reminderCategoryList;


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

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);
        remindersDocRef = MainActivity.remindersDocRef;

        Intent intent = getIntent();
        Gson gson = new Gson();

        Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
        String userProfileString = intent.getStringExtra(MainActivity.USER_PROFILE);

        userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Reminder Categories");

        viewReminderCategoriesRecycler = findViewById(R.id.view_reminder_categories_recycler);

        reminderCategoryList = new ArrayList<>();

        buildReminderCategoryList(userProfile.getReminderCategories());

        updateReminderCategoriesDisplay();
    }

    public void updateReminderCategoriesDisplay()
    {
        reminderCategoriesAdapter = new ReminderCategoriesAdapter(reminderCategoryList,
                this, reminderCategoryClickListener);
        viewReminderCategoriesRecycler.setAdapter(reminderCategoriesAdapter);

        reminderCategoriesAdapter.notifyDataSetChanged();
    }

    public void buildReminderCategoryList(Map<String, String> reminderCategoriesMap)
    {
        for (Map.Entry<String, String> entry : reminderCategoriesMap.entrySet())
        {
            ReminderCategory reminderCategory = new ReminderCategory(entry.getKey(), entry.getValue());

            if (!reminderCategory.getCategoryName().equals("Main"))
            {
                reminderCategoryList.add(reminderCategory);
            }
        }

        Collections.sort(reminderCategoryList);
    }
}
