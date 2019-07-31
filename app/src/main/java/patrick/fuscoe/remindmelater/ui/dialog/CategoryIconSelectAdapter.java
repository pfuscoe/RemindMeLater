package patrick.fuscoe.remindmelater.ui.dialog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;

public class CategoryIconSelectAdapter extends RecyclerView.Adapter<CategoryIconSelectAdapter.IconViewHolder> {

    private List<Integer> categoryIconList;
    private List<Boolean> categoryIconListIsChecked;
    private Context context;
    private int selectedIcon;
    private int selectedIconPos;

    private static AddReminderCategoryDialogFragment.CategoryIconClickListener categoryIconClickListener;

    public static class IconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewCategoryItemLayout;
        ImageView viewCategoryIcon;
        ImageView viewCategoryIconCheckbox;

        IconViewHolder(View v)
        {
            super(v);

            v.setOnClickListener(this);

            viewCategoryItemLayout = v.findViewById(R.id.grid_item_icon_select_layout);
            viewCategoryIcon = v.findViewById(R.id.grid_item_icon);
            viewCategoryIconCheckbox = v.findViewById(R.id.grid_item_checkbox);
        }

        @Override
        public void onClick(View v) {
            categoryIconClickListener.onIconClicked(v, this.getLayoutPosition());
        }
    }

    public CategoryIconSelectAdapter(List<Integer> categoryIconList, Context context,
                                     AddReminderCategoryDialogFragment.CategoryIconClickListener categoryIconClickListener)
    {
        this.categoryIconList = categoryIconList;
        this.context = context;
        this.categoryIconClickListener = categoryIconClickListener;
    }

    @NonNull
    @Override
    public IconViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        ConstraintLayout v = (ConstraintLayout) LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.grid_item_icon_select, viewGroup, false);

        return new IconViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IconViewHolder holder, final int position) {

        Integer categoryIconId = categoryIconList.get(position);

        holder.viewCategoryIcon.setImageResource(categoryIconId);

        if (categoryIconListIsChecked.get(position))
        {
            holder.viewCategoryIconCheckbox.setImageResource(R.drawable.checkbox_marked);
            holder.viewCategoryIconCheckbox.setColorFilter(context.getColor(R.color.blueDark));
        }
        else
        {
            holder.viewCategoryIconCheckbox.setImageResource(R.drawable.checkbox_blank_outline);
            holder.viewCategoryIconCheckbox.setColorFilter(context.getColor(R.color.greyLight));
        }
    }

    @Override
    public int getItemCount() {
        return categoryIconList.size();
    }
}
