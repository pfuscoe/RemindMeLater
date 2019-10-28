package patrick.fuscoe.remindmelater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ReminderCategory;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.AddCategoryDialogFragment;
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
    private ArrayList<ReminderItem> reminderItemList;

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
        Toolbar toolbar = findViewById(R.id.toolbar_reminder_categories);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);
        remindersDocRef = MainActivity.remindersDocRef;

        hasChanged = false;
        reminderItemList = new ArrayList<>();

        Intent intent = getIntent();
        Gson gson = new Gson();

        Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
        String userProfileString = intent.getStringExtra(MainActivity.USER_PROFILE);
        Log.d(TAG, "userProfileString: " + userProfileString);
        userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);

        //reminderCategoriesUsed = intent.getStringArrayListExtra(RemindersFragment.REMINDER_CATEGORIES_USED);

        Type dataTypeReminderItemList = new TypeToken<ArrayList<ReminderItem>>(){}.getType();
        String reminderItemListString = intent.getStringExtra(RemindersFragment.REMINDER_ITEMS);
        reminderItemList = gson.fromJson(reminderItemListString, dataTypeReminderItemList);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Edit Reminder Categories");

        viewReminderCategoriesRecycler = findViewById(R.id.view_reminder_categories_recycler);

        reminderCategoryList = new ArrayList<>();

        buildReminderCategoryList(userProfile.getReminderCategories());
        buildReminderCategoriesUsedList(userProfile.getReminderCategories());
        Log.d(TAG, "reminderCategoriesUsed: " + reminderCategoriesUsed.toString());

        updateReminderCategoriesDisplay();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        menu.removeItem(R.id.menu_main_edit);
        menu.removeItem(R.id.menu_main_logout);
        menu.removeItem(R.id.menu_main_user_settings);
        menu.removeItem(R.id.menu_main_reorder);
        menu.removeItem(R.id.menu_main_edit_reminder_categories);
        menu.removeItem(R.id.menu_main_tips);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                openAddReminderCategoryDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
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

    private void buildReminderCategoriesUsedList(Map<String, String> reminderCategoriesMap)
    {
        reminderCategoriesUsed = new ArrayList<>();

        for (Map.Entry<String, String> entry : reminderCategoriesMap.entrySet())
        {
            String userProfileCategoryName = entry.getKey();

            for (ReminderItem reminderItem : reminderItemList)
            {
                String reminderItemCategoryName = reminderItem.getCategory();

                if (userProfileCategoryName.equals(reminderItemCategoryName))
                {
                    reminderCategoriesUsed.add(reminderItemCategoryName);
                    break;
                }
            }
        }
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

    public void updateReminderItemsOnCategoryEdit(ReminderCategory updatedReminderCategory)
    {
        String oldReminderCategoryName = reminderCategoryToEdit.getCategoryName();
        String newReminderCategoryName = updatedReminderCategory.getCategoryName();
        String newReminderCategoryIconName = updatedReminderCategory.getIconName();

        for (ReminderItem reminderItem : reminderItemList)
        {
            if (oldReminderCategoryName.equals(reminderItem.getCategory()))
            {
                reminderItem.setCategory(newReminderCategoryName);
                reminderItem.setCategoryIconName(newReminderCategoryIconName);

                Log.d(TAG, reminderItem.getTitle() + " Category and Icon updated");
            }
        }

        int index = reminderCategoryList.indexOf(reminderCategoryToEdit);
        reminderCategoryList.set(index, updatedReminderCategory);
        reminderCategoriesAdapter.notifyDataSetChanged();

        /*
        remindersDocRef.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful())
                        {
                            DocumentSnapshot documentSnapshot = task.getResult();
                        }
                    }
                });
        */
    }

    public void deleteReminderCategory(ReminderCategory reminderCategory)
    {
        userProfile.removeReminderCategory(reminderCategory.getCategoryName());
        reminderCategoryList.remove(reminderCategory);
        reminderCategoriesAdapter.notifyDataSetChanged();
    }

    private Map<String, Object> buildRemindersDoc(ArrayList<ReminderItem> reminderItemList)
    {
        Map<String, Object> reminderDocMap = new HashMap<>();

        for (ReminderItem reminderItem : reminderItemList)
        {
            HashMap<String, Object> reminderItemMap = new HashMap<>();

            reminderItemMap.put("recurrence", reminderItem.getRecurrenceString());
            reminderItemMap.put("recurrenceNum", reminderItem.getRecurrenceNum());
            reminderItemMap.put("recurrenceInterval", reminderItem.getRecurrenceInterval());
            reminderItemMap.put("nextOccurrence", reminderItem.getNextOccurrence());
            reminderItemMap.put("category", reminderItem.getCategory());
            reminderItemMap.put("categoryIconName", reminderItem.getCategoryIconName());
            reminderItemMap.put("description", reminderItem.getDescription());
            reminderItemMap.put("isRecurring", reminderItem.isRecurring());
            reminderItemMap.put("isSnoozed", reminderItem.isSnoozed());
            reminderItemMap.put("isHibernating", reminderItem.isHibernating());
            reminderItemMap.put("history", reminderItem.getHistory());

            reminderDocMap.put(reminderItem.getTitle(), reminderItemMap);
        }

        return reminderDocMap;
    }

    private Map<String, Object> buildUserProfileDoc(UserProfile userProfile)
    {
        Map<String, Object> userProfileDoc = new HashMap<>();

        userProfileDoc.put("displayName", userProfile.getDisplayName());
        userProfileDoc.put("subscriptions", Arrays.asList(userProfile.getSubscriptions()));
        userProfileDoc.put("reminderCategories", userProfile.getReminderCategories());
        userProfileDoc.put("reminderHour", MainActivity.reminderTimeHour);
        userProfileDoc.put("reminderMinute", MainActivity.reminderTimeMinute);
        userProfileDoc.put("hibernateLength", userProfile.getHibernateLength());
        userProfileDoc.put("friends", Arrays.asList(userProfile.getFriends()));

        return userProfileDoc;
    }

    public void saveChanges(Map<String, Object> remindersDocMap, Map<String, Object> userProfileDocMap)
    {
        WriteBatch batch = db.batch();
        batch.set(remindersDocRef, remindersDocMap);
        batch.set(userDocRef, userProfileDocMap);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful())
                {
                    Log.d(TAG, "Reminders and UserProfile Doc Batch successfully written!");
                    Toast.makeText(getApplicationContext(), "Applied changes to Reminder Categories",
                            Toast.LENGTH_LONG).show();
                }
                else
                {
                    Log.d(TAG, "Error writing document to cloud");
                    if (task.getException() != null)
                    {
                        Log.d(TAG, task.getException().getMessage());
                    }

                    Toast.makeText(getApplicationContext(), "Error saving data to cloud",
                            Toast.LENGTH_LONG).show();
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "Error writing document to cloud: " + e.getMessage());
                Toast.makeText(getApplicationContext(), "Error saving data to cloud: "
                                + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        /*
        Map<String, Object> reminderDocMap = new HashMap<>();

        for (ReminderItem reminderItem : reminderItemList)
        {
            HashMap<String, Object> reminderItemMap = new HashMap<>();
            reminderItemMap.put("recurrence", reminderItem.getRecurrenceString());
            reminderItemMap.put("recurrenceNum", reminderItem.getRecurrenceNum());
            reminderItemMap.put("recurrenceInterval", reminderItem.getRecurrenceInterval());
            reminderItemMap.put("nextOccurrence", reminderItem.getNextOccurrence());
            reminderItemMap.put("category", reminderItem.getCategory());
            reminderItemMap.put("categoryIconName", reminderItem.getCategoryIconName());
            reminderItemMap.put("description", reminderItem.getDescription());
            reminderItemMap.put("isRecurring", reminderItem.isRecurring());
            reminderItemMap.put("isSnoozed", reminderItem.isSnoozed());
            reminderItemMap.put("isHibernating", reminderItem.isHibernating());
            reminderItemMap.put("history", reminderItem.getHistory());

            reminderDocMap.put(reminderItem.getTitle(), reminderItemMap);
        }

        remindersDocRef.set(reminderDocMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Reminders DocumentSnapshot successfully written!");
                        updateUserProfileDoc();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(getApplicationContext(), "Error saving data to cloud: "
                                + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        */
    }

    /*
    public void updateUserProfileDoc()
    {
        Map<String, Object> userProfileDoc = new HashMap<>();

        userProfileDoc.put("displayName", userProfile.getDisplayName());
        userProfileDoc.put("subscriptions", Arrays.asList(userProfile.getSubscriptions()));
        userProfileDoc.put("reminderCategories", userProfile.getReminderCategories());
        userProfileDoc.put("reminderHour", MainActivity.reminderTimeHour);
        userProfileDoc.put("reminderMinute", MainActivity.reminderTimeMinute);
        userProfileDoc.put("hibernateLength", userProfile.getHibernateLength());
        userProfileDoc.put("friends", Arrays.asList(userProfile.getFriends()));

        userDocRef.set(userProfileDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "UserProfile DocumentSnapshot successfully written!");
                        Toast.makeText(getApplicationContext(), "Applied changes to Reminder Categories",
                                Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }
    */

    private void openAddReminderCategoryDialog()
    {
        DialogFragment dialogFragment = new AddCategoryDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "addReminderCategory");
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

        if (dialogFragment instanceof AddCategoryDialogFragment)
        {
            Dialog dialogView = dialogFragment.getDialog();
            EditText viewCategoryName = dialogView.findViewById(R.id.dialog_category_edit_name);
            String categoryName = viewCategoryName.getText().toString();

            if (categoryName.equals(""))
            {
                Toast.makeText(this, "Add Reminder Category Failed: Category Name Must Not Be Blank", Toast.LENGTH_LONG).show();
                return;
            }

            String selectedIconName = ((AddCategoryDialogFragment) dialogFragment).getSelectedIconName();

            userProfile.addReminderCategory(categoryName, selectedIconName);
            ReminderCategory reminderCategory = new ReminderCategory(categoryName, selectedIconName);

            reminderCategoryList.add(reminderCategory);
            reminderCategoriesAdapter.notifyDataSetChanged();
        }
        else if (dialogFragment instanceof EditReminderCategoryDialogFragment)
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

            ReminderCategory updatedReminderCategory = new ReminderCategory(newTitle, selectedIconName);

            updateReminderItemsOnCategoryEdit(updatedReminderCategory);
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
            Map<String, Object> remindersDocMap = buildRemindersDoc(reminderItemList);
            Map<String, Object> userProfileDocMap = buildUserProfileDoc(userProfile);

            saveChanges(remindersDocMap, userProfileDocMap);
        }
    }
}
