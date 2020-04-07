package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;

/**
 * Dialog that handles UI for adding new individual to do items
 */
public class AddToDoItemDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.AddToDoItemDialogFragment";

    private EditText viewItemName;

    public interface AddToDoItemDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    AddToDoItemDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddToDoItemDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(ToDoItemListActivity.TAG
                    + " must implement AddToDoItemDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_to_do_item, null);

        viewItemName = v.findViewById(R.id.dialog_add_to_do_item_name);

        builder.setView(v)
                .setTitle(R.string.dialog_add_to_do_item_title)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(AddToDoItemDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(AddToDoItemDialogFragment.this);
                    }
                });
        return builder.create();
    }

    @Override
    public void onStart() {
        super.onStart();

        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null)
        {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String categoryNameString = viewItemName.getText().toString();
                    if (categoryNameString.equals(""))
                    {
                        viewItemName.setError("Please enter an item name.");
                    }
                    else
                    {
                        listener.onDialogPositiveClick(AddToDoItemDialogFragment.this);
                    }
                }
            });
        }
    }
}
