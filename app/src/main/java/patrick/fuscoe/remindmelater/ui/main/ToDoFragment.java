package patrick.fuscoe.remindmelater.ui.main;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.ToDoItem;
import patrick.fuscoe.remindmelater.ui.dialog.AddToDoGroupDialogFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToDoFragment extends Fragment implements AddToDoGroupDialogFragment.AddToDoGroupDialogListener {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoFragment";

    public static final String TO_DO_GROUP = "patrick.fuscoe.remindmelater.TO_DO_GROUP";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference toDoGroupsCollectionRef = db.collection("todogroups");

    private RecyclerView toDoGroupsRecyclerView;
    private RecyclerView.Adapter toDoGroupsAdapter;
    private RecyclerView.LayoutManager toDoGroupsLayoutManager;

    private ToDoGroupsViewModel toDoGroupsViewModel;

    private List<ToDoGroup> toDoGroupList;


    private ToDoGroupClickListener toDoGroupClickListener = new ToDoGroupClickListener() {
        @Override
        public void toDoGroupClicked(View v, int position) {

            Log.d(TAG, ": To Do Group " + position + " clicked");

            ToDoGroup toDoGroup = toDoGroupList.get(position);

            Intent intent = new Intent(getContext(), ToDoItemListActivity.class);

            Gson gson = new Gson();

            String toDoGroupString = gson.toJson(toDoGroup);
            intent.putExtra(TO_DO_GROUP, toDoGroupString);

            startActivity(intent);
        }
    };

    public interface ToDoGroupClickListener {
        void toDoGroupClicked(View v, int position);
    }


    public static ToDoFragment newInstance(int index) {
        ToDoFragment fragment = new ToDoFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toDoGroupList = new ArrayList<>();
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_to_do_groups, container, false);

        // Setup Toolbar
        setHasOptionsMenu(true);

        // Setup RecyclerView
        toDoGroupsRecyclerView = root.findViewById(R.id.view_to_do_groups_recycler);
        toDoGroupsRecyclerView.setHasFixedSize(true);

        toDoGroupsLayoutManager = new LinearLayoutManager(getContext());
        toDoGroupsRecyclerView.setLayoutManager(toDoGroupsLayoutManager);

        toDoGroupsAdapter = new ToDoGroupsAdapter(toDoGroupList, getContext(), toDoGroupClickListener);
        toDoGroupsRecyclerView.setAdapter(toDoGroupsAdapter);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        toDoGroupsViewModel = ViewModelProviders.of(this).get(ToDoGroupsViewModel.class);

        LiveData<QuerySnapshot> liveData = toDoGroupsViewModel.getQuerySnapshotLiveData();

        liveData.observe(getViewLifecycleOwner(), new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null)
                {
                    // Update UI views with values from the snapshot
                    // This probably means converting data from snapshot form
                    // into list of ToDoGroups then update data display

                    List<ToDoGroup> toDoGroupDocs = new ArrayList<>();

                    //for (QueryDocumentSnapshot doc : queryDocumentSnapshots)
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments())
                    {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        String iconName = doc.getString("iconName");
                        //if (doc.getBoolean("shared") != null)
                        boolean shared = doc.getBoolean("shared");
                        int numPriorityOneItems = doc.get("numPriorityOneItems", int.class);
                        int totalItems = doc.get("totalItems", int.class);

                        ArrayList<String> subscribersList = (ArrayList<String>) doc.get("subscribers");

                        String[] subscribers = subscribersList.toArray(new String[0]);

                        Map<String, Object> toDoItems = (Map<String, Object>) doc.get("toDoItems");

                        ToDoGroup toDoGroup = new ToDoGroup(id, title, iconName, shared, numPriorityOneItems, subscribers, toDoItems);
                        toDoGroupDocs.add(toDoGroup);
                    }

                    toDoGroupList = toDoGroupDocs;

                    Log.d(TAG, ": toDoGroupList size: " + toDoGroupList.size());
                    UpdateToDoGroupsDisplay();
                }
            }
        });
    }

    public void UpdateToDoGroupsDisplay()
    {
        toDoGroupsAdapter = new ToDoGroupsAdapter(toDoGroupList, getContext(), toDoGroupClickListener);
        toDoGroupsRecyclerView.setAdapter(toDoGroupsAdapter);

        toDoGroupsAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                Log.d(TAG, ": Add Button pressed");
                showAddToDoGroupDialog();

            case R.id.menu_main_user_settings:
                Log.d(TAG, ": Menu item selected: " + item.getItemId());

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addToDoGroup(String title)
    {
        DocumentReference docRef = toDoGroupsCollectionRef.document();
        String docId = docRef.getId();

        final ToDoGroup toDoGroup = new ToDoGroup(docId, title, "default", false, auth.getUid());

        Map<String, Object> toDoGroupDoc = buildToDoGroupDoc(toDoGroup);

        toDoGroupsCollectionRef.document(docId)
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

        Log.d(TAG, ": To Do Group " + title + " added");
        Toast.makeText(getContext(), "To Do Group added: " + title, Toast.LENGTH_LONG).show();
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

    // TODO: Override onPause() to write data to cloud


    public void showAddToDoGroupDialog() {
        // Create an instance of the dialog fragment and show it
        FragmentManager fm = getFragmentManager();
        AddToDoGroupDialogFragment dialogFrag = new AddToDoGroupDialogFragment();

        dialogFrag.setTargetFragment(ToDoFragment.this, 300);
        //dialogFrag.show(getChildFragmentManager(), AddToDoGroupDialogFragment.TAG);
        dialogFrag.show(fm, AddToDoGroupDialogFragment.TAG);
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the AddToDoGroupDialogFragment.AddToDoGroupDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        Dialog dialogView = dialog.getDialog();
        EditText viewAddToDoGroupTitle = dialogView.findViewById(R.id.dialog_add_to_do_group_title);
        String newTitle = viewAddToDoGroupTitle.getText().toString();
        addToDoGroup(newTitle);
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        Toast.makeText(getContext(), "Add To Do Group Cancelled", Toast.LENGTH_SHORT).show();
    }

}