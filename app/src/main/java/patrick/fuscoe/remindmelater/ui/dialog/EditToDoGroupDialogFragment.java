package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_to_do_group, null);

        Bundle bundle = getArguments();
        String groupTitle = bundle.getString("title");
        int groupIconId = bundle.getInt("iconId");

        EditText viewGroupTitle = v.findViewById(R.id.dialog_add_to_do_group_title);
        viewGroupTitle.setText(groupTitle);

        builder.setView(v)
                .setTitle(R.string.dialog_edit_to_do_group_title)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
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
