package patrick.fuscoe.remindmelater.ui.dialog;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ToDoGroup;

/**
 * Recycler adapter for viewing to do groups (aka to do lists) in share to do group dialog
 */
public class ShareToDoGroupDialogAdapter extends RecyclerView.Adapter<ShareToDoGroupDialogAdapter.
        ShareToDoGroupViewHolder> {

    private List<ToDoGroup> toDoGroupList;
    private Context context;

    private static ShareToDoGroupDialogFragment.ShareToDoGroupClickListener
            shareToDoGroupClickListener;

    public static class ShareToDoGroupViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        //ConstraintLayout viewShareToDoGroupLayout;
        ImageView viewShareToDoGroupIcon;
        TextView viewShareToDoGroupTitle;
        ImageView viewShareToDoGroupIconRight;

        ShareToDoGroupViewHolder(View v)
        {
            super(v);

            v.setOnClickListener(this);

            viewShareToDoGroupIcon = v.findViewById(R.id.view_row_share_to_do_group_icon);
            viewShareToDoGroupTitle = v.findViewById(R.id.view_row_share_to_do_group_title);
            viewShareToDoGroupIconRight = v.findViewById(
                    R.id.view_row_share_to_do_group_icon_right);
        }

        @Override
        public void onClick(View v) {
            shareToDoGroupClickListener.onToDoGroupClicked(v, this.getLayoutPosition());
        }
    }

    public ShareToDoGroupDialogAdapter(List<ToDoGroup> toDoGroupList, Context context,
                                       ShareToDoGroupDialogFragment.ShareToDoGroupClickListener
                                               shareToDoGroupClickListener)
    {
        this.toDoGroupList = toDoGroupList;
        this.context = context;
        this.shareToDoGroupClickListener = shareToDoGroupClickListener;
    }

    @NonNull
    @Override
    public ShareToDoGroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(parent.getContext()).inflate(
                R.layout.row_share_to_do_group, parent, false);

        return new ShareToDoGroupViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShareToDoGroupViewHolder holder, final int position) {

        ToDoGroup toDoGroup = toDoGroupList.get(position);

        holder.viewShareToDoGroupIcon.setImageDrawable(ContextCompat.getDrawable(context,
                context.getResources().getIdentifier(toDoGroup.getIconName(),
                        "drawable", context.getPackageName())));
        holder.viewShareToDoGroupIcon.setColorFilter(ContextCompat.getColor(
                context, R.color.greyDim));

        holder.viewShareToDoGroupTitle.setText(toDoGroup.getTitle());

        String numBoxIconString = selectNumBoxIconName(toDoGroup);
        int boxIconId = context.getResources().getIdentifier(numBoxIconString, "drawable",
                context.getPackageName());

        holder.viewShareToDoGroupIconRight.setImageResource(boxIconId);
        holder.viewShareToDoGroupIconRight.setColorFilter(ContextCompat.getColor(context,
                R.color.grey));
    }

    @Override
    public int getItemCount() {
        return toDoGroupList.size();
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
