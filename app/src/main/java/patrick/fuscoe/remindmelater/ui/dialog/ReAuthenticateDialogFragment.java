package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import patrick.fuscoe.remindmelater.R;

public class ReAuthenticateDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReAuthenticateDialogFragment";

    private TextView viewDescription;
    private TextView viewNote;

    ReAuthenticateDialogListener listener;

    public interface ReAuthenticateDialogListener {
        void onDialogPositiveClick(DialogFragment dialogFragment);
        void onDialogNegativeClick(DialogFragment dialogFragment);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ReAuthenticateDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Host must implement ReAuthenticateDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_re_authenticate, null);

        viewDescription = v.findViewById(R.id.dialog_re_authenticate_description);
        viewNote = v.findViewById(R.id.dialog_re_authenticate_note);

        String dialogTitle = "User Authentication";
        String description = "This action requires re-authentication";
        String note = "Forgot email or password? Logout and click 'Forgot Password'";
        
        viewDescription.setText(description);
        viewNote.setText(note);

        builder.setView(v)
                .setTitle(dialogTitle)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(ReAuthenticateDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(ReAuthenticateDialogFragment.this);
                    }
                });

        AlertDialog dialog = builder.create();

        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
}
