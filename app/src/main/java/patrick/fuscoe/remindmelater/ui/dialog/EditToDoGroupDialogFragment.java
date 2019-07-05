package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ui.main.ToDoFragment;

public class EditToDoGroupDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.EditToDoGroupDialogFragment";

    public interface EditToDoGroupDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    EditToDoGroupDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (EditToDoGroupDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(ToDoFragment.TAG
                    + " must implement EditToDoGroupDialogListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_add_to_do_group, null))
                .setTitle(R.string.dialog_edit_to_do_group_title)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(EditToDoGroupDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(EditToDoGroupDialogFragment.this);
                    }
                });
        return builder.create();
    }

}
