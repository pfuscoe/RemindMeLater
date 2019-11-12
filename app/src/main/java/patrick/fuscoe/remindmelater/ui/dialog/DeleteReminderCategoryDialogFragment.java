package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ReminderCategoriesActivity;

/**
 * Dialog that handles UI for confirming delete of reminder categories
 */
public class DeleteReminderCategoryDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.DeleteReminderCategoryDialogFragment";

    public interface DeleteReminderCategoryDialogListener {
        void onDialogPositiveClick(DialogFragment dialogFragment);
        void onDialogNegativeClick(DialogFragment dialogFragment);
    }

    DeleteReminderCategoryDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (DeleteReminderCategoryDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(ReminderCategoriesActivity.TAG +
                    " must implement DeleteReminderCategoryDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle bundle = getArguments();
        String reminderCategoryTitle = bundle.getString("title");

        builder.setTitle(R.string.dialog_delete_reminder_category_title)
                .setMessage("Are you sure you want to delete " + reminderCategoryTitle + "?\n\nThis action cannot be undone!")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(DeleteReminderCategoryDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(DeleteReminderCategoryDialogFragment.this);
                    }
                });

        return builder.create();
    }
}
