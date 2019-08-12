package patrick.fuscoe.remindmelater.ui.main;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.ToDoItem;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.AddToDoGroupDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.DeleteToDoGroupDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.EditToDoGroupDialogFragment;

public class ToDoFragment extends Fragment implements AddToDoGroupDialogFragment.AddToDoGroupDialogListener,
        EditToDoGroupDialogFragment.EditToDoGroupDialogListener,
        DeleteToDoGroupDialogFragment.DeleteToDoGroupDialogListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoFragment";
    public static final String TO_DO_GROUP = "patrick.fuscoe.remindmelater.TO_DO_GROUP";

    public static final int DEFAULT_CATEGORY_ICON = R.drawable.category_note;

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference toDoGroupsCollectionRef = db.collection("todogroups");

    private final String userId = auth.getUid();
    private final DocumentReference userDocRef = db.collection("users").document(userId);

    private RecyclerView toDoGroupsRecyclerView;
    private RecyclerView.Adapter toDoGroupsAdapter;
    private RecyclerView.LayoutManager toDoGroupsLayoutManager;

    private ToDoGroupsViewModel toDoGroupsViewModel;
    private UserProfileViewModel userProfileViewModel;

    private UserProfile userProfile;
    private List<ToDoGroup> toDoGroupList;

    private ToDoGroup toDoGroupToEdit;
    private ToDoGroup toDoGroupToDelete;

    private boolean editMode;
    private boolean reorderMode;
    private boolean userFieldChanged;
    private boolean groupAdded;

    private ItemTouchHelper toDoGroupReorderItemTouchHelper;


    private ToDoGroupClickListener toDoGroupClickListener = new ToDoGroupClickListener() {
        @Override
        public void toDoGroupClicked(View v, int position) {

            Log.d(TAG, ": To Do Group " + position + " clicked");

            ToDoGroup toDoGroup = toDoGroupList.get(position);

            if (!editMode)
            {
                Intent intent = new Intent(getContext(), ToDoItemListActivity.class);
                Gson gson = new Gson();
                String toDoGroupString = gson.toJson(toDoGroup);
                intent.putExtra(TO_DO_GROUP, toDoGroupString);
                startActivity(intent);
            }
            // User clicked pencil icon from editMode
            else if (v.getId() == R.id.view_card_category_num_circle)
            {
                toDoGroupToEdit = toDoGroupList.get(position);
                showEditToDoGroupDialog();
            }
            // User clicked delete icon from editMode
            else if (v.getId() == R.id.view_card_category_num_box)
            {
                toDoGroupToDelete = toDoGroupList.get(position);
                showDeleteToDoGroupDialog();
            }
        }
    };

    private ToDoGroupDragListener toDoGroupDragListener = new ToDoGroupDragListener() {
        @Override
        public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
            toDoGroupReorderItemTouchHelper.startDrag(viewHolder);
        }
    };

    public interface ToDoGroupClickListener {
        void toDoGroupClicked(View v, int position);
    }

    public interface ToDoGroupDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
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

        editMode = false;
        reorderMode = false;
        userFieldChanged = false;
        groupAdded = false;
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

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder dragged, @NonNull RecyclerView.ViewHolder target) {
                int fromPosition = dragged.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                Collections.swap(toDoGroupList, fromPosition, toPosition);
                toDoGroupsAdapter.notifyItemMoved(fromPosition, toPosition);

                // Check order and set flag if changed
                checkToDoGroupOrder();

                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {

            }
        };

        toDoGroupReorderItemTouchHelper = new ItemTouchHelper(callback);
        toDoGroupReorderItemTouchHelper.attachToRecyclerView(toDoGroupsRecyclerView);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        toDoGroupsViewModel = ViewModelProviders.of(this).get(ToDoGroupsViewModel.class);
        LiveData<QuerySnapshot> toDoGroupsLiveData = toDoGroupsViewModel.getQuerySnapshotLiveData();

        toDoGroupsLiveData.observe(getViewLifecycleOwner(), new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null)
                {
                    // Update UI views with values from the snapshot
                    // Convert data from snapshot form
                    // into list of ToDoGroups then update data display

                    List<ToDoGroup> toDoGroupDocs = new ArrayList<>();

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments())
                    {
                        String id = doc.getId();
                        String title = doc.getString("title");
                        //String iconName = doc.getString("iconName");
                        int iconId = Math.toIntExact(doc.getLong("iconId"));
                        boolean shared = doc.getBoolean("shared");
                        int numPriorityOneItems = doc.get("numPriorityOneItems", int.class);
                        int totalItems = doc.get("totalItems", int.class);

                        ArrayList<String> subscribersList = (ArrayList<String>) doc.get("subscribers");

                        String[] subscribers = subscribersList.toArray(new String[0]);

                        Map<String, Object> toDoItems = (Map<String, Object>) doc.get("toDoItems");

                        ToDoGroup toDoGroup = new ToDoGroup(id, title, iconId, shared, numPriorityOneItems, subscribers, toDoItems);
                        toDoGroupDocs.add(toDoGroup);
                    }

                    toDoGroupList = toDoGroupDocs;

                    Log.d(TAG, ": toDoGroupList size: " + toDoGroupList.size());
                    updateToDoGroupDisplayOnReorder();
                    UpdateToDoGroupsDisplay();
                }
            }
        });

        userProfileViewModel = ViewModelProviders.of(this).get(UserProfileViewModel.class);
        LiveData<DocumentSnapshot> userProfileLiveData = userProfileViewModel.getDocumentSnapshotLiveData();

        userProfileLiveData.observe(getViewLifecycleOwner(), new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null)
                {
                    Map<String, Object> docMap = documentSnapshot.getData();

                    String id = documentSnapshot.getId();
                    String displayName = documentSnapshot.getString("displayName");

                    ArrayList<String> subscriptionsList = (ArrayList<String>) docMap.get("subscriptions");

                    Log.d(TAG, "subscriptionsList: " + subscriptionsList);

                    String[] subscriptions = new String[subscriptionsList.size()];
                    subscriptions = subscriptionsList.toArray(subscriptions);

                    for (int i = 0; i < subscriptions.length; i++) {
                        Log.d("subscriptions item: ", subscriptions[i]);
                    }

                    Map<String, Integer> reminderCategories =
                            (Map<String, Integer>) documentSnapshot.get("reminderCategories");

                    userProfile = new UserProfile(id, displayName, subscriptions, reminderCategories);

                    Log.d(TAG, "UserProfile loaded");
                    ((MainActivity) getActivity()).setActionBarTitle("Hello, " + userProfile.getDisplayName());

                    updateToDoGroupDisplayOnReorder();
                    UpdateToDoGroupsDisplay();
                }
            }
        });
    }

    public void checkToDoGroupOrder()
    {
        String[] subscriptions = userProfile.getSubscriptions();

        for (int i = 0; i < toDoGroupList.size(); i++)
        {
            ToDoGroup toDoGroup = toDoGroupList.get(i);

            if (!toDoGroup.getId().equals(subscriptions[i]))
            {
                userFieldChanged = true;
            }
        }

        if (userFieldChanged)
        {
            updateToDoGroupOrder();
        }
    }

    public void updateToDoGroupOrder()
    {
        int numGroups = toDoGroupList.size();
        String[] newSubscriptionsArray = new String[numGroups];

        for (int i = 0; i < numGroups; i++)
        {
            ToDoGroup toDoGroup = toDoGroupList.get(i);

            newSubscriptionsArray[i] = toDoGroup.getId();
        }

        userProfile.setSubscriptions(newSubscriptionsArray);
    }

    public void updateToDoGroupDisplayOnReorder()
    {
        if (userProfile != null)
        {
            List<ToDoGroup> tempList = new ArrayList<>(toDoGroupList);
            List<ToDoGroup> reorderedList = new ArrayList<>();
            String[] subscriptions = userProfile.getSubscriptions();

            for (int i = 0; i < subscriptions.length; i++)
            {
                String id = subscriptions[i];

                for (ToDoGroup group : tempList)
                {
                    if (id.equals(group.getId()))
                    {
                        reorderedList.add(group);
                    }
                }
            }

            toDoGroupList = reorderedList;
        }
    }

    public void UpdateToDoGroupsDisplay()
    {
        if (reorderMode)
        {
            toDoGroupsAdapter = new ToDoGroupsReorderAdapter(toDoGroupList, getContext(), toDoGroupDragListener);
            toDoGroupsRecyclerView.setAdapter(toDoGroupsAdapter);
        }
        else if (editMode)
        {
            toDoGroupsAdapter = new ToDoGroupsEditAdapter(toDoGroupList, getContext(), toDoGroupClickListener);
            toDoGroupsRecyclerView.setAdapter(toDoGroupsAdapter);
        }
        else
        {
            toDoGroupsAdapter = new ToDoGroupsAdapter(toDoGroupList, getContext(), toDoGroupClickListener);
            toDoGroupsRecyclerView.setAdapter(toDoGroupsAdapter);
        }

        toDoGroupsAdapter.notifyDataSetChanged();
    }

    private void UpdateUserProfileDisplay()
    {
        
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem viewIconEdit = menu.findItem(R.id.menu_main_edit);

        if (editMode)
        {
            viewIconEdit.setIcon(R.drawable.ic_menu_list);
        }
        else
        {
            viewIconEdit.setIcon(R.drawable.ic_menu_edit);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                Log.d(TAG, ": Add Button pressed");
                showAddToDoGroupDialog();
                return true;

            case R.id.menu_main_edit:
                Log.d(TAG, ": Edit Button pressed");
                toggleEditMode();
                return true;

            case R.id.menu_main_user_settings:
                Log.d(TAG, ": Menu item selected: " + item.getItemId());
                return true;

            case R.id.menu_main_reorder:
                Log.d(TAG, ": Reorder menu item selected");
                enterReorderMode();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addToDoGroup(String title)
    {
        //groupAdded = true;

        DocumentReference docRef = toDoGroupsCollectionRef.document();
        final String docId = docRef.getId();
        final ToDoGroup toDoGroup = new ToDoGroup(docId, title, DEFAULT_CATEGORY_ICON, false, auth.getUid());

        Map<String, Object> toDoGroupDoc = buildToDoGroupDoc(toDoGroup);
        userProfile.addSubscription(docId);
        Map<String, Object> userProfileDoc = buildUserDoc(userProfile);

        commitAddToDoGroupBatch(docId, toDoGroupDoc, userProfileDoc);

        /*
        toDoGroupsCollectionRef.document(docId)
                .set(toDoGroupDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        userProfile.addSubscription(docId);
                        userFieldChanged = true;
                        groupAdded = false;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
        */

        Log.d(TAG, ": To Do Group " + title + " added");
        Toast.makeText(getContext(), "To Do Group Added: " + title, Toast.LENGTH_LONG).show();
    }

    private void editToDoGroup(ToDoGroup toDoGroup)
    {
        String docId = toDoGroup.getId();

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

        Log.d(TAG, ": To Do Group " + toDoGroup.getTitle() + " saved");
        Toast.makeText(getContext(), "To Do Group Saved: " + toDoGroup.getTitle(), Toast.LENGTH_SHORT).show();
    }

    private void deleteToDoGroup(ToDoGroup toDoGroup)
    {
        final String docId = toDoGroup.getId();
        String groupTitle = toDoGroup.getTitle();

        userProfile.removeSubscription(docId);
        Map<String, Object> userProfileDoc = buildUserDoc(userProfile);

        commitDeleteToDoGroupBatch(docId, userProfileDoc);

        /*
        toDoGroupsCollectionRef.document(docId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully deleted!");
                        userProfile.removeSubscription(docId);
                        userFieldChanged = true;

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error deleting document", e);
                    }
                });
        */

        Log.d(TAG, ": To Do Group " + groupTitle + " deleted");
        Toast.makeText(getContext(), "To Do Group Deleted: " + groupTitle, Toast.LENGTH_LONG).show();
    }

    private Map<String, Object> buildToDoGroupDoc(ToDoGroup toDoGroup)
    {
        Map<String, Object> toDoGroupDoc = new HashMap<>();
        toDoGroupDoc.put("title", toDoGroup.getTitle());
        toDoGroupDoc.put("iconId", toDoGroup.getIconId());
        toDoGroupDoc.put("shared", toDoGroup.isShared());
        toDoGroupDoc.put("numPriorityOneItems", toDoGroup.getNumPriorityOneItems());
        toDoGroupDoc.put("totalItems", toDoGroup.getTotalItems());
        toDoGroupDoc.put("subscribers", Arrays.asList(toDoGroup.getSubscribers()));

        toDoGroupDoc.put("toDoItems", toDoGroup.getToDoItems());

        return toDoGroupDoc;
    }

    private Map<String, Object> buildUserDoc(UserProfile userProfile)
    {
        Map<String, Object> userProfileDoc = new HashMap<>();
        userProfileDoc.put("displayName", userProfile.getDisplayName());
        userProfileDoc.put("subscriptions", Arrays.asList(userProfile.getSubscriptions()));
        userProfileDoc.put("reminderCategories", userProfile.getReminderCategories());

        return userProfileDoc;
    }

    private void commitUserDoc(Map<String, Object> userProfileDoc)
    {
        userDocRef.set(userProfileDoc)
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

        Log.d(TAG, userProfile.getDisplayName() + " User Profile Updated");
    }

    private void commitAddToDoGroupBatch(String groupId, Map<String, Object> toDoGroupDoc, Map<String, Object> userProfileDoc)
    {
        WriteBatch batch = db.batch();
        DocumentReference groupRef = toDoGroupsCollectionRef.document(groupId);

        batch.set(groupRef, toDoGroupDoc);
        batch.set(userDocRef, userProfileDoc);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Batch successfully written!");
            }
        });
    }

    private void commitDeleteToDoGroupBatch(String groupId, Map<String, Object> userProfileDoc)
    {
        WriteBatch batch = db.batch();
        DocumentReference groupRef = toDoGroupsCollectionRef.document(groupId);

        batch.delete(groupRef);
        batch.set(userDocRef, userProfileDoc);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Log.d(TAG, "Delete To Do Group Batch successfully finished!");
            }
        });
    }

    @Override
    public void onPause() {
        if (userFieldChanged)
        {
            commitUserDoc(buildUserDoc(userProfile));
            userFieldChanged = false;
        }

        super.onPause();
    }

    public void showAddToDoGroupDialog()
    {
        // Create an instance of the dialog fragment and show it
        FragmentManager fm = getFragmentManager();
        AddToDoGroupDialogFragment dialogFrag = new AddToDoGroupDialogFragment();

        dialogFrag.setTargetFragment(ToDoFragment.this, 300);
        //dialogFrag.show(getChildFragmentManager(), AddToDoGroupDialogFragment.TAG);
        dialogFrag.show(fm, AddToDoGroupDialogFragment.TAG);
    }

    public void showEditToDoGroupDialog()
    {
        EditToDoGroupDialogFragment dialogFragment = new EditToDoGroupDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", toDoGroupToEdit.getTitle());
        bundle.putString("iconName", toDoGroupToEdit.getIconName());

        dialogFragment.setArguments(bundle);
        dialogFragment.setTargetFragment(ToDoFragment.this, 300);
        dialogFragment.show(getFragmentManager(), EditToDoGroupDialogFragment.TAG);
    }

    public void showDeleteToDoGroupDialog()
    {
        DeleteToDoGroupDialogFragment dialogFragment = new DeleteToDoGroupDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", toDoGroupToDelete.getTitle());

        dialogFragment.setArguments(bundle);
        dialogFragment.setTargetFragment(ToDoFragment.this, 300);
        dialogFragment.show(getFragmentManager(), DeleteToDoGroupDialogFragment.TAG);
    }

    // The dialog fragment receives a reference to this Activity through the
    // Fragment.onAttach() callback, which it uses to call the following methods
    // defined by the AddToDoGroupDialogFragment.AddToDoGroupDialogListener interface
    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof AddToDoGroupDialogFragment)
        {
            Dialog dialogView = dialog.getDialog();
            EditText viewAddToDoGroupTitle = dialogView.findViewById(R.id.dialog_add_to_do_group_title);
            String newTitle = viewAddToDoGroupTitle.getText().toString();
            addToDoGroup(newTitle);
        }
        else if (dialog instanceof EditToDoGroupDialogFragment)
        {
            Dialog dialogView = dialog.getDialog();
            EditText viewEditToDoGroupTitle = dialogView.findViewById(R.id.dialog_add_to_do_group_title);
            String newTitle = viewEditToDoGroupTitle.getText().toString();
            toDoGroupToEdit.setTitle(newTitle);

            editToDoGroup(toDoGroupToEdit);
        }
        else if (dialog instanceof DeleteToDoGroupDialogFragment)
        {
            deleteToDoGroup(toDoGroupToDelete);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof AddToDoGroupDialogFragment)
        {
            Toast.makeText(getContext(), "Add To Do Group Cancelled", Toast.LENGTH_SHORT).show();
        }
        else if (dialog instanceof EditToDoGroupDialogFragment)
        {
            Toast.makeText(getContext(), "Edit To Do Group Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    public void toggleEditMode()
    {
        if (editMode)
        {
            reorderMode = false;
            editMode = false;
        }
        else
        {
            editMode = true;
        }

        requireActivity().invalidateOptionsMenu();

        UpdateToDoGroupsDisplay();
    }

    public void enterReorderMode()
    {
        editMode = true;
        reorderMode = true;

        requireActivity().invalidateOptionsMenu();

        UpdateToDoGroupsDisplay();
    }

}