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

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderCategory;
import patrick.fuscoe.remindmelater.ui.main.RemindersFragment;

public class ReminderCategorySpinnerAdapter extends BaseAdapter {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReminderCategorySpinnerAdapter";

    public static final String REMINDER_CATEGORY_ALL_ICON_NAME = "category_all_animation";

    private Context context;
    private LayoutInflater inflater;

    private List<ReminderCategory> reminderCategories;

    public ReminderCategorySpinnerAdapter(Context context, Map<String, String> reminderCategoriesMap)
    {
        this.context = context;
        buildReminderCategoryList(reminderCategoriesMap);

        inflater = LayoutInflater.from(context);
    }

    public void buildReminderCategoryList(Map<String, String> reminderCategoriesMap)
    {
        List<ReminderCategory> reminderCategoryList = new ArrayList<>();

        ReminderCategory reminderCategoryMain = new ReminderCategory(
                "Main", MainActivity.DEFAULT_REMINDER_CATEGORY_ICON_NAME);

        for (Map.Entry<String, String> entry : reminderCategoriesMap.entrySet())
        {
            ReminderCategory reminderCategory = new ReminderCategory(entry.getKey(), entry.getValue());

            if (!reminderCategory.getCategoryName().equals("Main"))
            {
                reminderCategoryList.add(reminderCategory);
            }
        }

        Collections.sort(reminderCategoryList);

        // Make 'Main' category show up on top (except for 'All')
        reminderCategoryList.add(0, reminderCategoryMain);

        // Add "All" category to front for use in filter spinner
        if (this.context instanceof MainActivity)
        {
            ReminderCategory reminderCategoryAll =
                    new ReminderCategory("All", REMINDER_CATEGORY_ALL_ICON_NAME);
            reminderCategoryList.add(0, reminderCategoryAll);
        }

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

        categoryIcon.setImageResource(context.getResources().getIdentifier(
                reminderCategory.getIconName(), "drawable", context.getPackageName()));
        categoryName.setText(reminderCategory.getCategoryName());

        return convertView;
    }

    public List<ReminderCategory> getReminderCategories() {
        return reminderCategories;
    }
}
