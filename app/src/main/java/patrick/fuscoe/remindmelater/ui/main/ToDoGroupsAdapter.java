package patrick.fuscoe.remindmelater.ui.main;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ToDoGroup;

public class ToDoGroupsAdapter extends RecyclerView.Adapter<ToDoGroupsAdapter.ToDoGroupsViewHolder> {

    private List<ToDoGroup> toDoGroupList;
    private Context context;

    private static ToDoFragment.ToDoGroupClickListener toDoGroupClickListener;

    public static class ToDoGroupsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView viewToDoGroupCard;
        ImageView viewToDoGroupIcon;
        TextView viewToDoGroupTitle;
        ImageView viewToDoGroupNumCircle;
        ImageView viewToDoGroupNumBox;

        public ToDoGroupsViewHolder(View toDoGroupsView)
        {
            super(toDoGroupsView);

            toDoGroupsView.setOnClickListener(this);

            viewToDoGroupCard = toDoGroupsView.findViewById(R.id.view_card_category_cardview);
            viewToDoGroupIcon = toDoGroupsView.findViewById(R.id.view_card_category_icon);
            viewToDoGroupTitle = toDoGroupsView.findViewById(R.id.view_card_category_title);
            viewToDoGroupNumCircle = toDoGroupsView.findViewById(R.id.view_card_category_num_circle);
            viewToDoGroupNumBox = toDoGroupsView.findViewById(R.id.view_card_category_num_box);
        }

        @Override
        public void onClick(View view) {
            toDoGroupClickListener.toDoGroupClicked(view, this.getLayoutPosition());
        }
    }

    public ToDoGroupsAdapter(List<ToDoGroup> toDoGroupList, Context context, ToDoFragment.ToDoGroupClickListener toDoGroupClickListener)
    {
        this.toDoGroupList = toDoGroupList;
        this.context = context;
        this.toDoGroupClickListener = toDoGroupClickListener;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ToDoGroupsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        // create a new view
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_category, parent, false);

        ToDoGroupsViewHolder vh = new ToDoGroupsViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ToDoGroupsViewHolder holder, final int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        ToDoGroup toDoGroup = toDoGroupList.get(position);

        holder.viewToDoGroupTitle.setText(toDoGroup.getTitle());

        // TODO: Load icons and numbers
        holder.viewToDoGroupNumCircle.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.numeric_1_circle));
        holder.viewToDoGroupNumCircle.setColorFilter(ContextCompat.getColor(context, R.color.redDark));

        holder.viewToDoGroupNumBox.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.numeric_5_box_multiple_outline));
        holder.viewToDoGroupNumBox.setColorFilter(ContextCompat.getColor(context, R.color.greyDark));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return toDoGroupList.size();
    }

}
