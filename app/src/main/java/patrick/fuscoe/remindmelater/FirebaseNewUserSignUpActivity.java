package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;

public class FirebaseNewUserSignUpActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.FirebaseNewUserSignUpActivity";

    private FirebaseAuth auth;

    private EditText viewEmail;
    private EditText viewPassword;
    private EditText viewDisplayName;
    private Button btnSignUp;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.btn_new_user_sign_up_sign_up:
                    signUp();
                    return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_firebase_new_user_sign_up);

        viewEmail = findViewById(R.id.view_new_user_sign_up_email);
        viewPassword = findViewById(R.id.view_new_user_sign_up_password);
        viewDisplayName = findViewById(R.id.view_new_user_sign_up_display_name);
        btnSignUp = findViewById(R.id.btn_new_user_sign_up_sign_up);

        btnSignUp.setOnClickListener(onClickListener);
    }

    private void signUp()
    {

    }
}
