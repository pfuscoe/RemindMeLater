package patrick.fuscoe.remindmelater;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.comparator.SortToDoItemByDate;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.ToDoItem;
import patrick.fuscoe.remindmelater.ui.dialog.AddToDoItemDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.EditToDoItemDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.ToDoFragment;
import patrick.fuscoe.remindmelater.ui.main.ToDoGroupViewModel;
import patrick.fuscoe.remindmelater.ui.main.ToDoGroupViewModelFactory;
import patrick.fuscoe.remindmelater.ui.todoitem.ToDoItemListAdapter;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;

/**
 * Manages UI and manipulation of individual to do items within a to do group (i.e. to do list)
*/
public class ToDoItemListActivity extends AppCompatActivity implements
        AddToDoItemDialogFragment.AddToDoItemDialogListener,
        EditToDoItemDialogFragment.EditToDoItemDialogListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoItemListActivity";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView toDoItemListRecyclerView;
    private RecyclerView.Adapter toDoItemListAdapter;
    private RecyclerView.LayoutManager toDoItemListLayoutManager;

    private ToDoGroup toDoGroup;
    private String toDoGroupId;
    private List<ToDoItem> toDoItemList;
    private List<ToDoItem> toDoItemListDone;
    private List<ToDoItem> toDoItemListUnsorted;

    private ToDoItem toDoItemToEdit;

    private boolean hasChanged;
    private boolean isShared;
    private boolean justOpened;

    private ToDoGroupViewModel toDoGroupViewModel;
    private ToDoGroupViewModelFactory toDoGroupViewModelFactory;


    private ToDoItemClickListener toDoItemClickListener = new ToDoItemClickListener() {
        @Override
        public void toDoItemClicked(View v, int position) {

            Log.d(TAG, ": To Do Item at pos: " + position + " clicked");
            int numItemsToDo = toDoItemList.size();

            if (position == 0 || position == numItemsToDo + 1)
            {
                // do nothing
            }
            else if (position <= numItemsToDo)
            {
                if (v.getId() == R.id.view_to_do_item_edit_icon)
                {
                    toDoItemToEdit = toDoItemList.get(position - 1);

                    showEditToDoItemDialog();
                }
                else
                {
                    final ToDoItem item = toDoItemList.get(position - 1);
                    CheckBox checkBox = v.findViewById(R.id.view_to_do_item_priority_checkbox);
                    checkBox.setChecked(true);

                    // Short delay to allow checkbox animation to complete
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            markToDoItemDone(item);
                        }
                    }, 500);
                }
            }
            else
            {
                ToDoItem item = toDoItemListDone.get(position - numItemsToDo - 2);

                if (v.getId() == R.id.view_to_do_item_done_delete_icon)
                {
                    deleteToDoItem(item);
                }
                else
                {
                    markToDoItemNotDone(item);
                }
            }
        }
    };

    public interface ToDoItemClickListener {
        void toDoItemClicked(View v, int position);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_to_do_item_list);
        Toolbar toolbar = findViewById(R.id.toolbar_to_do_item_list);
        setSupportActionBar(toolbar);

        // Get To Do Group from intent
        Intent intent = getIntent();
        Gson gson = new Gson();
        Type dataType = new TypeToken<ToDoGroup>(){}.getType();

        String toDoGroupString = intent.getStringExtra(ToDoFragment.TO_DO_GROUP);
        toDoGroup = gson.fromJson(toDoGroupString, dataType);

        toDoItemListUnsorted = toDoGroup.getToDoItemArrayList();
        splitAndSortToDoItems();

        toDoGroupId = toDoGroup.getId();
        Log.d(TAG, "toDoGroupId: " + toDoGroupId);

        hasChanged = false;
        justOpened = true;

        if (toDoGroup.getSubscribers().length > 1)
        {
            isShared = true;
            loadToDoGroupObserver();
        }
        else
        {
            isShared = false;
        }

        // Setup RecyclerView
        toDoItemListRecyclerView = findViewById(R.id.view_to_do_item_list_recycler);
        toDoItemListRecyclerView.setHasFixedSize(false);

        toDoItemListLayoutManager = new LinearLayoutManager(this);
        toDoItemListRecyclerView.setLayoutManager(toDoItemListLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(toDoItemListRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        toDoItemListRecyclerView.addItemDecoration(dividerItemDecoration);

        toDoItemListAdapter = new ToDoItemListAdapter(toDoItemList, toDoItemListDone, this, toDoItemClickListener);
        toDoItemListRecyclerView.setAdapter(toDoItemListAdapter);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(toDoGroup.getTitle());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (toDoItemListUnsorted.isEmpty())
        {
            showAddToDoItemDialog();
        }
    }

    private void loadToDoGroupObserver()
    {
        toDoGroupViewModelFactory = new ToDoGroupViewModelFactory(toDoGroupId);
        toDoGroupViewModel = ViewModelProviders.of(this, toDoGroupViewModelFactory)
                .get(ToDoGroupViewModel.class);
        LiveData<DocumentSnapshot> toDoGroupLiveData =
                toDoGroupViewModel.getDocumentSnapshotLiveData();

        toDoGroupLiveData.observe(this, new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null)
                {
                    toDoGroup = FirebaseDocUtils.createToDoGroupObj(documentSnapshot);
                    Log.d(TAG, "toDoGroup loaded");

                    getSupportActionBar().setTitle(toDoGroup.getTitle());
                    toDoItemListUnsorted = toDoGroup.getToDoItemArrayList();
                    splitAndSortToDoItems();

                    UpdateToDoItemListDisplay();
                }
            }
        });
    }

    public void UpdateToDoItemListDisplay()
    {
        toDoItemListAdapter = new ToDoItemListAdapter(toDoItemList, toDoItemListDone, this, toDoItemClickListener);
        toDoItemListRecyclerView.setAdapter(toDoItemListAdapter);

        toDoItemListAdapter.notifyDataSetChanged();

        if (!justOpened)
        {
            if (isShared)
            {
                commitToDoGroup();
            }
        }
        else
        {
            justOpened = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        menu.removeItem(R.id.menu_main_logout);
        menu.removeItem(R.id.menu_main_friends);
        menu.removeItem(R.id.menu_main_user_settings);
        menu.removeItem(R.id.menu_main_reorder);
        menu.removeItem(R.id.menu_main_edit_reminder_categories);
        menu.removeItem(R.id.menu_main_tips);
        menu.removeItem(R.id.menu_main_edit);
        menu.removeItem(R.id.menu_main_privacy);
        menu.removeItem(R.id.menu_main_feedback);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                Log.d(TAG, ": Add Button pressed");
                showAddToDoItemDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void splitAndSortToDoItems()
    {
        toDoItemList = new ArrayList<>();
        toDoItemListDone = new ArrayList<>();

        for (ToDoItem item : toDoItemListUnsorted)
        {
            if (item.isDone())
            {
                toDoItemListDone.add(item);
            }
            else
            {
                toDoItemList.add(item);
            }
        }

        Collections.sort(toDoItemList);
        Collections.sort(toDoItemListDone, new SortToDoItemByDate());
    }

    public void addToDoItem(String itemName, int priority)
    {
        ToDoItem newItem = new ToDoItem(itemName, priority);

        toDoGroup.addToDoItem(newItem);
        toDoItemListUnsorted = toDoGroup.getToDoItemArrayList();
        splitAndSortToDoItems();

        hasChanged = true;
        UpdateToDoItemListDisplay();
    }

    public void editToDoItem(String itemName, int priority)
    {
        ToDoItem updatedToDoItem = new ToDoItem(itemName, priority);

        toDoGroup.updateToDoItem(toDoItemToEdit, updatedToDoItem);

        toDoItemListUnsorted = toDoGroup.getToDoItemArrayList();
        splitAndSortToDoItems();

        hasChanged = true;
        UpdateToDoItemListDisplay();
    }

    public void markToDoItemDone(ToDoItem toDoItem)
    {
        toDoItem.setDone(true);
        toDoItem.setTimestamp(Timestamp.now());
        toDoItemListDone.add(0, toDoItem);

        toDoItemList.remove(toDoItem);

        toDoGroup.decreaseNumUnfinishedItems();

        if (toDoItem.getPriority() == 1)
        {
            toDoGroup.decreaseNumPriorityOneItems();
        }

        hasChanged = true;
        UpdateToDoItemListDisplay();
    }

    public void markToDoItemNotDone(ToDoItem toDoItem)
    {
        toDoItem.setDone(false);
        toDoItem.setTimestamp(Timestamp.now());
        toDoItemList.add(toDoItem);
        Collections.sort(toDoItemList);

        toDoItemListDone.remove(toDoItem);

        toDoGroup.increaseNumUnfinishedItems();

        if (toDoItem.getPriority() == 1)
        {
            toDoGroup.increaseNumPriorityOneItems();
        }

        hasChanged = true;
        UpdateToDoItemListDisplay();
    }

    public void deleteToDoItem(ToDoItem toDoItem)
    {
        toDoGroup.removeToDoItem(toDoItem);
        toDoItemListUnsorted = toDoGroup.getToDoItemArrayList();

        toDoItemListDone.remove(toDoItem);

        hasChanged = true;
        UpdateToDoItemListDisplay();
    }

    private void commitToDoGroup()
    {
        Map<String, Object> toDoGroupDoc = FirebaseDocUtils.createToDoGroupDoc(toDoGroup);

        db.collection("todogroups").document(toDoGroupId)
                .set(toDoGroupDoc)
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

        hasChanged = false;
        Log.d(TAG, ": To Do Group " + toDoGroup.getTitle() + " updated");
        Toast.makeText(this, "To Do List updated: " + toDoGroup.getTitle(), Toast.LENGTH_LONG).show();
    }

    public void showAddToDoItemDialog()
    {
        FragmentManager fm = getSupportFragmentManager();
        AddToDoItemDialogFragment dialogFrag = new AddToDoItemDialogFragment();

        dialogFrag.show(fm, AddToDoItemDialogFragment.TAG);
    }

    public void showEditToDoItemDialog()
    {
        FragmentManager fm = getSupportFragmentManager();
        EditToDoItemDialogFragment dialogFragment = new EditToDoItemDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString("itemName", toDoItemToEdit.getItemName());
        bundle.putInt("priority", toDoItemToEdit.getPriority());
        dialogFragment.setArguments(bundle);
        dialogFragment.show(fm, EditToDoItemDialogFragment.TAG);
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();

        EditText viewAddToDoItemName = dialogView.findViewById(R.id.dialog_add_to_do_item_name);
        String newItemName = viewAddToDoItemName.getText().toString();

        if (newItemName.equals(""))
        {
            Toast.makeText(this, "Add/Edit To Do Item Failed: Name Must Not Be Empty", Toast.LENGTH_LONG).show();
            return;
        }

        RadioGroup radioGroupPriority = dialogView.findViewById(R.id.dialog_add_to_do_item_radio_group);
        int radioPriorityCheckedId = radioGroupPriority.getCheckedRadioButtonId();
        int priority = processRadioPrioritySelection(radioPriorityCheckedId);

        if (dialog instanceof AddToDoItemDialogFragment)
        {
            addToDoItem(newItemName, priority);
        }
        else if (dialog instanceof EditToDoItemDialogFragment)
        {
            editToDoItem(newItemName, priority);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof AddToDoItemDialogFragment)
        {
            Toast.makeText(this, "Add To Do Item Cancelled", Toast.LENGTH_SHORT).show();
        }
        else if (dialog instanceof EditToDoItemDialogFragment)
        {
            Toast.makeText(this, "Edit To Do Item Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private int processRadioPrioritySelection(int radioPriorityCheckedId)
    {
        switch (radioPriorityCheckedId)
        {
            case R.id.dialog_add_to_do_item_radio_high:
                return 1;

            case R.id.dialog_add_to_do_item_radio_medium:
                return 2;

            case R.id.dialog_add_to_do_item_radio_low:
                return 3;

            default:
                return 1;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (hasChanged) {
            commitToDoGroup();
        }
    }
}
