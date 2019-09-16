package patrick.fuscoe.remindmelater;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;

public class UserPreferencesActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.UserPreferencesActivity";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public static FirebaseAuth auth;
    public static String userId;
    public static DocumentReference userDocRef;

    private UserProfile userProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_preferences);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        auth = FirebaseAuth.getInstance();
        userId = auth.getUid();
        userDocRef = db.collection("users").document(userId);

        Intent intent = getIntent();
        Gson gson = new Gson();

        Type dataTypeUserProfile = new TypeToken<UserProfile>(){}.getType();
        String userProfileString = intent.getStringExtra(RemindersFragment.USER_PROFILE);
        userProfile = gson.fromJson(userProfileString, dataTypeUserProfile);
        Log.d(TAG, "User Profile obtained from intent");


    }

}
