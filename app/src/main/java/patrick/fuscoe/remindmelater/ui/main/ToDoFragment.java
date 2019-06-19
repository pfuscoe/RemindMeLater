package patrick.fuscoe.remindmelater.ui.main;

import android.app.Dialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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

import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.ui.dialog.AddToDoGroupDialogFragment;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToDoFragment extends Fragment implements AddToDoGroupDialogFragment.AddToDoGroupDialogListener {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoFragment";

    private RecyclerView toDoGroupsRecyclerView;
    private RecyclerView.Adapter toDoGroupsAdapter;
    private RecyclerView.LayoutManager toDoGroupsLayoutManager;

    private ToDoGroupsViewModel toDoGroupsViewModel;

    private List<ToDoGroup> toDoGroupList;


    private ToDoGroupClickListener toDoGroupClickListener = new ToDoGroupClickListener() {
        @Override
        public void toDoGroupClicked(View v, int position) {

            Log.d(TAG, "- To Do Group " + position + " clicked");

            // TODO: Setup ToDoGroup click action

            /*
            String viewItemURLString = similarItemsList.get(position).getViewItemURLString();

            // might need try statement
            Uri page = Uri.parse(viewItemURLString);
            Intent intent = new Intent(Intent.ACTION_VIEW, page);
            startActivity(intent);
            */
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

                    toDoGroupList = queryDocumentSnapshots.toObjects(ToDoGroup.class);
                    Log.d(TAG, "- toDoGroupList size: " + toDoGroupList.size());
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
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                Log.d(TAG, "- Add Button pressed");
                showAddToDoGroupDialog();

            case R.id.menu_main_user_settings:
                Log.d(TAG, "- Menu item selected: " + item.getItemId());

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addToDoGroup(String title)
    {
        Log.d(TAG, "- Add Button pressed");


    }

    // TODO: Override onPause() to write data to cloud


    public void showAddToDoGroupDialog() {
        // Create an instance of the dialog fragment and show it
        DialogFragment dialog = new AddToDoGroupDialogFragment();
        dialog.show(getChildFragmentManager(), AddToDoGroupDialogFragment.TAG);
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