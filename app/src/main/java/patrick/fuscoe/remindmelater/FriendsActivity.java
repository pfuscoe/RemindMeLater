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
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.FrameLayout;
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
import patrick.fuscoe.remindmelater.models.ReminderItem;
import patrick.fuscoe.remindmelater.models.ToDoGroup;
import patrick.fuscoe.remindmelater.models.UserProfile;
import patrick.fuscoe.remindmelater.ui.dialog.AddFriendDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.RemoveFriendDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.SendReminderDialogFragment;
import patrick.fuscoe.remindmelater.ui.dialog.ShareToDoGroupDialogFragment;
import patrick.fuscoe.remindmelater.ui.main.FriendsAdapter;
import patrick.fuscoe.remindmelater.util.FirebaseDocUtils;


/**
 * Manages UI for Friends List. Handles friend requests, To Do List sharing and requests to add
 * reminders to friends.
 */

public class FriendsActivity extends AppCompatActivity implements
        AddFriendDialogFragment.AddFriendDialogListener,
        ShareToDoGroupDialogFragment.ShareToDoGroupDialogListener,
        SendReminderDialogFragment.SendReminderDialogListener,
        RemoveFriendDialogFragment.RemoveFriendDialogListener {

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
    private List<ToDoGroup> toDoGroupList;
    private List<ReminderItem> reminderItemList;

    private Friend selectedFriend;
    private ToDoGroup selectedToDoGroup;
    private ReminderItem selectedReminderItem;
    private String remindersDocId;

    private MenuItem tipsMenuItem;
    private boolean isTipsOn;
    private FrameLayout viewFrameTips;
    private WebView viewTipsWebView;


    public interface FriendsClickListener {
        void friendClicked(View v, int position);
    }

    private FriendsClickListener friendsClickListener = new FriendsClickListener() {
        @Override
        public void friendClicked(View v, int position) {

            selectedFriend = friendList.get(position);

            switch (v.getId())
            {
                case R.id.view_row_friend_share_to_do_list_icon:
                    openShareToDoGroupDialog();
                    return;

                case R.id.view_row_friend_share_reminder_icon:
                    openSendReminderDialog();
                    return;

                case R.id.view_row_friend_delete_icon:
                    openRemoveFriendDialog();
                    return;
            }
        }
    };

    public interface ShareToDoGroupSelectedListener {
        void onToDoGroupSelected(DialogFragment dialogFragment, ToDoGroup toDoGroup);
    }

    private ShareToDoGroupSelectedListener shareToDoGroupSelectedListener =
            new ShareToDoGroupSelectedListener() {
        @Override
        public void onToDoGroupSelected(DialogFragment dialogFragment, ToDoGroup toDoGroup)
        {
            selectedToDoGroup = toDoGroup;

            for (String subscriberId : selectedToDoGroup.getSubscribers())
            {
                if (selectedFriend.getFriendId().equals(subscriberId))
                {
                    Toast.makeText(getApplicationContext(), selectedFriend.
                            getFriendDisplayName() + " is already subscribed to this list!",
                            Toast.LENGTH_LONG).show();
                    dialogFragment.dismiss();
                    return;
                }
            }

            // TODO: Prompt user to confirm share to do list

            sendShareToDoRequestMessage(selectedFriend, selectedToDoGroup);
            dialogFragment.dismiss();
        }
    };

    public interface SendReminderSelectedListener {
        void onReminderSelected(DialogFragment dialogFragment, ReminderItem reminderItem);
    }

    private SendReminderSelectedListener sendReminderSelectedListener =
            new SendReminderSelectedListener() {
        @Override
        public void onReminderSelected(DialogFragment dialogFragment, ReminderItem reminderItem)
        {
            selectedReminderItem = reminderItem;

            // TODO: Prompt user to confirm send reminder

            sendReminderRequestMessage(selectedFriend, remindersDocId, selectedReminderItem);
            dialogFragment.dismiss();
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

        Type dataTypeToDoGroupList = new TypeToken<List<ToDoGroup>>(){}.getType();
        String toDoGroupListString = intent.getStringExtra(MainActivity.TO_DO_GROUP_LIST);
        Log.d(TAG, "toDoGroupListString: " + toDoGroupListString);
        toDoGroupList = gson.fromJson(toDoGroupListString, dataTypeToDoGroupList);

        Type dataTypeReminderItemList = new TypeToken<List<ReminderItem>>(){}.getType();
        String reminderItemListString = intent.getStringExtra(MainActivity.REMINDER_ITEM_LIST);
        Log.d(TAG, "reminderItemListString: " + reminderItemListString);
        reminderItemList = gson.fromJson(reminderItemListString, dataTypeReminderItemList);

        remindersDocId = intent.getStringExtra(MainActivity.REMINDERS_DOC_ID);

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

        viewFrameTips = findViewById(R.id.view_friends_tips);
        viewTipsWebView = findViewById(R.id.view_friends_tips_webview);
        viewTipsWebView.loadUrl("file:///android_asset/tips_friends.html");
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

        tipsMenuItem = menu.findItem(R.id.menu_main_tips);

        if (friendList.size() == 0)
        {
            showFriendsTips();
        }
        else
        {
            isTipsOn = false;
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.menu_main_add:
                openAddFriendDialog();
                return true;

            case R.id.menu_main_tips:
                toggleFriendsTips();
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

    // Write a message to FireStore to trigger cloud function for sending friend request
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

    // Write a message to FireStore to trigger cloud function for sending share to do request
    private void sendShareToDoRequestMessage(Friend friend, ToDoGroup toDoGroup)
    {
        Map<String, Object> shareToDoRequestMessageDocMap =
                FirebaseDocUtils.createShareToDoRequestMessageDoc(friend, userProfile, toDoGroup);

        db.collection("messages")
                .add(shareToDoRequestMessageDocMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Share to do request message successfully written. " +
                                "FireStore messageID: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(), "Share to do request sent " +
                                "successfully!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing share to do request document to cloud: " +
                                e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error sending share to do " +
                                "request to cloud: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Write a message to FireStore to trigger cloud function for sending reminder request
    private void sendReminderRequestMessage(Friend friend, String reminderDocId,
                                            ReminderItem reminderItem)
    {
        Map<String, Object> sendReminderRequestMessageDocMap = FirebaseDocUtils.
                createSendReminderMessageDoc(friend, userProfile, reminderDocId,
                        selectedReminderItem.getTitle());

        db.collection("messages")
                .add(sendReminderRequestMessageDocMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Send reminder request message successfully written. " +
                                "FireStore messageID: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(), "Send reminder request sent " +
                                "successfully!", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing Send reminder request document to cloud: " +
                                e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error sending reminder " +
                                "request to cloud: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Write a message to FireStore to trigger cloud function for removing a friend
    private void sendRemoveFriendMessage(final Friend friend, UserProfile userProfile)
    {
        Map<String, Object> removeFriendMessageDoc = FirebaseDocUtils.
                createRemoveFriendMessageDoc(friend, userProfile);

        db.collection("messages")
                .add(removeFriendMessageDoc)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "Remove friend message successfully written. " +
                                "FireStore messageID: " + documentReference.getId());
                        Toast.makeText(getApplicationContext(), friend.getFriendDisplayName() +
                                " has been removed.", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Error writing remove friend message document to cloud: " +
                                e.getMessage());
                        Toast.makeText(getApplicationContext(), "Error removing friend: " +
                                e.getMessage(), Toast.LENGTH_LONG).show();
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
        DialogFragment dialogFragment = new ShareToDoGroupDialogFragment(toDoGroupList,
                shareToDoGroupSelectedListener);
        dialogFragment.show(getSupportFragmentManager(), "shareToDoGroup");
    }

    private void openSendReminderDialog()
    {
        DialogFragment dialogFragment = new SendReminderDialogFragment(reminderItemList,
                sendReminderSelectedListener);
        dialogFragment.show(getSupportFragmentManager(), "sendReminder");
    }

    private void openRemoveFriendDialog()
    {
        DialogFragment dialogFragment = new RemoveFriendDialogFragment(selectedFriend);
        dialogFragment.show(getSupportFragmentManager(), "removeFriend");
    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialogFragment) {

        if (dialogFragment instanceof AddFriendDialogFragment)
        {
            Dialog dialogView = dialogFragment.getDialog();
            EditText viewFriendEmail = dialogView.findViewById(R.id.dialog_add_friend_email);
            String friendEmail = viewFriendEmail.getText().toString();

            if (friendEmail.equals(""))
            {
                Toast.makeText(this, "Add Friend Failed: Must enter an email address", Toast.LENGTH_LONG).show();
                return;
            }

            sendFriendRequestMessage(friendEmail);
        }
        else if (dialogFragment instanceof RemoveFriendDialogFragment)
        {
            sendRemoveFriendMessage(selectedFriend, userProfile);
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
        else if (dialogFragment instanceof SendReminderDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Send Reminder Cancelled",
                    Toast.LENGTH_SHORT).show();
        }
        else if (dialogFragment instanceof RemoveFriendDialogFragment)
        {
            Toast.makeText(getApplicationContext(), "Remove Friend Cancelled",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showFriendsTips()
    {
        viewFrameTips.setVisibility(View.VISIBLE);
        viewFriendsRecycler.setVisibility(View.INVISIBLE);
        tipsMenuItem.setTitle(R.string.hide_tips);
        isTipsOn = true;
    }

    private void hideFriendsTips()
    {
        viewFrameTips.setVisibility(View.INVISIBLE);
        viewFriendsRecycler.setVisibility(View.VISIBLE);
        tipsMenuItem.setTitle(R.string.show_tips);
        isTipsOn = false;
    }

    private void toggleFriendsTips()
    {
        if (isTipsOn)
        {
            hideFriendsTips();
        }
        else
        {
            showFriendsTips();
        }
    }
}
