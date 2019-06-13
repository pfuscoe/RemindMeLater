package patrick.fuscoe.remindmelater.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.QuerySnapshot;

import patrick.fuscoe.remindmelater.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ToDoFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    private ToDoGroupsViewModel toDoGroupsViewModel;

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
        View root = inflater.inflate(R.layout.fragment_main, container, false);
        final TextView textView = root.findViewById(R.id.section_label);

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
                    // TODO: Update UI views with values from the snapshot

                }
            }
        });

    }
}