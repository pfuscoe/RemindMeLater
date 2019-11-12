package patrick.fuscoe.remindmelater.ui.main;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ToDoGroup;

/**
 * Recycler adapter for viewing to do groups (aka to do lists) in edit mode of ToDoFragment in
 * MainActivity
 */
public class ToDoGroupsEditAdapter extends RecyclerView.Adapter<ToDoGroupsEditAdapter.ToDoGroupsEditViewHolder> {

    private List<ToDoGroup> toDoGroupList;
    private Context context;

    private static ToDoFragment.ToDoGroupClickListener toDoGroupClickListener;

    public static class ToDoGroupsEditViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView viewToDoGroupCard;
        ImageView viewToDoGroupIcon;
        TextView viewToDoGroupTitle;
        ImageView viewToDoGroupEditIcon;
        ImageView viewToDoGroupDeleteIcon;

        ToDoGroupsEditViewHolder(View v)
        {
            super(v);

            viewToDoGroupCard = v.findViewById(R.id.view_card_category_cardview);
            viewToDoGroupIcon = v.findViewById(R.id.view_card_category_icon);
            viewToDoGroupTitle = v.findViewById(R.id.view_card_category_title);
            viewToDoGroupEditIcon = v.findViewById(R.id.view_card_category_num_circle);
            viewToDoGroupDeleteIcon = v.findViewById(R.id.view_card_category_num_box);

            viewToDoGroupEditIcon.setOnClickListener(this);
            viewToDoGroupDeleteIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            toDoGroupClickListener.toDoGroupClicked(view, this.getLayoutPosition());
        }
    }

    public ToDoGroupsEditAdapter(List<ToDoGroup> toDoGroupList, Context context, ToDoFragment.ToDoGroupClickListener toDoGroupClickListener)
    {
        this.toDoGroupList = toDoGroupList;
        this.context = context;
        this.toDoGroupClickListener = toDoGroupClickListener;
    }

    @NonNull
    @Override
    public ToDoGroupsEditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_category, parent, false);

        return new ToDoGroupsEditViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoGroupsEditViewHolder holder, int position) {

        ToDoGroup toDoGroup = toDoGroupList.get(position);

        holder.viewToDoGroupTitle.setText(toDoGroup.getTitle());
        holder.viewToDoGroupTitle.setTextColor(ContextCompat.getColor(context, R.color.grey));

        holder.viewToDoGroupIcon.setImageDrawable(ContextCompat.getDrawable(context,
                context.getResources().getIdentifier(toDoGroup.getIconName(),
                        "drawable", context.getPackageName())));
        holder.viewToDoGroupIcon.setColorFilter(ContextCompat.getColor(context, R.color.grey));

        int editIconId = context.getResources().getIdentifier("action_pencil", "drawable", context.getPackageName());
        holder.viewToDoGroupEditIcon.setImageResource(editIconId);
        holder.viewToDoGroupEditIcon.setColorFilter(ContextCompat.getColor(context, R.color.black));

        int dragIconId = context.getResources().getIdentifier("action_delete", "drawable", context.getPackageName());
        holder.viewToDoGroupDeleteIcon.setImageResource(dragIconId);
        holder.viewToDoGroupDeleteIcon.setColorFilter(ContextCompat.getColor(context, R.color.black));
    }

    @Override
    public int getItemCount() {
        return toDoGroupList.size();
    }

}
