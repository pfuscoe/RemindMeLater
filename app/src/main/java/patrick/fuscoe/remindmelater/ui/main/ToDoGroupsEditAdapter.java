package patrick.fuscoe.remindmelater.ui.main;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ToDoGroup;

public class ToDoGroupsEditAdapter extends RecyclerView.Adapter<ToDoGroupsEditAdapter.ToDoGroupsEditViewHolder> {

    private List<ToDoGroup> toDoGroupList;
    private Context context;

    private static ToDoFragment.ToDoGroupClickListener toDoGroupClickListener;
    private static ToDoFragment.ToDoGroupDragListener toDoGroupDragListener;

    public static class ToDoGroupsEditViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnTouchListener {

        CardView viewToDoGroupCard;
        ImageView viewToDoGroupIcon;
        TextView viewToDoGroupTitle;
        ImageView viewToDoGroupEditIcon;
        ImageView viewToDoGroupDragIcon;

        ToDoGroupsEditViewHolder(View v)
        {
            super(v);

            viewToDoGroupCard = v.findViewById(R.id.view_card_category_cardview);
            viewToDoGroupIcon = v.findViewById(R.id.view_card_category_icon);
            viewToDoGroupTitle = v.findViewById(R.id.view_card_category_title);
            viewToDoGroupEditIcon = v.findViewById(R.id.view_card_category_num_circle);
            viewToDoGroupDragIcon = v.findViewById(R.id.view_card_category_num_box);

            viewToDoGroupEditIcon.setOnClickListener(this);
            viewToDoGroupDragIcon.setOnTouchListener(this);
        }

        @Override
        public void onClick(View view) {
            toDoGroupClickListener.toDoGroupClicked(view, this.getLayoutPosition());
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

    public ToDoGroupsEditAdapter(List<ToDoGroup> toDoGroupList, Context context, ToDoFragment.ToDoGroupClickListener toDoGroupClickListener, ToDoFragment.ToDoGroupDragListener toDoGroupDragListener)
    {
        this.toDoGroupList = toDoGroupList;
        this.context = context;
        this.toDoGroupClickListener = toDoGroupClickListener;
        this.toDoGroupDragListener = toDoGroupDragListener;
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
        holder.viewToDoGroupTitle.setTextColor(ContextCompat.getColor(context, R.color.greyDark));

        holder.viewToDoGroupIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.home));
        holder.viewToDoGroupIcon.setColorFilter(ContextCompat.getColor(context, R.color.greyDark));

        int editIconId = context.getResources().getIdentifier("action_pencil", "drawable", context.getPackageName());
        holder.viewToDoGroupEditIcon.setImageResource(editIconId);
        holder.viewToDoGroupEditIcon.setColorFilter(ContextCompat.getColor(context, R.color.black));

        int dragIconId = context.getResources().getIdentifier("action_drag", "drawable", context.getPackageName());
        holder.viewToDoGroupDragIcon.setImageResource(dragIconId);
        holder.viewToDoGroupDragIcon.setColorFilter(ContextCompat.getColor(context, R.color.black));
    }

    @Override
    public int getItemCount() {
        return toDoGroupList.size();
    }

}
