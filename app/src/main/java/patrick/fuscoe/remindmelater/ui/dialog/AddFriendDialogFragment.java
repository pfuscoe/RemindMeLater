package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import patrick.fuscoe.remindmelater.FriendsActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.ToDoItemListActivity;

/**
 * Dialog that handles UI for adding new friends
 */
public class AddFriendDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.AddFriendDialogFragment";

    public interface AddFriendDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    AddFriendDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddFriendDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(FriendsActivity.TAG
                    + " must implement AddToDoItemDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_add_friend, null))
                .setTitle(R.string.dialog_add_new_friend)
                .setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(AddFriendDialogFragment.this);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(AddFriendDialogFragment.this);
                    }
                });
        return builder.create();
    }

}
