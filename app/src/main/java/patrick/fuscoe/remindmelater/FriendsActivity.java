package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;


/**
 * Manages UI for Friends List. Handles friend requests, To Do List sharing and requests to add
 * reminders to friends.
 */

public class FriendsActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.FriendsActivity";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
    }
}
