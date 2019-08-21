package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ui.main.ToDoFragment;

public class DeleteToDoGroupDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.DeleteToDoGroupDialogFragment";

    public interface DeleteToDoGroupDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    DeleteToDoGroupDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (DeleteToDoGroupDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(ToDoFragment.TAG
                    + " must implement DeleteToDoGroupDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        Bundle bundle = getArguments();
        String groupTitle = bundle.getString("title");

        builder.setTitle(R.string.dialog_delete_to_do_group_title)
                .setMessage("Are you sure you want to delete " + groupTitle + "?\n\nThis action cannot be undone!")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(DeleteToDoGroupDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(DeleteToDoGroupDialogFragment.this);
                    }
                });
        return builder.create();
    }

}
