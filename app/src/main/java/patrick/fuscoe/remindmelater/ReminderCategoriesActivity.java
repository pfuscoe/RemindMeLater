package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import patrick.fuscoe.remindmelater.ui.dialog.DeleteReminderCategoryDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.EditReminderCategoryDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.ReminderCategoriesAdapter;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;

public class ReminderCategoriesActivity extends AppCompatActivity implements
        DeleteReminderCategoryDialogFragment.DeleteReminderCategoryDialogListener {

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

    private ArrayList<String> reminderCategoriesUsed;
    public List<ReminderCategory> reminderCategoryList;

    private ReminderCategory reminderCategoryToEdit;
    private ReminderCategory reminderCategoryToDelete;

    private boolean hasChanged;


    private ReminderCategoryClickListener reminderCategoryClickListener = new ReminderCategoryClickListener() {
        @Override
        public void reminderCategoryClicked(View v, int position) {

            ReminderCategory reminderCategory = reminderCategoryList.get(position);

            if (v.getId() == R.id.view_row_reminder_category_delete_icon)
            {
                if (isReminderCategoryEmpty(reminderCategory))
                {
                    reminderCategoryToDelete = reminderCategory;
                    openConfirmDeleteReminderCategoryDialog(reminderCategory);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "There are Reminders in this category! " +
                            "Please reassign or delete them before proceeding.", Toast.LENGTH_LONG).show();
                }
            }
            else
            {
                reminderCategoryToEdit = reminderCategory;
                openEditReminderCategoryDialog(reminderCategory);
            }
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

        hasChanged = false;
        reminderCategoriesUsed = new ArrayList<>();

        Intent intent = getIntent();
        Gson gson = new Gson();

        Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
        String userProfileString = intent.getStringExtra(MainActivity.USER_PROFILE);
        reminderCategoriesUsed = intent.getStringArrayListExtra(RemindersFragment.REMINDER_CATEGORIES_USED);
        //String[] reminderCategoriesUsedArray = intent.getStringArrayExtra(RemindersFragment.REMINDER_CATEGORIES_USED);

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

    private boolean isReminderCategoryEmpty(ReminderCategory reminderCategory)
    {
        String reminderCategoryName = reminderCategory.getCategoryName();

        for (String usedCategoryName : reminderCategoriesUsed)
        {
            if (usedCategoryName.equals(reminderCategoryName))
            {
                return false;
            }
        }

        return true;
    }

    public void deleteReminderCategory(ReminderCategory reminderCategory)
    {
        userProfile.removeReminderCategory(reminderCategory.getCategoryName());
    }

    public void saveUserProfile()
    {

    }

    private void openConfirmDeleteReminderCategoryDialog(ReminderCategory reminderCategory)
    {
        DialogFragment dialogFragment = new DeleteReminderCategoryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", reminderCategory.getCategoryName());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "deleteReminderCategory");
    }

    private void openEditReminderCategoryDialog(ReminderCategory reminderCategory)
    {
        DialogFragment dialogFragment = new EditReminderCategoryDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", reminderCategory.getCategoryName());
        bundle.putString("iconName", reminderCategory.getIconName());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(getSupportFragmentManager(), "editReminderCategory");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment) {

        hasChanged = true;

        if (dialogFragment instanceof EditReminderCategoryDialogFragment)
        {
            Dialog dialogView = dialogFragment.getDialog();
            EditText viewReminderCategoryName = dialogView.findViewById(R.id.dialog_category_edit_name);
            String newTitle = viewReminderCategoryName.getText().toString();

            if (newTitle.equals(""))
            {
                Toast.makeText(this, "Edit Reminder Category Failed: Category Name Must Not Be Blank", Toast.LENGTH_LONG).show();
                return;
            }

            String selectedIconName = ((EditReminderCategoryDialogFragment) dialogFragment).getSelectedIconName();
            
        }
        else if (dialogFragment instanceof DeleteReminderCategoryDialogFragment)
        {
            deleteReminderCategory(reminderCategoryToDelete);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialogFragment) {
        if (dialogFragment instanceof EditReminderCategoryDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Edit Reminder Category Cancelled", Toast.LENGTH_SHORT).show();
        }
        else if (dialogFragment instanceof DeleteReminderCategoryDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Delete Reminder Category Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (hasChanged)
        {
            saveUserProfile();
        }
    }
}
