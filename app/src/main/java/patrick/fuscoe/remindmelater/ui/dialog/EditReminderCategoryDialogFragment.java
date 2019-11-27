package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.util.List;

import patrick.fuscoe.remindmelater.MainActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ReminderCategoriesActivity;
import patrick.fuscoe.remindmelater.models.CategoryIconSet;

/**
 * Dialog that handles UI for editing reminder categories
 */
public class EditReminderCategoryDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.EditReminderCategoryDialogFragment";

    private List<Integer> categoryIconList;
    private List<Boolean> categoryIconListIsChecked;
    private int selectedIcon;
    private int selectedIconPos;
    private String selectedIconName;

    private RecyclerView categoryIconRecycler;
    private RecyclerView.LayoutManager categoryIconRecyclerLayoutManager;
    private RecyclerView.Adapter categoryIconRecyclerAdapter;

    // Borrows listener type from to do group edit dialog to simplify reuse with icon select adapter
    private EditToDoGroupDialogFragment.CategoryIconClickListener categoryIconClickListener = new EditToDoGroupDialogFragment.CategoryIconClickListener() {
        @Override
        public void onIconClicked(View v, int position) {
            int oldPos = selectedIconPos;
            selectedIconPos = position;

            if (oldPos != -1)
            {
                categoryIconListIsChecked.set(oldPos, false);
            }

            if (oldPos == position)
            {
                selectedIcon = -1;
                selectedIconPos = -1;
                selectedIconName = MainActivity.DEFAULT_REMINDER_CATEGORY_ICON_NAME;
            }
            else
            {
                categoryIconListIsChecked.set(position, true);
                selectedIcon = categoryIconList.get(position);
                selectedIconName = getResources().getResourceEntryName(selectedIcon);

                // notify old pos changed
                categoryIconRecyclerAdapter.notifyItemChanged(oldPos);
            }

            // notify new pos changed
            categoryIconRecyclerAdapter.notifyItemChanged(position);
        }
    };

    public interface CategoryIconClickListener {
        void onIconClicked(View v, int position);
    }

    public interface EditReminderCategoryDialogListener {
        void onDialogPositiveClick(DialogFragment dialogFragment);
        void onDialogNegativeClick(DialogFragment dialogFragment);
    }

    EditReminderCategoryDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (EditReminderCategoryDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(ReminderCategoriesActivity.TAG +
                    " must implement EditReminderCategoryDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_category_edit, null);

        Bundle bundle = getArguments();
        String reminderCategoryName = bundle.getString("title");
        String reminderCategoryIconName = bundle.getString("iconName");

        EditText viewReminderCategoryName = v.findViewById(R.id.dialog_category_edit_name);
        viewReminderCategoryName.setText(reminderCategoryName);
        selectedIconName = reminderCategoryIconName;
        selectedIcon = getResources().getIdentifier(reminderCategoryIconName, "drawable",
                requireActivity().getPackageName());

        CategoryIconSet categoryIconSet = new CategoryIconSet();
        categoryIconSet.markSelected(selectedIcon);
        categoryIconList = categoryIconSet.getCategoryIconList();
        categoryIconListIsChecked = categoryIconSet.getCategoryIconListIsChecked();
        selectedIconPos = categoryIconSet.getSelectedIconPos();

        categoryIconRecycler = v.findViewById(R.id.dialog_category_edit_recycler);
        categoryIconRecycler.setHasFixedSize(true);

        categoryIconRecyclerLayoutManager = new GridLayoutManager(getContext(), 5);
        categoryIconRecycler.setLayoutManager(categoryIconRecyclerLayoutManager);

        categoryIconRecyclerAdapter = new EditCategoryIconSelectAdapter(categoryIconList, categoryIconListIsChecked, getContext(), categoryIconClickListener);
        categoryIconRecycler.setAdapter(categoryIconRecyclerAdapter);

        builder.setView(v)
                .setTitle(R.string.dialog_edit_reminder_category_title)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(EditReminderCategoryDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(EditReminderCategoryDialogFragment.this);
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
    }

    public String getSelectedIconName() {
        return selectedIconName;
    }
}
