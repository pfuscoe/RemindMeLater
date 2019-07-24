package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerDialogFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {

    private OnDateSetListener dateSetListener;

    public interface OnDateSetListener {
        void onDateSet(DatePicker view, int year, int month, int day);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            dateSetListener = (OnDateSetListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnDateSetListener.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(requireActivity(),
                this, year, month, day);

        // Allow only future dates to be chosen
        Calendar today = Calendar.getInstance();
        //yesterday.add(Calendar.DATE, -1);

        datePickerDialog.getDatePicker().setMinDate(today.getTimeInMillis());

        return datePickerDialog;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        // Do something with the date chosen by the user
        // Setup date set action
        if (dateSetListener != null)
        {
            dateSetListener.onDateSet(view, year, month, day);
        }
        // LocalDate localDate = LocalDate.of(year, month, day);
        // setRecurrenceDate(localDate);
    }


}