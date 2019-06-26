package patrick.fuscoe.remindmelater;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.ToDoItem;
import patrick.fuscoe.remindmelater.ui.dialog.AddToDoItemDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.ToDoFragment;
import patrick.fuscoe.remindmelater.ui.todoitem.ToDoItemListAdapter;

public class ToDoItemListActivity extends AppCompatActivity implements AddToDoItemDialogFragment.AddToDoItemDialogListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoItemListActivity";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private RecyclerView toDoItemListRecyclerView;
    private RecyclerView.Adapter toDoItemListAdapter;
    private RecyclerView.LayoutManager toDoItemListLayoutManager;

    private ToDoGroup toDoGroup;
    private String toDoGroupId;
    private List<ToDoItem> toDoItemList;


    private ToDoItemClickListener toDoItemClickListener = new ToDoItemClickListener() {
        @Override
        public void toDoItemClicked(View v, int position) {

            Log.d(TAG, ": To Do Item at pos: " + position + " clicked");

            // TODO: Setup ToDoItem click action

            /*
            String viewItemURLString = similarItemsList.get(position).getViewItemURLString();

            // might need try statement
            Uri page = Uri.parse(viewItemURLString);
            Intent intent = new Intent(Intent.ACTION_VIEW, page);
            startActivity(intent);
            */
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

        toDoItemList = toDoGroup.getToDoItemArrayList();
        toDoGroupId = toDoGroup.getId();

        // Setup RecyclerView
        toDoItemListRecyclerView = findViewById(R.id.view_to_do_item_list_recycler);
        toDoItemListRecyclerView.setHasFixedSize(true);

        toDoItemListLayoutManager = new LinearLayoutManager(this);
        toDoItemListRecyclerView.setLayoutManager(toDoItemListLayoutManager);

        toDoItemListAdapter = new ToDoItemListAdapter(toDoItemList, this, toDoItemClickListener);
        toDoItemListRecyclerView.setAdapter(toDoItemListAdapter);


        /*
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(toDoGroup.getTitle());
    }

    public void UpdateToDoItemListDisplay()
    {
        toDoItemListAdapter = new ToDoItemListAdapter(toDoItemList, this, toDoItemClickListener);
        toDoItemListRecyclerView.setAdapter(toDoItemListAdapter);

        toDoItemListAdapter.notifyDataSetChanged();
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
                showAddToDoItemDialog();

            case R.id.menu_main_user_settings:
                Log.d(TAG, ": Menu item selected: " + item.getItemId());

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addToDoItem(String itemName, int priority)
    {
        toDoGroup.addToDoItem(new ToDoItem(itemName, priority));
        toDoItemList = toDoGroup.getToDoItemArrayList();

        UpdateToDoItemListDisplay();
    }

    private Map<String, Object> buildToDoGroupDoc(ToDoGroup toDoGroup)
    {
        Map<String, Object> toDoGroupDoc = new HashMap<>();
        toDoGroupDoc.put("title", toDoGroup.getTitle());
        toDoGroupDoc.put("iconName", toDoGroup.getIconName());
        toDoGroupDoc.put("shared", toDoGroup.isShared());
        toDoGroupDoc.put("numPriorityOneItems", toDoGroup.getNumPriorityOneItems());
        toDoGroupDoc.put("totalItems", toDoGroup.getTotalItems());
        toDoGroupDoc.put("subscribers", Arrays.asList(toDoGroup.getSubscribers()));

        toDoGroupDoc.put("toDoItems", toDoGroup.getToDoItems());

        return toDoGroupDoc;
    }

    private void commitToDoGroup()
    {
        Map<String, Object> toDoGroupDoc = buildToDoGroupDoc(toDoGroup);

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

        Log.d(TAG, ": To Do Group " + toDoGroup.getTitle() + " updated");
        Toast.makeText(this, "To Do Group updated: " + toDoGroup.getTitle(), Toast.LENGTH_LONG).show();
    }

    public void showAddToDoItemDialog() {
        // Create an instance of the dialog fragment and show it
        FragmentManager fm = getSupportFragmentManager();
        AddToDoItemDialogFragment dialogFrag = new AddToDoItemDialogFragment();

        //dialogFrag.setTargetFragment(ToDoItemListActivity.this, 300);
        dialogFrag.show(fm, AddToDoItemDialogFragment.TAG);
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the AddToDoItemDialogFragment.AddToDoItemDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();

        EditText viewAddToDoItemName = dialogView.findViewById(R.id.dialog_add_to_do_item_name);
        String newItemName = viewAddToDoItemName.getText().toString();

        RadioGroup radioGroupPriority = dialogView.findViewById(R.id.dialog_add_to_do_item_radio_group);
        int radioPriorityCheckedId = radioGroupPriority.getCheckedRadioButtonId();
        int priority = processRadioPrioritySelection(radioPriorityCheckedId);

        addToDoItem(newItemName, priority);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Toast.makeText(this, "Add To Do Item Cancelled", Toast.LENGTH_SHORT).show();
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

    // TODO: commit ToDoGroup to cloud onPause()


    @Override
    protected void onPause() {
        super.onPause();

        commitToDoGroup();
    }
}
