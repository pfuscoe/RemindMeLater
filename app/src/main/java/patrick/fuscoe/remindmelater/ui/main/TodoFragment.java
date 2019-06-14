package patrick.fuscoe.remindmelater.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ToDoGroup;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToDoFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    public static final String TAG = "ToDoFragment";

    private RecyclerView toDoGroupsRecyclerView;
    private RecyclerView.Adapter toDoGroupsAdapter;
    private RecyclerView.LayoutManager toDoGroupsLayoutManager;

    private ToDoGroupsViewModel toDoGroupsViewModel;

    private List<ToDoGroup> toDoGroupList;


    private ToDoGroupClickListener toDoGroupClickListener = new ToDoGroupClickListener() {
        @Override
        public void toDoGroupClicked(View v, int position) {

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

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_to_do_groups, container, false);

        // Setup RecyclerView
        toDoGroupsRecyclerView = root.findViewById(R.id.view_to_do_groups_recycler);
        toDoGroupsRecyclerView.setHasFixedSize(true);

        toDoGroupsLayoutManager = new LinearLayoutManager(getContext());
        toDoGroupsRecyclerView.setLayoutManager(toDoGroupsLayoutManager);

        toDoGroupsAdapter = new ToDoGroupsAdapter(toDoGroupList, getContext(), toDoGroupClickListener);
        toDoGroupsRecyclerView.setAdapter(toDoGroupsAdapter);

        return root;
    }

    public void UpdateToDoGroupsDisplay()
    {
        toDoGroupsAdapter = new ToDoGroupsAdapter(toDoGroupList, getContext(), toDoGroupClickListener);
        toDoGroupsRecyclerView.setAdapter(toDoGroupsAdapter);

        toDoGroupsAdapter.notifyDataSetChanged();
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
                    UpdateToDoGroupsDisplay();
                }
            }
        });

    }
}