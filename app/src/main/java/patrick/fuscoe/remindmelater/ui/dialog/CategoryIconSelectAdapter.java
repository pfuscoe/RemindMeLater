package patrick.fuscoe.remindmelater.ui.dialog;

import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

public class CategoryIconSelectAdapter extends RecyclerView.Adapter<CategoryIconSelectAdapter.IconViewHolder> {

    private List<Integer> categoryIconList;
    private Context context;
    private int selectedIcon;

    public static class IconViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ConstraintLayout viewGridItemLayout;
        

    }

}
