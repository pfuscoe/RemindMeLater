package patrick.fuscoe.remindmelater.ui.main;

import android.app.Dialog;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.AddCategoryDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.DeleteToDoGroupDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.EditToDoGroupDialogFragment;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;

/**
 * Manages UI for To Do Tab. Listens to FireStore for updates to subscribed To Do Lists and
 * changes in user profile.
 *
 * Also handles Adding, Editing, Deleting and Reordering new To Do Lists.
*/
public class ToDoFragment extends Fragment implements AddCategoryDialogFragment.AddCategoryDialogListener,
        EditToDoGroupDialogFragment.EditToDoGroupDialogListener,
        DeleteToDoGroupDialogFragment.DeleteToDoGroupDialogListener {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoFragment";
    public static final String TO_DO_GROUP = "patrick.fuscoe.remindmelater.TO_DO_GROUP";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference toDoGroupsCollectionRef = db.collection("todogroups");

    private RecyclerView toDoGroupsRecyclerView;
    private RecyclerView.Adapter toDoGroupsAdapter;
    private RecyclerView.LayoutManager toDoGroupsLayoutManager;
    private FrameLayout viewToDoGroupsTips;
    private WebView viewToDoGroupsTipsWebview;

    private ToDoGroupsViewModel toDoGroupsViewModel;
    private UserProfileViewModel userProfileViewModel;

    private UserProfile userProfile;
    private List<ToDoGroup> toDoGroupList;

    private ToDoGroup toDoGroupToEdit;
    private ToDoGroup toDoGroupToDelete;

    private MenuItem tipsMenuItem;

    private boolean editMode;
    private boolean reorderMode;
    private boolean userFieldChanged;
    private boolean groupAdded;
    private boolean isTipsOn;

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
        isTipsOn = false;
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_to_do_groups, container, false);

        viewToDoGroupsTips = root.findViewById(R.id.view_to_do_groups_tips);
        viewToDoGroupsTipsWebview = root.findViewById(R.id.view_to_do_groups_tips_webview);
        viewToDoGroupsTipsWebview.loadUrl("file:///android_asset/tips_to_do_groups.html");

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
                // Do nothing
            }
        };

        toDoGroupReorderItemTouchHelper = new ItemTouchHelper(callback);
        toDoGroupReorderItemTouchHelper.attachToRecyclerView(toDoGroupsRecyclerView);

        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Updates UI when To Do Groups are changed in FireStore
        toDoGroupsViewModel = ViewModelProviders.of(this).get(ToDoGroupsViewModel.class);
        LiveData<QuerySnapshot> toDoGroupsLiveData = toDoGroupsViewModel.getQuerySnapshotLiveData();

        toDoGroupsLiveData.observe(getViewLifecycleOwner(), new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null)
                {
                    List<ToDoGroup> toDoGroupDocs = new ArrayList<>();

                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments())
                    {
                        ToDoGroup toDoGroup = FirebaseDocUtils.createToDoGroupObj(documentSnapshot);

                        toDoGroupDocs.add(toDoGroup);
                    }

                    toDoGroupList = toDoGroupDocs;

                    if (tipsMenuItem != null)
                    {
                        if (toDoGroupList.isEmpty() && !isTipsOn)
                        {
                            toggleTips();
                        }

                        if (!toDoGroupList.isEmpty() && isTipsOn)
                        {
                            toggleTips();
                        }
                    }
                    else
                    {
                        if (toDoGroupList.isEmpty() && !isTipsOn)
                        {
                            toggleTipsNoMenu();
                        }

                        if (!toDoGroupList.isEmpty() && isTipsOn)
                        {
                            toggleTipsNoMenu();
                        }
                    }

                    updateToDoGroupDisplayOnReorder();
                    UpdateToDoGroupsDisplay();
                }
            }
        });

        // Updates UI when user profile is changed in FireStore
        userProfileViewModel = ViewModelProviders.of(this).get(UserProfileViewModel.class);
        LiveData<DocumentSnapshot> userProfileLiveData = userProfileViewModel.getDocumentSnapshotLiveData();

        userProfileLiveData.observe(getViewLifecycleOwner(), new Observer<DocumentSnapshot>() {
            @Override
            public void onChanged(@Nullable DocumentSnapshot documentSnapshot) {
                if (documentSnapshot != null)
                {
                    userProfile = FirebaseDocUtils.createUserProfileObj(documentSnapshot);

                    Log.d(TAG, "UserProfile loaded");

                    ((MainActivity) getActivity()).setActionBarTitle("Hello, " + userProfile.getDisplayName() + "!");

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_main, menu);

        menu.removeItem(R.id.menu_main_edit_reminder_categories);

        MenuItem viewIconEdit = menu.findItem(R.id.menu_main_edit);

        if (editMode)
        {
            viewIconEdit.setIcon(R.drawable.ic_menu_list);
        }
        else
        {
            viewIconEdit.setIcon(R.drawable.ic_menu_edit);
        }

        tipsMenuItem = menu.findItem(R.id.menu_main_tips);

        if (isTipsOn)
        {
            tipsMenuItem.setTitle(R.string.hide_tips);
        }
        else
        {
            tipsMenuItem.setTitle(R.string.show_tips);
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
                if (reorderMode)
                {
                    exitReorderMode();
                }
                else
                {
                    enterReorderMode();
                }
                return true;

            case R.id.menu_main_tips:
                toggleTips();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addToDoGroup(String title, String selectedIconName)
    {
        DocumentReference docRef = toDoGroupsCollectionRef.document();
        final String docId = docRef.getId();
        final ToDoGroup toDoGroup = new ToDoGroup(docId, title, selectedIconName, false,
                MainActivity.auth.getUid());

        Map<String, Object> toDoGroupDoc = FirebaseDocUtils.createToDoGroupDoc(toDoGroup);

        userProfile.addSubscription(docId);
        Map<String, Object> userProfileDoc = FirebaseDocUtils.createUserProfileDoc(userProfile);

        commitAddToDoGroupBatch(docId, toDoGroupDoc, userProfileDoc);

        Log.d(TAG, ": To Do Group " + title + " added");
        Toast.makeText(getContext(), "To Do List Added: " + title, Toast.LENGTH_LONG).show();
    }

    private void editToDoGroup(ToDoGroup toDoGroup)
    {
        String docId = toDoGroup.getId();
        final String groupTitle = toDoGroup.getTitle();

        Map<String, Object> toDoGroupDoc = FirebaseDocUtils.createToDoGroupDoc(toDoGroup);

        toDoGroupsCollectionRef.document(docId)
                .set(toDoGroupDoc)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Toast.makeText(getContext(), groupTitle + " Updated", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        Toast.makeText(getContext(), "Failed to save changes for: " +
                                groupTitle + ". " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        Log.d(TAG, ": To Do Group " + toDoGroup.getTitle() + " saved");
    }

    private void deleteToDoGroup(ToDoGroup toDoGroup)
    {
        final String docId = toDoGroup.getId();
        String groupTitle = toDoGroup.getTitle();

        userProfile.removeSubscription(docId);
        Map<String, Object> userProfileDoc = FirebaseDocUtils.createUserProfileDoc(userProfile);

        commitDeleteToDoGroupBatch(docId, userProfileDoc);

        Log.d(TAG, ": To Do Group " + groupTitle + " deleted");
        Toast.makeText(getContext(), "To Do List Deleted: " + groupTitle, Toast.LENGTH_LONG).show();
    }

    private void commitUserDoc(Map<String, Object> userProfileDoc)
    {
        MainActivity.userDocRef.set(userProfileDoc)
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
        batch.set(MainActivity.userDocRef, userProfileDoc);

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
        batch.set(MainActivity.userDocRef, userProfileDoc);

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
            Map<String, Object> userProfileDoc = FirebaseDocUtils.createUserProfileDoc(userProfile);
            commitUserDoc(userProfileDoc);
            userFieldChanged = false;
        }

        super.onPause();
    }

    public void showAddToDoGroupDialog()
    {
        AddCategoryDialogFragment dialogFrag = new AddCategoryDialogFragment();

        dialogFrag.setTargetFragment(ToDoFragment.this, 300);
        dialogFrag.show(getFragmentManager(), AddCategoryDialogFragment.TAG);
    }

    public void showEditToDoGroupDialog()
    {
        EditToDoGroupDialogFragment dialogFragment = new EditToDoGroupDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", toDoGroupToEdit.getTitle());
        bundle.putString("iconName", toDoGroupToEdit.getIconName());
        Log.d(TAG, "called showEditToDoGroupDialog");

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

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {
        if (dialog instanceof AddCategoryDialogFragment)
        {
            Dialog dialogView = dialog.getDialog();
            EditText viewCategoryEditName = dialogView.findViewById(R.id.dialog_category_edit_name);
            String newTitle = viewCategoryEditName.getText().toString();

            if (newTitle.equals(""))
            {
                Toast.makeText(getContext(), "Add To Do List Failed: List Name Must Not Be Blank", Toast.LENGTH_LONG).show();
                return;
            }

            String selectedIconName = ((AddCategoryDialogFragment) dialog).getSelectedIconName();
            if (selectedIconName.equals("default"))
            {
                addToDoGroup(newTitle, MainActivity.DEFAULT_TO_DO_GROUP_CATEGORY_ICON_NAME);
            }
            else
            {
                addToDoGroup(newTitle, selectedIconName);
            }
        }
        else if (dialog instanceof EditToDoGroupDialogFragment)
        {
            Dialog dialogView = dialog.getDialog();
            EditText viewEditToDoGroupTitle = dialogView.findViewById(R.id.dialog_category_edit_name);
            String newTitle = viewEditToDoGroupTitle.getText().toString();

            if (newTitle.equals(""))
            {
                Toast.makeText(getContext(), "Edit To Do List Failed: List Name Must Not Be Blank", Toast.LENGTH_LONG).show();
                return;
            }

            String selectedIconName = ((EditToDoGroupDialogFragment) dialog).getSelectedIconName();
            toDoGroupToEdit.setTitle(newTitle);
            toDoGroupToEdit.setIconName(selectedIconName);

            editToDoGroup(toDoGroupToEdit);
        }
        else if (dialog instanceof DeleteToDoGroupDialogFragment)
        {
            deleteToDoGroup(toDoGroupToDelete);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {
        if (dialog instanceof AddCategoryDialogFragment)
        {
            Toast.makeText(getContext(), "Add To Do List Cancelled", Toast.LENGTH_SHORT).show();
        }
        else if (dialog instanceof EditToDoGroupDialogFragment)
        {
            Toast.makeText(getContext(), "Edit To Do List Cancelled", Toast.LENGTH_SHORT).show();
        }
        else if (dialog instanceof DeleteToDoGroupDialogFragment)
        {
            Toast.makeText(getContext(), "Delete To Do List Cancelled", Toast.LENGTH_SHORT).show();
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

    private void enterReorderMode()
    {
        editMode = true;
        reorderMode = true;

        requireActivity().invalidateOptionsMenu();

        UpdateToDoGroupsDisplay();
    }

    private void exitReorderMode()
    {
        editMode = false;
        reorderMode = false;

        requireActivity().invalidateOptionsMenu();

        UpdateToDoGroupsDisplay();
    }

    private void toggleTips()
    {
        if (isTipsOn)
        {
            viewToDoGroupsTips.setVisibility(View.INVISIBLE);
            toDoGroupsRecyclerView.setVisibility(View.VISIBLE);
            tipsMenuItem.setTitle(R.string.show_tips);
            isTipsOn = false;
        }
        else
        {
            viewToDoGroupsTips.setVisibility(View.VISIBLE);
            toDoGroupsRecyclerView.setVisibility(View.INVISIBLE);
            tipsMenuItem.setTitle(R.string.hide_tips);
            isTipsOn = true;
        }
    }

    private void toggleTipsNoMenu()
    {
        if (isTipsOn)
        {
            viewToDoGroupsTips.setVisibility(View.INVISIBLE);
            toDoGroupsRecyclerView.setVisibility(View.VISIBLE);
            isTipsOn = false;
        }
        else
        {
            viewToDoGroupsTips.setVisibility(View.VISIBLE);
            toDoGroupsRecyclerView.setVisibility(View.INVISIBLE);
            isTipsOn = true;
        }
    }

}