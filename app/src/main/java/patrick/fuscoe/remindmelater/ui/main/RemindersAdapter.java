package patrick.fuscoe.remindmelater.ui.main;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import patrick.fuscoe.remindmelater.models.ReminderItem;

public class RemindersAdapter extends RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder> {

    private List<ReminderItem> reminderItemList;
    private Context context;

    private static RemindersFragment.ReminderClickListener reminderClickListener;

    public static class ReminderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {



    }

}
