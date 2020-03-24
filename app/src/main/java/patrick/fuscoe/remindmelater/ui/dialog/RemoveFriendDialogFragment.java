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
import androidx.fragment.app.DialogFragment;

import patrick.fuscoe.remindmelater.FriendsActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.Friend;

/**
 * Dialog to confirm removing a friend
 */
public class RemoveFriendDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.RemoveFriendDialogFragment";

    private Friend friend;

    public interface RemoveFriendDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    RemoveFriendDialogListener listener;

    public RemoveFriendDialogFragment(Friend friend)
    {
        this.friend = friend;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (RemoveFriendDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(FriendsActivity.TAG
                    + " must implement RemoveFriendDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_confirm_basic, null);

        String confirmText = "Are you sure you want to remove " + friend.getFriendDisplayName() +
                " as a friend?";

        TextView viewConfirmText = v.findViewById(R.id.view_confirm_basic_text);
        viewConfirmText.setText(confirmText);

        builder.setView(v)
                .setTitle(R.string.dialog_remove_friend)
                .setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(RemoveFriendDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(RemoveFriendDialogFragment.this);
                    }
                });
        return builder.create();
    }

}
