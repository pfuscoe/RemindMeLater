package patrick.fuscoe.remindmelater.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import patrick.fuscoe.remindmelater.FirebaseDocumentLiveData;

/**
 * ViewModel for LiveData of FireStore user profile document
 */
public class UserProfileViewModel extends ViewModel {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final String userId = auth.getCurrentUser().getUid();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DocumentReference userDocRef = db.collection("users").document(userId);

    private final FirebaseDocumentLiveData liveData = new FirebaseDocumentLiveData(userDocRef);

    @NonNull
    public LiveData<DocumentSnapshot> getDocumentSnapshotLiveData() {
        return liveData;
    }
}
