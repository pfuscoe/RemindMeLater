package patrick.fuscoe.remindmelater.ui.main;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import patrick.fuscoe.remindmelater.FirebaseQueryLiveData;

public class ToDoGroupViewModel extends ViewModel {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final String userID = auth.getCurrentUser().getUid();

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final CollectionReference toDoGroups = db.collection("toDoGroups");

    private static final Query toDoGroupsQuery = toDoGroups.whereArrayContains("subscriptions", userID);

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(toDoGroupsQuery);

    @NonNull
    public LiveData<QuerySnapshot> getQuerySnapshotLiveData() {
        return liveData;
    }

}