package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ReminderDetailsActivity;
import patrick.fuscoe.remindmelater.ui.main.ToDoFragment;

public class DeleteReminderDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.DeleteReminderDialogFragment";

    public interface DeleteReminderDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    DeleteReminderDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DeleteReminderDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(ReminderDetailsActivity.TAG
                    + " must implement DeleteReminderDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle bundle = getArguments();
        String reminderTitle = bundle.getString("title");

        builder.setTitle("Delete Reminder")
                .setMessage("Are you sure you want to delete " + reminderTitle + "?\n\nThis action cannot be undone!")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(DeleteReminderDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(DeleteReminderDialogFragment.this);
                    }
                });
        return builder.create();
    }

}
