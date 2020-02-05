package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import patrick.fuscoe.remindmelater.models.UserProfile;


/**
 * Manages UI for Friends List. Handles friend requests, To Do List sharing and requests to add
 * reminders to friends.
 */

public class FriendsActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.FriendsActivity";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static FirebaseAuth auth;
    private static String userId;
    private static DocumentReference userDocRef;
    private static DocumentReference remindersDocRef;

    private RecyclerView viewFriendsRecycler;
    private RecyclerView.LayoutManager friendsRecyclerLayoutManager;
    //private FriendsAdapter friendsAdapter;

    private UserProfile userProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = findViewById(R.id.toolbar_reminder_categories);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);
        remindersDocRef = MainActivity.remindersDocRef;

        Intent intent = getIntent();
        Gson gson = new Gson();

        Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
        String userProfileString = intent.getStringExtra(MainActivity.USER_PROFILE);
        Log.d(TAG, "userProfileString: " + userProfileString);
        userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Friends");
    }
}
