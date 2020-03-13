package patrick.fuscoe.remindmelater.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import patrick.fuscoe.remindmelater.FriendsActivity;
import patrick.fuscoe.remindmelater.R;
import patrick.fuscoe.remindmelater.models.ReminderItem;

/**
 * Dialog that handles UI for sending (copying) a reminder to a friend
 */
public class SendReminderDialogFragment extends DialogFragment {

    public static final String TAG = "patrick.fuscoe.remindmelater.SendReminderDialogFragment";

    private List<ReminderItem> reminderItemList;
    private ReminderItem selectedReminder;

    private RecyclerView sendReminderRecycler;
    private RecyclerView.LayoutManager sendReminderRecyclerLayoutManager;
    private RecyclerView.Adapter sendReminderRecyclerAdapter;

    SendReminderDialogListener listener;
    FriendsActivity.SendReminderSelectedListener sendReminderSelectedListener;


    public interface SendReminderDialogListener {
        void onDialogNegativeClick(DialogFragment dialogFragment);
    }

    public interface SendReminderClickListener {
        void onSendReminderClicked(View v, int position);
    }

    private SendReminderClickListener sendReminderClickListener = new SendReminderClickListener() {
        @Override
        public void onSendReminderClicked(View v, int position) {
            selectedReminder = reminderItemList.get(position);
            sendReminderSelectedListener.onReminderSelected(
                    SendReminderDialogFragment.this, selectedReminder);
        }
    };

    public SendReminderDialogFragment(List<ReminderItem> reminderItemList,
                                      FriendsActivity.SendReminderSelectedListener
                                              sendReminderSelectedListener)
    {
        this.reminderItemList = reminderItemList;
        this.sendReminderSelectedListener = sendReminderSelectedListener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (SendReminderDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException("Host must implement SendReminderDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.dialog_send_reminder, null);

        String dialogTitle = getString(R.string.dialog_send_reminder_title);

        sendReminderRecycler = v.findViewById(R.id.dialog_send_reminder_recycler);
        sendReminderRecycler.setHasFixedSize(true);

        sendReminderRecyclerLayoutManager = new LinearLayoutManager(getContext());
        sendReminderRecycler.setLayoutManager(sendReminderRecyclerLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(
                sendReminderRecycler.getContext(), DividerItemDecoration.VERTICAL);
        sendReminderRecycler.addItemDecoration(dividerItemDecoration);

        
    }
}
