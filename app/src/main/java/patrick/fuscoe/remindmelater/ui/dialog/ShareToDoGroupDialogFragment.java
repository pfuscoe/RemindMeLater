package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import patrick.fuscoe.remindmelater.FriendsActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.Friend;
import patrick.fuscoe.remindmelater.models.ToDoGroup;

/**
 * Dialog that handles UI for sharing a to do group (aka to do list) with a friend
 */
public class ShareToDoGroupDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.ShareToDoGroupDialogFragment";

    private List<ToDoGroup> toDoGroupList;
    private ToDoGroup selectedToDoGroup;
    private Friend friend;

    private RecyclerView shareToDoGroupRecycler;
    private RecyclerView.LayoutManager shareToDoGroupRecyclerLayoutManager;
    private RecyclerView.Adapter shareToDoGroupRecyclerAdapter;

    ShareToDoGroupDialogListener listener;
    FriendsActivity.ShareToDoGroupSelectedListener shareToDoGroupSelectedListener;

    public interface ShareToDoGroupClickListener {
        void onToDoGroupClicked(View v, int position);
    }

    public interface ShareToDoGroupDialogListener {
        //void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    private ShareToDoGroupClickListener shareToDoGroupClickListener = new ShareToDoGroupClickListener() {
        @Override
        public void onToDoGroupClicked(View v, int position) {
            selectedToDoGroup = toDoGroupList.get(position);
            shareToDoGroupSelectedListener.onToDoGroupSelected(
                    ShareToDoGroupDialogFragment.this, selectedToDoGroup);
        }
    };

    public ShareToDoGroupDialogFragment(Friend friend, List<ToDoGroup> toDoGroupList,
                                        FriendsActivity.ShareToDoGroupSelectedListener
                                                shareToDoGroupSelectedListener)
    {
        this.friend = friend;
        this.toDoGroupList = toDoGroupList;
        this.shareToDoGroupSelectedListener = shareToDoGroupSelectedListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (ShareToDoGroupDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Host must implement ShareToDoGroupDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_share_to_do_group, null);

        String dialogTitle = getString(R.string.dialog_share_to_do_group_title) + " with " + 
                friend.getFriendDisplayName();

        shareToDoGroupRecycler = v.findViewById(R.id.dialog_share_to_do_group_recycler);
        shareToDoGroupRecycler.setHasFixedSize(true);

        shareToDoGroupRecyclerLayoutManager = new LinearLayoutManager(getContext());
        shareToDoGroupRecycler.setLayoutManager(shareToDoGroupRecyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                shareToDoGroupRecycler.getContext(), DividerItemDecoration.VERTICAL);
        shareToDoGroupRecycler.addItemDecoration(dividerItemDecoration);

        shareToDoGroupRecyclerAdapter = new ShareToDoGroupDialogAdapter(toDoGroupList,
                getContext(), shareToDoGroupClickListener);
        shareToDoGroupRecycler.setAdapter(shareToDoGroupRecyclerAdapter);

        builder.setView(v)
                .setTitle(dialogTitle)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogNegativeClick(ShareToDoGroupDialogFragment.this);
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
