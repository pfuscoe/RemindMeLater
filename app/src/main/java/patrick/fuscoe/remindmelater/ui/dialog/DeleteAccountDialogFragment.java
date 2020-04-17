package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.UserPreferencesActivity;
import patrick.fuscoe.remindmelater.models.UserProfile;

public class DeleteAccountDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.DeleteAccountDialogFragment";

    private UserProfile userProfile;

    public interface DeleteAccountDialogListener {
        void onDialogPositiveClick(DialogFragment dialogFragment);
        void onDialogNegativeClick(DialogFragment dialogFragment);
    }

    DeleteAccountDialogListener listener;

    public DeleteAccountDialogFragment(UserProfile userProfile)
    {
        this.userProfile = userProfile;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (DeleteAccountDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(UserPreferencesActivity.TAG + " must implement " +
                    "DeleteAccountDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_confirm_basic, null);

        String confirmText = "Are you sure you want to delete the account for " +
                userProfile.getDisplayName() + "?  This cannot be undone!";

        TextView viewConfirmText = v.findViewById(R.id.view_confirm_basic_text);
        viewConfirmText.setText(confirmText);

        builder.setView(v)
                .setTitle("Delete Account")
                .setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogPositiveClick(DeleteAccountDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listener.onDialogNegativeClick(DeleteAccountDialogFragment.this);
                    }
                });
        return builder.create();
    }
}
