package patrick.fuscoe.remindmelater.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import patrick.fuscoe.remindmelater.FirebaseQueryLiveData;
import patrick.fuscoe.remindmelater.MainActivity;

/**
 * ViewModel for LiveData of user's reminders on FireStore
 */
public class RemindersViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference reminders = db.collection("reminders");

    private final Query remindersQuery = reminders.whereEqualTo("userId", MainActivity.userId);

    private final FirebaseQueryLiveData liveData = new FirebaseQueryLiveData(remindersQuery);

    @NonNull
    public LiveData<QuerySnapshot> getQuerySnapshotLiveData() {
        return liveData;
    }

}
