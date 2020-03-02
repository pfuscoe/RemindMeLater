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

        // TODO: setup image handling for icon-right
    }

    @Override
    public int getItemCount() {
        return toDoGroupList.size();
    }
}
