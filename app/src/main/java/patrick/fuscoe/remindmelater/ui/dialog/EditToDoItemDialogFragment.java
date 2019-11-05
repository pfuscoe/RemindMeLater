package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;

public class EditToDoItemDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.EditToDoItemDialogFragment";

    private RadioButton viewPriorityRadioHigh;
    private RadioButton viewPriorityRadioMedium;
    private RadioButton viewPriorityRadioLow;

    public interface EditToDoItemDialogListener {
        void onDialogPositiveClick(DialogFragment dialogFragment);
        void onDialogNegativeClick(DialogFragment dialogFragment);
    }

    EditToDoItemDialogListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (EditToDoItemDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(ToDoItemListActivity.TAG
                    + " must implement EditToDoItemDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_add_to_do_item, null);

        Bundle bundle = getArguments();
        String itemName = bundle.getString("itemName");
        int priority = bundle.getInt("priority");

        Log.d(TAG, "itemName: " + itemName + ", priority: " + priority);

        EditText viewItemName = v.findViewById(R.id.dialog_add_to_do_item_name);
        viewItemName.setText(itemName);

        //RadioGroup viewPriorityRadioGroup = v.findViewById(R.id.dialog_add_to_do_item_radio_group);
        viewPriorityRadioHigh = v.findViewById(R.id.dialog_add_to_do_item_radio_high);
        viewPriorityRadioMedium = v.findViewById(R.id.dialog_add_to_do_item_radio_medium);
        viewPriorityRadioLow = v.findViewById(R.id.dialog_add_to_do_item_radio_low);

        setPriorityRadioButton(priority);

        builder.setView(inflater.inflate(R.layout.dialog_add_to_do_item, null))
                .setTitle(R.string.dialog_edit_to_do_item_title)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(EditToDoItemDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(EditToDoItemDialogFragment.this);
                    }
                });

        return builder.create();
    }

    private void setPriorityRadioButton(int priority)
    {
        switch (priority)
        {
            case 1:
                viewPriorityRadioHigh.setChecked(true);
                return;

            case 2:
                viewPriorityRadioMedium.setChecked(true);
                return;

            case 3:
                viewPriorityRadioLow.setChecked(true);
                return;

            default:
                viewPriorityRadioHigh.setChecked(true);
                return;
        }
    }
}
