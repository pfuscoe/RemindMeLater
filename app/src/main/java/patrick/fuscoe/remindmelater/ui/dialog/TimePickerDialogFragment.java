package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * Dialog that handles UI for picking time of day
 */
public class TimePickerDialogFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    private OnTimeSetListener timeSetListener;

    public interface OnTimeSetListener {
        void onTimeSet(TimePicker view, int hourOfDay, int minute);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            timeSetListener = (OnTimeSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnTimeSetListener.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        Bundle bundle = getArguments();

        int hourOfDay = bundle.getInt("hourOfDay");
        int minute = bundle.getInt("minute");

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireActivity(), this,
                hourOfDay, minute,false);

        return timePickerDialog;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        if (timeSetListener != null)
        {
            timeSetListener.onTimeSet(view, hourOfDay, minute);
        }
    }
}
