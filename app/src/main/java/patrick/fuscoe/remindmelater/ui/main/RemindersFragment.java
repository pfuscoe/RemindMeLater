package patrick.fuscoe.remindmelater.ui.main;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ReminderDetailsActivity;
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.UserProfile;

public class RemindersFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    public static final String TAG = "patrick.fuscoe.remindmelater.RemindersFragment";
    public static final String REMINDER_ITEM = "patrick.fuscoe.remindmelater.REMINDERS";
    public static final String REMINDERS_DOC_ID = "patrick.fuscoe.remindmelater.REMINDERS_DOC_ID";

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference remindersCollectionRef = db.collection("reminders");

    private final String userId = auth.getUid();
    private final DocumentReference userDocRef = db.collection("users").document(userId);
    private String remindersDocId;

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
            Log.d(TAG, ": Reminder " + position + " clicked");
            ReminderItem reminderItem = reminderItemList.get(position);

            // Open details activity for reminder item clicked
            if (!editMode)
            {
                Intent intent = new Intent(getContext(), ReminderDetailsActivity.class);
                Gson gson = new Gson();
                String reminderItemString = gson.toJson(reminderItem);
                intent.putExtra(REMINDER_ITEM, reminderItemString);
                intent.putExtra(REMINDERS_DOC_ID, remindersDocId);
                startActivity(intent);
            }
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

        reminderItemList = new ArrayList<>();

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

        setHasOptionsMenu(true);

        remindersRecyclerView = root.findViewById(R.id.view_reminders_recycler);
        remindersRecyclerView.setHasFixedSize(true);

        remindersLayoutManager = new LinearLayoutManager(getContext());
        remindersRecyclerView.setLayoutManager(remindersLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(remindersRecyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        remindersRecyclerView.addItemDecoration(dividerItemDecoration);

        remindersAdapter = new RemindersAdapter(reminderItemList, getContext(), reminderClickListener);
        remindersRecyclerView.setAdapter(remindersAdapter);

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
                    // Update UI views with data from snapshot
                    List<ReminderItem> reminderListFromDoc = new ArrayList<>();

                    Log.d(TAG, ": queryDocumentSnapshots size: " + queryDocumentSnapshots.size());

                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments())
                    {
                        remindersDocId = doc.getId();
                        Log.d(TAG, ": remindersDocId: " + remindersDocId);

                        Map<String, Object> docMap = doc.getData();

                        for (Map.Entry<String, Object> entry : docMap.entrySet())
                        {
                            if (!entry.getKey().contentEquals("userId"))
                            {
                                String title = entry.getKey();
                                HashMap<String, Object> reminderItemMap = (HashMap<String, Object>) entry.getValue();

                                String recurrenceString = (String) reminderItemMap.get("recurrence");
                                Period recurrence = Period.parse(recurrenceString);

                                int recurrenceNum = Math.toIntExact((long) reminderItemMap.get("recurrenceNum"));
                                Log.d(TAG, ": recurrenceNum: " + recurrenceNum);
                                String recurrenceInterval = (String) reminderItemMap.get("recurrenceInterval");

                                String nextOccurrence = (String) reminderItemMap.get("nextOccurrence");
                                //LocalDate nextOccurrence = LocalDate.parse(nextOccurrenceString);
                                //Log.d(TAG, ": nextOccurrence.toString: " + nextOccurrence.toString());

                                String category = (String) reminderItemMap.get("category");
                                int categoryIcon = Math.toIntExact((long) reminderItemMap.get("categoryIcon"));
                                Log.d(TAG, ": categoryIcon: " + categoryIcon);

                                String description = (String) reminderItemMap.get("description");

                                ReminderItem reminderItem = new ReminderItem(title, recurrenceNum,
                                        recurrenceInterval, nextOccurrence, category, categoryIcon, description);
                                reminderListFromDoc.add(reminderItem);
                            }
                        }
                    }

                    reminderItemList = reminderListFromDoc;
                    Log.d(TAG, ": reminderItemList size: " + reminderItemList.size());
                    updateRemindersDisplay();
                }
            }
        });

        // TODO: implement user profile observer
    }

    public void updateRemindersDisplay()
    {
        remindersAdapter = new RemindersAdapter(reminderItemList, getContext(), reminderClickListener);
        remindersRecyclerView.setAdapter(remindersAdapter);

        remindersAdapter.notifyDataSetChanged();
    }
}