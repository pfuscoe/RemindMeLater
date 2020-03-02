package patrick.fuscoe.remindmelater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.models.Friend;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.AddFriendDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.ShareToDoGroupDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.FriendsAdapter;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;


/**
 * Manages UI for Friends List. Handles friend requests, To Do List sharing and requests to add
 * reminders to friends.
 */

public class FriendsActivity extends AppCompatActivity implements
        AddFriendDialogFragment.AddFriendDialogListener,
        ShareToDoGroupDialogFragment.ShareToDoGroupDialogListener {

    public static final String TAG = "patrick.fuscoe.remindmelater.FriendsActivity";

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private static FirebaseAuth auth;
    private static String userId;
    private static DocumentReference userDocRef;
    private static DocumentReference remindersDocRef;

    private RecyclerView viewFriendsRecycler;
    private RecyclerView.LayoutManager friendsRecyclerLayoutManager;
    private FriendsAdapter friendsAdapter;

    private UserProfile userProfile;
    public List<Friend> friendList;

    private ToDoGroup selectedToDoGroup;


    private FriendsClickListener friendsClickListener = new FriendsClickListener() {
        @Override
        public void friendClicked(View v, int position) {

            // TODO: setup click action
            Friend friend = friendList.get(position);

            switch (v.getId())
            {
                case R.id.view_row_friend_share_to_do_list_icon:
                    openShareToDoGroupDialog();
                    return;

                case R.id.view_row_friend_share_reminder_icon:
                    // add share (copy) reminder feature here
                    return;

                case R.id.view_row_friend_delete_icon:
                    // TODO: implement remove friend
                    return;
            }

        }
    };

    public interface FriendsClickListener {
        void friendClicked(View v, int position);
    }

    public interface ShareToDoGroupSelectedListener {
        void onToDoGroupSelected(DialogFragment dialogFragment, ToDoGroup toDoGroup);
    }

    private ShareToDoGroupSelectedListener shareToDoGroupSelectedListener =
            new ShareToDoGroupSelectedListener() {
        @Override
        public void onToDoGroupSelected(DialogFragment dialogFragment, ToDoGroup toDoGroup)
        {
            selectedToDoGroup = toDoGroup;
            dialogFragment.dismiss();
            // TODO: confirmation? then execute share
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);
        Toolbar toolbar = findViewById(R.id.toolbar_friends);
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

        friendList = userProfile.getFriendArrayList();

        viewFriendsRecycler = findViewById(R.id.view_friends_recycler);
        viewFriendsRecycler.setHasFixedSize(true);

        friendsRecyclerLayoutManager = new LinearLayoutManager(this);
        viewFriendsRecycler.setLayoutManager(friendsRecyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                viewFriendsRecycler.getContext(), DividerItemDecoration.VERTICAL);
        viewFriendsRecycler.addItemDecoration(dividerItemDecoration);

        friendsAdapter = new FriendsAdapter(friendList, this, friendsClickListener);
        viewFriendsRecycler.setAdapter(friendsAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);

        menu.removeItem(R.id.menu_main_edit);
        menu.removeItem(R.id.menu_main_logout);
        menu.removeItem(R.id.menu_main_friends);
        menu.removeItem(R.id.menu_main_user_settings);
        menu.removeItem(R.id.menu_main_reorder);
        menu.removeItem(R.id.menu_main_edit_reminder_categories);
        menu.removeItem(R.id.menu_main_tips);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                openAddFriendDialog();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateFriendsDisplay()
    {
        friendsAdapter = new FriendsAdapter(friendList, this, friendsClickListener);
        viewFriendsRecycler.setAdapter(friendsAdapter);

        friendsAdapter.notifyDataSetChanged();
    }

    // Write a message to FireStore to trigger cloud function
    private void sendFriendRequestMessage(String friendEmail)
    {
        Map<String, Object> friendRequestMessageDocMap =
                FirebaseDocUtils.createFriendRequestMessageDoc(friendEmail, userProfile);

        db.collection("messages")
                .add(friendRequestMessageDocMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Friend request message successfully written. FireStore" +
                                " messageID: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(), "Friend request sent " +
                                "successfully!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing friend request document to cloud: " +
                                e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error sending friend " +
                                "request to cloud: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void openAddFriendDialog()
    {
        DialogFragment dialogFragment = new AddFriendDialogFragment();
        dialogFragment.show(getSupportFragmentManager(), "addFriend");
    }

    private void openShareToDoGroupDialog()
    {
        DialogFragment dialogFragment = new ShareToDoGroupDialogFragment(
                shareToDoGroupSelectedListener);
        dialogFragment.show(getSupportFragmentManager(), "shareToDoGroup");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment) {

        if (dialogFragment instanceof AddFriendDialogFragment)
        {
            Dialog dialogView = dialogFragment.getDialog();
            EditText viewFriendEmail = dialogView.findViewById(R.id.dialog_add_friend_email);
            EditText viewFriendNickname = dialogView.findViewById(R.id.dialog_add_friend_nickname);
            String friendEmail = viewFriendEmail.getText().toString();
            String friendNickname = viewFriendNickname.getText().toString();

            if (friendEmail.equals(""))
            {
                Toast.makeText(this, "Add Friend Failed: Must enter an email address", Toast.LENGTH_LONG).show();
                return;
            }

            if (friendNickname.equals(""))
            {
                Toast.makeText(this, "Add Friend Failed: Must enter a nickname", Toast.LENGTH_LONG).show();
                return;
            }

            sendFriendRequestMessage(friendEmail);
        }
    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialogFragment) {
        if (dialogFragment instanceof AddFriendDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Add Friend Cancelled",
                    Toast.LENGTH_SHORT).show();
        }
        else if (dialogFragment instanceof ShareToDoGroupDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Share To Do List Cancelled",
                    Toast.LENGTH_SHORT).show();
        }
    }
}
