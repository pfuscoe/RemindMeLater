package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import java.util.List;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.CategoryIconSet;

public class AddReminderCategoryDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.AddReminderCategoryDialogFragment";

    private List<Integer> categoryIconList;
    private List<Boolean> categoryIconListIsChecked;
    private int selectedIcon;
    private int selectedIconPos;

    public interface CategoryIconClickListener {
        void onIconClicked(View v, int position);
    }

    private CategoryIconClickListener categoryIconClickListener = new CategoryIconClickListener() {
        @Override
        public void onIconClicked(View v, int position) {
            // TODO: implement icon clicked action (check the box.., notifyItemChanged)

        }
    };

    AddReminderCategoryDialogListener listener;

    public interface AddReminderCategoryDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddReminderCategoryDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException("Host must implement AddReminderCategoryDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_category_edit, null);

        CategoryIconSet categoryIconSet = new CategoryIconSet();
        categoryIconList = categoryIconSet.getCategoryIconList();
        categoryIconListIsChecked = categoryIconSet.getCategoryIconListIsChecked();

        //Bundle bundle = getArguments();

        EditText viewCategoryName = v.findViewById(R.id.dialog_category_edit_name);

        // TODO: Setup recycler and adapter

        builder.setView(v)
                .setTitle(R.string.dialog_add_reminder_category_title)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(AddReminderCategoryDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(AddReminderCategoryDialogFragment.this);
                    }
                });
        return builder.create();
    }

}
