package patrick.fuscoe.remindmelater.ui.todoitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;
import patrick.fuscoe.remindmelater.comparator.SortToDoItemByDate;
import patrick.fuscoe.remindmelater.models.ToDoItem;

public class ToDoItemListAdapter extends RecyclerView.Adapter<ToDoItemListAdapter.ToDoItemViewHolder> {

    private static final int TYPE_HEADER_TO_DO = 0;
    private static final int TYPE_HEADER_DONE = 1;
    private static final int TYPE_ITEM_TO_DO = 2;
    private static final int TYPE_ITEM_DONE = 3;

    private List<ToDoItem> toDoItemList;
    private List<ToDoItem> toDoItemListDone;
    private int numItemsToDo;
    private int numItemsDone;
    private Context context;

    private static ToDoItemListActivity.ToDoItemClickListener toDoItemClickListener;

    public static class ToDoItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewToDoItemLayout;
        ImageView viewToDoItemPriorityIcon;
        TextView viewToDoItemName;

        public ToDoItemViewHolder(View toDoItemView)
        {
            super(toDoItemView);

            toDoItemView.setOnClickListener(this);

            viewToDoItemLayout = toDoItemView.findViewById(R.id.view_to_do_item_layout);
            viewToDoItemPriorityIcon = toDoItemView.findViewById(R.id.view_to_do_item_priority_icon);
            viewToDoItemName = toDoItemView.findViewById(R.id.view_to_do_item_name);
        }

        @Override
        public void onClick(View v) {
            toDoItemClickListener.toDoItemClicked(v, this.getLayoutPosition());
        }
    }

    public ToDoItemListAdapter(List<ToDoItem> toDoItemListUnsorted, Context context, ToDoItemListActivity.ToDoItemClickListener toDoItemClickListener)
    {
        this.toDoItemList = new ArrayList<>();
        this.toDoItemListDone = new ArrayList<>();

        for (ToDoItem item : toDoItemListUnsorted)
        {
            if (item.isDone())
            {
                toDoItemListDone.add(item);
            }
            else
            {
                toDoItemList.add(item);
            }
        }

        Collections.sort(toDoItemList);
        Collections.sort(toDoItemListDone, new SortToDoItemByDate());

        this.numItemsToDo = toDoItemList.size();
        this.numItemsDone = toDoItemListDone.size();

        this.context = context;
        this.toDoItemClickListener = toDoItemClickListener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ToDoItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        // create a new view
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_to_do_item, parent, false);

        ToDoItemViewHolder vh = new ToDoItemViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ToDoItemViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        ToDoItem toDoItem = toDoItemList.get(position);

        holder.viewToDoItemName.setText(toDoItem.getItemName());

        // TODO: Load priority icon
        int itemPriorityIconId = context.getResources().getIdentifier("checkbox_blank_outline", "drawable", context.getPackageName());
        holder.viewToDoItemPriorityIcon.setImageResource(itemPriorityIconId);

        int itemPriorityIconColorId = selectItemPriorityIconColor(toDoItem.getPriority());
        holder.viewToDoItemPriorityIcon.setColorFilter(ContextCompat.getColor(context, itemPriorityIconColorId));
    }

    // Total items are all ToDoItems and 2 headers
    @Override
    public int getItemCount() {
        if (numItemsDone > 0) {
            return numItemsToDo + numItemsDone + 2;
        }
        else
        {
            return numItemsToDo + 1;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
        {
            return TYPE_HEADER_TO_DO;
        }
        else if (0 < position && position < numItemsToDo)
        {
            return TYPE_ITEM_TO_DO;
        }
        else if (position == numItemsToDo)
        {
            return TYPE_HEADER_DONE;
        }
        else
        {
            return TYPE_ITEM_DONE;
        }
    }

    private int selectItemPriorityIconColor(int priority)
    {
        switch (priority)
        {
            case 1:
                return R.color.redDark;

            case 2:
                return R.color.orangeDark;

            case 3:
                return R.color.blueDark;

            default:
                return R.color.redDark;
        }
    }

}
