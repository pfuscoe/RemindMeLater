package patrick.fuscoe.remindmelater.ui.todoitem;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;
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

        ToDoItemHeaderViewHolder(View v)
        {
            super(v);

            viewToDoItemHeaderLayout = v.findViewById(R.id.view_to_do_item_header_layout);
            viewToDoItemHeader = v.findViewById(R.id.view_to_do_item_header);
        }
    }

    public static class ToDoItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewToDoItemLayout;
        //ImageView viewToDoItemPriorityIcon;
        CheckBox viewToDoItemPriorityCheckbox;
        TextView viewToDoItemName;

        ToDoItemViewHolder(View v)
        {
            super(v);

            v.setOnClickListener(this);

            viewToDoItemLayout = v.findViewById(R.id.view_to_do_item_layout);
            //viewToDoItemPriorityIcon = v.findViewById(R.id.view_to_do_item_priority_icon);
            viewToDoItemPriorityCheckbox = v.findViewById(R.id.view_to_do_item_priority_checkbox);
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

        ToDoItemDoneHeaderViewHolder(View v)
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

        ToDoItemDoneViewHolder(View v)
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

    public ToDoItemListAdapter(List<ToDoItem> toDoItemList, List<ToDoItem> toDoItemListDone, Context context, ToDoItemListActivity.ToDoItemClickListener toDoItemClickListener)
    {
        this.toDoItemList = toDoItemList;
        this.toDoItemListDone = toDoItemListDone;

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
                    .inflate(R.layout.row_to_do_item_done_header, parent, false);

            return new ToDoItemDoneHeaderViewHolder(v);
        }
        else if (viewType == TYPE_ITEM_DONE)
        {
            ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_to_do_item_done, parent, false);

            return new ToDoItemDoneViewHolder(v);
        }
        else
        {
            throw new RuntimeException("No view holder type match - onCreateViewHolder - " + TAG);
        }
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position)
    {
        if (holder instanceof ToDoItemHeaderViewHolder)
        {
            ToDoItemHeaderViewHolder viewHolder = (ToDoItemHeaderViewHolder) holder;
            viewHolder.viewToDoItemHeaderLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
        }
        else if (holder instanceof ToDoItemViewHolder)
        {
            ToDoItem item = toDoItemList.get(position - 1);
            ToDoItemViewHolder viewHolder = (ToDoItemViewHolder) holder;

            viewHolder.viewToDoItemName.setText(item.getItemName());

            //int itemPriorityIconId = context.getResources().getIdentifier("checkbox_blank_outline", "drawable", context.getPackageName());
            //viewHolder.viewToDoItemPriorityIcon.setImageResource(itemPriorityIconId);
            //int itemPriorityIconColorId = selectItemPriorityIconColor(item.getPriority());
            ColorStateList colorStateList = selectItemPriorityCheckboxColor(item.getPriority());
            //viewHolder.viewToDoItemPriorityIcon.setColorFilter(ContextCompat.getColor(context, itemPriorityIconColorId));
            viewHolder.viewToDoItemPriorityCheckbox.setButtonTintList(colorStateList);
        }
        else if (holder instanceof ToDoItemDoneHeaderViewHolder)
        {
            ToDoItemDoneHeaderViewHolder viewHolder = (ToDoItemDoneHeaderViewHolder) holder;
            viewHolder.viewToDoItemDoneHeaderLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimaryLight));
        }
        else if (holder instanceof ToDoItemDoneViewHolder)
        {
            ToDoItem item = toDoItemListDone.get(position - numItemsToDo - 2);
            ToDoItemDoneViewHolder viewHolder = (ToDoItemDoneViewHolder) holder;

            viewHolder.viewToDoItemDoneName.setText(item.getItemName());

            int itemPriorityIconId = context.getResources().getIdentifier("checkbox_marked_outline", "drawable", context.getPackageName());
            viewHolder.viewToDoItemDonePriorityIcon.setImageResource(itemPriorityIconId);
            viewHolder.viewToDoItemDonePriorityIcon.setColorFilter(ContextCompat.getColor(context, R.color.greyDark));
        }
        else
        {
            throw new RuntimeException("No view holder type match - onBindViewHolder - " + TAG);
        }
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
                return R.color.red;

            case 2:
                return R.color.orangeDark;

            case 3:
                return R.color.blue;

            default:
                return R.color.red;
        }
    }

    private ColorStateList selectItemPriorityCheckboxColor(int priority)
    {
        switch (priority)
        {
            case 1:
                return ContextCompat.getColorStateList(context, R.color.checkbox_priority_high);

            case 2:
                return ContextCompat.getColorStateList(context, R.color.checkbox_priority_medium);

            case 3:
                return ContextCompat.getColorStateList(context, R.color.checkbox_priority_low);

            default:
                return ContextCompat.getColorStateList(context, R.color.checkbox_priority_high);
        }
    }

}
