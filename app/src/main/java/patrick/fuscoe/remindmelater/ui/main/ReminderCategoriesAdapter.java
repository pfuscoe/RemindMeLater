package patrick.fuscoe.remindmelater.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ReminderCategoriesActivity;
import patrick.fuscoe.remindmelater.models.ReminderCategory;

public class ReminderCategoriesAdapter extends RecyclerView.Adapter<ReminderCategoriesAdapter.ReminderCategoryViewHolder> {

    private List<ReminderCategory> reminderCategoryList;
    private Context context;

    private static ReminderCategoriesActivity.ReminderCategoryClickListener reminderCategoryClickListener;

    public static class ReminderCategoryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewRowReminderCategoryLayout;
        ImageView viewRowReminderCategoryIcon;
        TextView viewRowReminderCategoryTitle;
        ImageView viewRowReminderCategoryDeleteIcon;

        ReminderCategoryViewHolder(View v)
        {
            super(v);

            viewRowReminderCategoryLayout = v.findViewById(R.id.view_row_reminder_category_edit_layout);
            viewRowReminderCategoryIcon = v.findViewById(R.id.view_row_reminder_category_edit_icon);
            viewRowReminderCategoryTitle = v.findViewById(R.id.view_row_reminder_category_edit_title);
            viewRowReminderCategoryDeleteIcon = v.findViewById(R.id.view_row_reminder_category_delete_icon);

            v.setOnClickListener(this);
            viewRowReminderCategoryDeleteIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            reminderCategoryClickListener.reminderCategoryClicked(v, this.getLayoutPosition());
        }
    }

    public ReminderCategoriesAdapter(List<ReminderCategory> reminderCategoryList, Context context,
                                     ReminderCategoriesActivity.ReminderCategoryClickListener reminderCategoryClickListener)
    {
        this.reminderCategoryList = reminderCategoryList;
        this.context = context;
        this.reminderCategoryClickListener = reminderCategoryClickListener;
    }

    @NonNull
    @Override
    public ReminderCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.row_reminder_category_edit, viewGroup, false);

        return new ReminderCategoryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderCategoryViewHolder holder, final int position) {

        ReminderCategory reminderCategory = reminderCategoryList.get(position);

        holder.viewRowReminderCategoryTitle.setText(reminderCategory.getCategoryName());

        holder.viewRowReminderCategoryIcon.setImageResource(context.getResources().getIdentifier(
                reminderCategory.getIconName(), "drawable", context.getPackageName()));
        holder.viewRowReminderCategoryDeleteIcon.setImageResource(R.drawable.action_delete);
    }

    @Override
    public int getItemCount() {
        return reminderCategoryList.size();
    }
}
