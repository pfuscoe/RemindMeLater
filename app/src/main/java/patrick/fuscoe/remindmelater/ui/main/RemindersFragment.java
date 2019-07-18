package patrick.fuscoe.remindmelater.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;

public class RemindersFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = "patrick.fuscoe.remindmelater.RemindersFragment";
    public static final String REMINDERS = "patrick.fuscoe.remindmelater.REMINDERS";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    private final String userId = auth.getUid();
    private final DocumentReference userDocRef = db.collection("users").document(userId);

    private RecyclerView remindersRecyclerView;
    private RecyclerView.Adapter remindersAdapter;
    private RecyclerView.LayoutManager remindersLayoutManager;

    private RemindersViewModel remindersViewModel;
    private UserProfileViewModel userProfileViewModel;

    private UserProfile userProfile;
    private List<ReminderItem> reminderItemList;

    private boolean editMode;


    private ReminderClickListener reminderClickListener = new ReminderClickListener() {
        @Override
        public void reminderClicked(View v, int position) {
            // TODO: implement reminder click action
        }
    };

    public interface ReminderClickListener {
        void reminderClicked(View v, int position);
    }

    public static RemindersFragment newInstance(int index) {
        RemindersFragment fragment = new RemindersFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
        */
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reminders, container, false);

        /*
        final TextView textView = root.findViewById(R.id.section_label);
        pageViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        */

        setHasOptionsMenu(true);

        // TODO: setup recycler view
        remindersRecyclerView = root.findViewById(R.id.view_reminders_recycler);
        remindersRecyclerView.setHasFixedSize(true);

        remindersLayoutManager = new LinearLayoutManager(getContext());
        remindersRecyclerView.setLayoutManager(remindersLayoutManager);



        return root;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        remindersViewModel = ViewModelProviders.of(this).get(RemindersViewModel.class);
        LiveData<QuerySnapshot> remindersLiveData = remindersViewModel.getQuerySnapshotLiveData();

        remindersLiveData.observe(getViewLifecycleOwner(), new Observer<QuerySnapshot>() {
            @Override
            public void onChanged(@Nullable QuerySnapshot queryDocumentSnapshots) {
                if (queryDocumentSnapshots != null)
                {
                    // TODO: Update UI views with data from snapshot
                }
            }
        });

        // TODO: implement user profile observer
    }
}