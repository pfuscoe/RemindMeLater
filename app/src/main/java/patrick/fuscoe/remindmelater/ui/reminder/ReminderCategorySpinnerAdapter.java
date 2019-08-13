package patrick.fuscoe.remindmelater.ui.reminder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderCategory;

public class ReminderCategorySpinnerAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;

    private List<ReminderCategory> reminderCategories;

    public ReminderCategorySpinnerAdapter(Context context, Map<String, Integer> reminderCategoriesMap)
    {
        this.context = context;
        buildReminderCategoryList(reminderCategoriesMap);

        inflater = LayoutInflater.from(context);
    }

    public void buildReminderCategoryList(Map<String, Integer> reminderCategoriesMap)
    {
        List<ReminderCategory> reminderCategoryList = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : reminderCategoriesMap.entrySet())
        {
            ReminderCategory reminderCategory = new ReminderCategory(entry.getKey(), entry.getValue());
            reminderCategoryList.add(reminderCategory);
        }

        Collections.sort(reminderCategoryList);
        reminderCategories = reminderCategoryList;
    }

    @Override
    public int getCount() {
        return reminderCategories.size();
    }

    @Override
    public Object getItem(int position) {
        //return null;
        return reminderCategories.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = inflater.inflate(R.layout.spinner_row_category_select, null);
        ImageView categoryIcon = convertView.findViewById(R.id.spinner_row_category_select_icon);
        TextView categoryName = convertView.findViewById(R.id.spinner_row_category_select_name);

        ReminderCategory reminderCategory = reminderCategories.get(position);

        categoryIcon.setImageResource(reminderCategory.getIconId());
        categoryName.setText(reminderCategory.getCategoryName());

        return convertView;
    }

    public List<ReminderCategory> getReminderCategories() {
        return reminderCategories;
    }
}
