package patrick.fuscoe.remindmelater.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import patrick.fuscoe.remindmelater.FriendsActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ReminderCategoriesActivity;
import patrick.fuscoe.remindmelater.models.Friend;
import patrick.fuscoe.remindmelater.models.ReminderCategory;

/**
 * Recycler adapter for viewing friends in FriendsActivity
 */
public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendViewHolder> {
    
    private List<Friend> friendList;
    private Context context;

    private static FriendsActivity.FriendsClickListener friendsClickListener;

    public static class FriendViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewRowFriendsLayout;
        TextView viewRowFriendDisplayName;
        ImageView viewRowFriendShareToDoListIcon;
        ImageView viewRowFriendShareReminderIcon;
        ImageView viewRowFriendDeleteIcon;

        FriendViewHolder(View v)
        {
            super(v);

            viewRowFriendsLayout = v.findViewById(R.id.view_row_friends_layout);
            viewRowFriendDisplayName = v.findViewById(R.id.view_row_friend_display_name);
            viewRowFriendShareToDoListIcon = v.findViewById(R.id.view_row_friend_share_to_do_list_icon);
            viewRowFriendShareReminderIcon = v.findViewById(R.id.view_row_friend_share_reminder_icon);
            viewRowFriendDeleteIcon = v.findViewById(R.id.view_row_friend_delete_icon);

            v.setOnClickListener(this);
            viewRowFriendShareToDoListIcon.setOnClickListener(this);
            viewRowFriendShareReminderIcon.setOnClickListener(this);
            viewRowFriendDeleteIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            friendsClickListener.friendClicked(v, this.getLayoutPosition());
        }
    }

    public FriendsAdapter(List<Friend> friendList, Context context,
                          FriendsActivity.FriendsClickListener friendsClickListener)
    {
        this.friendList = friendList;
        this.context = context;
        this.friendsClickListener = friendsClickListener;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_friends, viewGroup, false);

        return new FriendViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, final int position) {

        Friend friend = friendList.get(position);

        holder.viewRowFriendDisplayName.setText(friend.getFriendDisplayName());

        holder.viewRowFriendShareToDoListIcon.setImageResource(R.drawable.action_playlist_plus);
        holder.viewRowFriendShareToDoListIcon.setColorFilter(ContextCompat.getColor(
                context, R.color.greyDark));

        holder.viewRowFriendShareReminderIcon.setImageResource(R.drawable.action_alarm_plus);
        holder.viewRowFriendShareReminderIcon.setColorFilter(ContextCompat.getColor(
                context, R.color.greyDark));

        holder.viewRowFriendDeleteIcon.setImageResource(R.drawable.action_delete);
        holder.viewRowFriendDeleteIcon.setColorFilter(ContextCompat.getColor(
                context, R.color.greyDark));
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }
}
