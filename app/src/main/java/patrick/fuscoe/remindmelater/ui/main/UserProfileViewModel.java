package patrick.fuscoe.remindmelater.ui.main;

import android.arch.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class UserProfileViewModel extends ViewModel {

    private static final FirebaseAuth auth = FirebaseAuth.getInstance();
    private static final String userId = auth.getCurrentUser().getUid();

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final DocumentReference userDocRef = db.collection("users").document(userId);

    private final Query userQuery = userDocRef;
}
