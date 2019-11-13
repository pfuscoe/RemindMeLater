package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ui.main.ToDoFragment;

/**
 * Dialog that handles UI for adding new to do group (aka to do list)
 */
public class AddToDoGroupDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.AddToDoGroupDialogFragment";

    public interface AddToDoGroupDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    AddToDoGroupDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            // Use getTargetFragment when dialog opened from fragment
            listener = (AddToDoGroupDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(ToDoFragment.TAG
                    + " must implement AddToDoGroupDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_add_to_do_group, null))
                .setTitle(R.string.dialog_add_to_do_group_title)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(AddToDoGroupDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(AddToDoGroupDialogFragment.this);
                    }
                });
        return builder.create();
    }

}
