package patrick.fuscoe.remindmelater.ui.main;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ToDoGroup;

public class ToDoGroupsReorderAdapter extends RecyclerView.Adapter<ToDoGroupsReorderAdapter.ToDoGroupsReorderViewHolder> {

    private List<ToDoGroup> toDoGroupList;
    private Context context;

    private static ToDoFragment.ToDoGroupDragListener toDoGroupDragListener;

    public static class ToDoGroupsReorderViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener {

        CardView viewToDoGroupCard;
        ImageView viewToDoGroupIcon;
        TextView viewToDoGroupTitle;
        ImageView viewToDoGroupDragIcon;

        ToDoGroupsReorderViewHolder(View v)
        {
            super(v);

            viewToDoGroupCard = v.findViewById(R.id.view_card_category_cardview);
            viewToDoGroupIcon = v.findViewById(R.id.view_card_category_icon);
            viewToDoGroupTitle = v.findViewById(R.id.view_card_category_title);
            viewToDoGroupDragIcon = v.findViewById(R.id.view_card_category_num_box);

            viewToDoGroupDragIcon.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN)
            {
                toDoGroupDragListener.onStartDrag(this);
            }
            else if (event.getActionMasked() == MotionEvent.ACTION_UP)
            {
                v.performClick();
            }
            return false;
        }
    }

    public ToDoGroupsReorderAdapter(List<ToDoGroup> toDoGroupList, Context context, ToDoFragment.ToDoGroupDragListener toDoGroupDragListener)
    {
        this.toDoGroupList = toDoGroupList;
        this.context = context;
        this.toDoGroupDragListener = toDoGroupDragListener;
    }

    @NonNull
    @Override
    public ToDoGroupsReorderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_category, parent, false);

        return new ToDoGroupsReorderViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoGroupsReorderViewHolder holder, int position) {

        ToDoGroup toDoGroup = toDoGroupList.get(position);

        holder.viewToDoGroupTitle.setText(toDoGroup.getTitle());
        holder.viewToDoGroupTitle.setTextColor(ContextCompat.getColor(context, R.color.grey));

        holder.viewToDoGroupIcon.setImageDrawable(ContextCompat.getDrawable(context,
                context.getResources().getIdentifier(toDoGroup.getIconName(),
                        "drawable", context.getPackageName())));
        holder.viewToDoGroupIcon.setColorFilter(ContextCompat.getColor(context, R.color.grey));

        int dragIconId = context.getResources().getIdentifier("action_drag", "drawable", context.getPackageName());
        holder.viewToDoGroupDragIcon.setImageResource(dragIconId);
        holder.viewToDoGroupDragIcon.setColorFilter(ContextCompat.getColor(context, R.color.black));
    }

    @Override
    public int getItemCount() {
        return toDoGroupList.size();
    }

}
