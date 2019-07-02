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

public class ToDoItemListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String TAG = "patrick.fuscoe.remindmelater.ToDoItemListAdapter";

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

    public static class ToDoItemHeaderViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout viewToDoItemHeaderLayout;
        TextView viewToDoItemHeader;

        public ToDoItemHeaderViewHolder(View v)
        {
            super(v);

            viewToDoItemHeaderLayout = v.findViewById(R.id.view_to_do_item_header_layout);
            viewToDoItemHeader = v.findViewById(R.id.view_to_do_item_header);
        }
    }

    public static class ToDoItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewToDoItemLayout;
        ImageView viewToDoItemPriorityIcon;
        TextView viewToDoItemName;

        public ToDoItemViewHolder(View v)
        {
            super(v);

            v.setOnClickListener(this);

            viewToDoItemLayout = v.findViewById(R.id.view_to_do_item_layout);
            viewToDoItemPriorityIcon = v.findViewById(R.id.view_to_do_item_priority_icon);
            viewToDoItemName = v.findViewById(R.id.view_to_do_item_name);
        }

        @Override
        public void onClick(View v) {
            toDoItemClickListener.toDoItemClicked(v, this.getLayoutPosition());
        }
    }

    public static class ToDoItemDoneHeaderViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout viewToDoItemDoneHeaderLayout;
        TextView viewToDoItemDoneHeader;

        public ToDoItemDoneHeaderViewHolder(View v)
        {
            super(v);

            viewToDoItemDoneHeaderLayout = v.findViewById(R.id.view_to_do_item_done_header_layout);
            viewToDoItemDoneHeader = v.findViewById(R.id.view_to_do_item_done_header);
        }
    }

    public static class ToDoItemDoneViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewToDoItemDoneLayout;
        ImageView viewToDoItemDonePriorityIcon;
        TextView viewToDoItemDoneName;

        public ToDoItemDoneViewHolder(View v)
        {
            super(v);

            v.setOnClickListener(this);

            viewToDoItemDoneLayout = v.findViewById(R.id.view_to_do_item_done_layout);
            viewToDoItemDonePriorityIcon = v.findViewById(R.id.view_to_do_item_done_priority_icon);
            viewToDoItemDoneName = v.findViewById(R.id.view_to_do_item_done_name);
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType == TYPE_HEADER_TO_DO)
        {
            ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_to_do_item_header, parent, false);

            return new ToDoItemHeaderViewHolder(v);
        }
        else if (viewType == TYPE_ITEM_TO_DO)
        {
            ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_to_do_item, parent, false);

            return new ToDoItemViewHolder(v);
        }
        else if (viewType == TYPE_HEADER_DONE)
        {
            ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_to_do_item_header, parent, false);

            return new ToDoItemDoneHeaderViewHolder(v);
        }
        else if (viewType == TYPE_ITEM_DONE)
        {
            ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_to_do_item_done, parent, false);

            return new ToDoItemDoneViewHolder(v);
        }

        throw new RuntimeException("No view holder type match found in " + TAG);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        // TODO: Rework for various types

        ToDoItem toDoItem = toDoItemList.get(position);

        holder.viewToDoItemName.setText(toDoItem.getItemName());

        int itemPriorityIconId = context.getResources().getIdentifier("checkbox_blank_outline", "drawable", context.getPackageName());
        holder.viewToDoItemPriorityIcon.setImageResource(itemPriorityIconId);

        int itemPriorityIconColorId = selectItemPriorityIconColor(toDoItem.getPriority());
        holder.viewToDoItemPriorityIcon.setColorFilter(ContextCompat.getColor(context, itemPriorityIconColorId));
    }

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
        else if (0 < position && position <= numItemsToDo)
        {
            return TYPE_ITEM_TO_DO;
        }
        else if (position == numItemsToDo + 1)
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
