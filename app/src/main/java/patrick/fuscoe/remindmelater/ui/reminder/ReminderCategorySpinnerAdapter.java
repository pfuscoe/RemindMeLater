package patrick.fuscoe.remindmelater.ui.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.List;

import patrick.fuscoe.remindmelater.models.ReminderCategory;

public class ReminderCategorySpinnerAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private List<ReminderCategory> reminderCategories;

    @Override
    public int getCount() {
        return 0;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}
