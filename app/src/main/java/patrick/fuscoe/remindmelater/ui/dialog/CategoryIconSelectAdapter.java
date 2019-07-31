package patrick.fuscoe.remindmelater.ui.dialog;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import java.util.List;

import patrick.fuscoe.remindmelater.R;

public class CategoryIconSelectAdapter extends RecyclerView.Adapter<CategoryIconSelectAdapter.IconViewHolder> {

    private List<Integer> categoryIconList;
    private Context context;
    private int selectedIcon;
    private int selectedIconPos;

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

        }
    }

}
