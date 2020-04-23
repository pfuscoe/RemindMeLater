package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class UserFeedbackActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.UserFeedbackActivity";

    private TextView viewDescription;
    private RadioGroup radioGroupUserFeedback;
    private RadioButton radioReportBug;
    private RadioButton radioOtherFeedback;
    private EditText viewFeedbackField;
    private TextView viewRateAppLink;
    private Button btnSendFeedback;

    private View.OnClickListener btnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.view_user_feedback_store_page_link:
                    openStorePage();
                    return;

                case R.id.button_user_feedback_send:
                    sendFeedback();
                    return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_feedback);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewDescription = findViewById(R.id.view_user_feedback_description);
        radioGroupUserFeedback = findViewById(R.id.radio_group_user_feedback);
        radioReportBug = findViewById(R.id.radio_button_user_feedback_report_bug);
        radioOtherFeedback = findViewById(R.id.radio_button_user_feedback_other_feedback);
        viewFeedbackField = findViewById(R.id.view_user_feedback_field);
        viewRateAppLink = findViewById(R.id.view_user_feedback_store_page_link);
        btnSendFeedback = findViewById(R.id.button_user_feedback_send);

        viewRateAppLink.setOnClickListener(btnClickListener);
        btnSendFeedback.setOnClickListener(btnClickListener);
        viewDescription.setText(R.string.user_feedback_description);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Send Feedback");
    }

    // Prompt user to choose email client and auto-fill feedback
    private void sendFeedback()
    {
        int radioCheckedId = radioGroupUserFeedback.getCheckedRadioButtonId();
        String feedbackSubject = processRadioCheckedId(radioCheckedId);
        String feedbackText = viewFeedbackField.getText().toString();

        Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
        selectorIntent.setData(Uri.parse("mailto:"));

        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"info@patrickfuscoe.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, feedbackSubject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, feedbackText);
        emailIntent.setSelector(selectorIntent);
        startActivity(Intent.createChooser(emailIntent, "Email the developer:"));
    }

    // Direct user to the Play Store page
    private void openStorePage()
    {
        Uri uri = Uri.parse("market://details?id=" + getPackageName());
        Intent storePageIntent = new Intent(Intent.ACTION_VIEW, uri);
        try {
            startActivity(storePageIntent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Cannot find app in Play Store",
                    Toast.LENGTH_LONG).show();
        }
    }

    private String processRadioCheckedId(int radioCheckedId)
    {
        switch (radioCheckedId)
        {
            case R.id.radio_button_user_feedback_report_bug:
                return getResources().getString(R.string.user_feedback_radio_label_report_bug);

            case R.id.radio_button_user_feedback_other_feedback:
                return getResources().getString(R.string.user_feedback_radio_label_other_feedback);

            default:
                return getResources().getString(R.string.user_feedback_radio_label_other_feedback);
        }
    }
}
