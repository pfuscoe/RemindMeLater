package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import patrick.fuscoe.remindmelater.R;

public class ReAuthenticateDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.ReAuthenticateDialogFragment";

    private TextView viewDescription;
    private TextView viewNote;
    private EditText viewEmail;
    private EditText viewPassword;

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
        viewEmail = v.findViewById(R.id.dialog_re_authenticate_email);
        viewPassword = v.findViewById(R.id.dialog_re_authenticate_password);

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
                        //listener.onDialogPositiveClick(ReAuthenticateDialogFragment.this);
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
    public void onStart() {
        super.onStart();

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null)
        {
            Button positiveButton = dialog.getButton(Dialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String email = viewEmail.getText().toString();
                    String password = viewPassword.getText().toString();

                    if (email.equals(""))
                    {
                        viewEmail.setError("Please enter an email.");
                    }
                    else if (password.equals(""))
                    {
                        viewPassword.setError("Please enter a password.");
                    }
                    else
                    {
                        listener.onDialogPositiveClick(ReAuthenticateDialogFragment.this);
                        dialog.cancel();
                    }
                }
            });
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }
}
