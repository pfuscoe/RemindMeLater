package patrick.fuscoe.remindmelater.ui.todoitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;
import patrick.fuscoe.remindmelater.models.ToDoItem;

public class ToDoItemListAdapter extends RecyclerView.Adapter<ToDoItemListAdapter.ToDoItemViewHolder> {

    private List<ToDoItem> toDoItemList;
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

    public ToDoItemListAdapter(List<ToDoItem> toDoItemList, Context context, ToDoItemListActivity.ToDoItemClickListener toDoItemClickListener)
    {
        this.toDoItemList = toDoItemList;
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

        // TODO: Load icons and numbers
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return toDoItemList.size();
    }

}
