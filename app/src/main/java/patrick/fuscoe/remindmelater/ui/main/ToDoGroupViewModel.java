package patrick.fuscoe.remindmelater.ui.main;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import patrick.fuscoe.remindmelater.FirebaseDocumentLiveData;

/**
 * ViewModel for LiveData of a specific to do group (aka to do list) in FireStore
 */
public class ToDoGroupViewModel extends ViewModel {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DocumentReference toDoGroupDocRef;
    private FirebaseDocumentLiveData liveData;

    public ToDoGroupViewModel(String toDoGroupId)
    {
        this.toDoGroupDocRef = db.collection("todogroups").document(toDoGroupId);
        this.liveData = new FirebaseDocumentLiveData(toDoGroupDocRef);
    }

    @NonNull
    public LiveData<DocumentSnapshot> getDocumentSnapshotLiveData() {
        return liveData;
    }
}
