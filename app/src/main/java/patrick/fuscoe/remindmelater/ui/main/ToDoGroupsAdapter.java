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

        ToDoGroupsViewHolder(View toDoGroupsView)
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

    @NonNull
    @Override
    public ToDoGroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        CardView v = (CardView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_category, parent, false);

        return new ToDoGroupsViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ToDoGroupsViewHolder holder, final int position) {

        ToDoGroup toDoGroup = toDoGroupList.get(position);

        holder.viewToDoGroupTitle.setText(toDoGroup.getTitle());

        String numBoxIconString = selectNumBoxIconName(toDoGroup);
        String numCircleIconString = selectNumCircleIconName(toDoGroup);

        holder.viewToDoGroupIcon.setImageDrawable(ContextCompat.getDrawable(context,
                context.getResources().getIdentifier(toDoGroup.getIconName(),
                        "drawable", context.getPackageName())));
        holder.viewToDoGroupIcon.setColorFilter(ContextCompat.getColor(context, R.color.greyDark));

        if (toDoGroup.getNumPriorityOneItems() != 0)
        {
            int circleIconId = context.getResources().getIdentifier(numCircleIconString, "drawable", context.getPackageName());
            holder.viewToDoGroupNumCircle.setImageResource(circleIconId);
            holder.viewToDoGroupNumCircle.setColorFilter(ContextCompat.getColor(context, R.color.red));
        }

        int boxIconId = context.getResources().getIdentifier(numBoxIconString, "drawable", context.getPackageName());
        holder.viewToDoGroupNumBox.setImageResource(boxIconId);
        holder.viewToDoGroupNumBox.setColorFilter(ContextCompat.getColor(context, R.color.greyDark));
    }

    @Override
    public int getItemCount() {
        return toDoGroupList.size();
    }

    private String selectNumCircleIconName(ToDoGroup toDoGroup)
    {
        int numPriorityOneItems = toDoGroup.getNumPriorityOneItems();

        switch (numPriorityOneItems)
        {
            case 0:
                return "numeric_0_circle";

            case 1:
                return "numeric_1_circle";

            case 2:
                return "numeric_2_circle";

            case 3:
                return "numeric_3_circle";

            case 4:
                return "numeric_4_circle";

            case 5:
                return "numeric_5_circle";

            case 6:
                return "numeric_6_circle";

            case 7:
                return "numeric_7_circle";

            case 8:
                return "numeric_8_circle";

            default:
                return "numeric_9_plus_circle";
        }
    }

    private String selectNumBoxIconName(ToDoGroup toDoGroup)
    {
        int numUnfinishedItems = toDoGroup.getNumUnfinishedItems();

        switch (numUnfinishedItems)
        {
            case 0:
                return "numeric_0_box_multiple_outline";

            case 1:
                return "numeric_1_box_multiple_outline";

            case 2:
                return "numeric_2_box_multiple_outline";

            case 3:
                return "numeric_3_box_multiple_outline";

            case 4:
                return "numeric_4_box_multiple_outline";

            case 5:
                return "numeric_5_box_multiple_outline";

            case 6:
                return "numeric_6_box_multiple_outline";

            case 7:
                return "numeric_7_box_multiple_outline";

            case 8:
                return "numeric_8_box_multiple_outline";

            default:
                return "numeric_9_plus_box_multiple_outline";
        }

    }

}
