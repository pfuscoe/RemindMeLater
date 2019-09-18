package patrick.fuscoe.remindmelater.ui.main;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderItem;

public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder> {

    private List<ReminderItem> reminderItemList;
    private Context context;

    private static RemindersFragment.ReminderClickListener reminderClickListener;

    public static class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewRowReminderLayout;
        ImageView viewRowReminderCategoryIcon;
        TextView viewRowReminderTitle;
        ImageView viewRowReminderSnoozeIcon;
        TextView viewRowReminderDaysAway;

        ReminderViewHolder(View v)
        {
            super(v);

            v.setOnClickListener(this);

            viewRowReminderLayout = v.findViewById(R.id.view_row_reminder_layout);
            viewRowReminderCategoryIcon = v.findViewById(R.id.view_row_reminder_category_icon);
            viewRowReminderTitle = v.findViewById(R.id.view_row_reminder_title);
            viewRowReminderSnoozeIcon = v.findViewById(R.id.view_row_reminder_snooze_icon);
            viewRowReminderDaysAway = v.findViewById(R.id.view_row_reminder_days_away);
        }

        @Override
        public void onClick(View v) {
            reminderClickListener.reminderClicked(v, this.getLayoutPosition());
        }
    }

    public RemindersAdapter(List<ReminderItem> reminderItemList, Context context, RemindersFragment.ReminderClickListener reminderClickListener)
    {
        this.reminderItemList = reminderItemList;
        this.context = context;
        this.reminderClickListener = reminderClickListener;
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_reminder, viewGroup, false);

        return new ReminderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, final int position) {
        ReminderItem reminderItem = reminderItemList.get(position);

        holder.viewRowReminderTitle.setText(reminderItem.getTitle());

        // TODO: Setup snooze icon
        holder.viewRowReminderCategoryIcon.setImageResource(context.getResources().getIdentifier(
                reminderItem.getCategoryIconName(), "drawable", context.getPackageName()));
        holder.viewRowReminderDaysAway.setText(String.valueOf(reminderItem.getDaysAway()));
    }

    @Override
    public int getItemCount() {
        return reminderItemList.size();
    }
}
