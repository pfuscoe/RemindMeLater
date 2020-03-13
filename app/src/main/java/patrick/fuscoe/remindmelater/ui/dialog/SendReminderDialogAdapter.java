package patrick.fuscoe.remindmelater.ui.dialog;

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

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderItem;

/**
 * Recycler adapter for viewing reminders in send reminder dialog
 */
public class SendReminderDialogAdapter extends RecyclerView.Adapter<SendReminderDialogAdapter.
        SendReminderViewHolder> {

    private List<ReminderItem> reminderItemList;
    private Context context;

    private static SendReminderDialogFragment.SendReminderClickListener sendReminderClickListener;

    public static class SendReminderViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        ImageView viewSendReminderIcon;
        TextView viewSendReminderTitle;
        TextView viewSendReminderDaysAway;

        SendReminderViewHolder(View v)
        {
            super(v);

            v.setOnClickListener(this);

            viewSendReminderIcon = v.findViewById(R.id.view_row_send_reminder_category_icon);
            viewSendReminderTitle = v.findViewById(R.id.view_row_send_reminder_title);
            viewSendReminderDaysAway = v.findViewById(R.id.view_row_send_reminder_days_away);
        }

        @Override
        public void onClick(View v) {
            sendReminderClickListener.onSendReminderClicked(v, this.getLayoutPosition());
        }
    }

    public SendReminderDialogAdapter(List<ReminderItem> reminderItemList, Context context,
                                     SendReminderDialogFragment.SendReminderClickListener
                                             sendReminderClickListener)
    {
        this.reminderItemList = reminderItemList;
        this.context = context;
        this.sendReminderClickListener = sendReminderClickListener;
    }

    @NonNull
    @Override
    public SendReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.row_send_reminder, parent, false);

        return new SendReminderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull SendReminderViewHolder holder, int position) {
        ReminderItem reminderItem = reminderItemList.get(position);

        holder.viewSendReminderIcon.setImageDrawable(ContextCompat.getDrawable(context,
                context.getResources().getIdentifier(reminderItem.getCategoryIconName(),
                        "drawable", context.getPackageName())));
        holder.viewSendReminderIcon.setColorFilter(ContextCompat.getColor(
                context, R.color.greyDim));

        holder.viewSendReminderTitle.setText(reminderItem.getTitle());

        // TODO: setup snooze icon / days away handling here
    }

    @Override
    public int getItemCount() {
        return reminderItemList.size();
    }
}
