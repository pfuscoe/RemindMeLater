package patrick.fuscoe.remindmelater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class FirebaseNewUserSignUpActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.FirebaseNewUserSignUpActivity";
    public static final String DISPLAY_NAME = "patrick.fuscoe.remindmelater.DISPLAY_NAME";

    private FirebaseAuth auth;

    private ProgressBar viewProgressBar;
    private ImageView viewAppIcon;
    private TextView viewAppTitle;
    private TextView viewAppAuthor;
    private EditText viewEmail;
    private EditText viewPassword;
    private EditText viewVerifyPassword;
    //private EditText viewDisplayName;
    private CheckBox viewPrivacyTosCheckbox;
    private TextView viewPrivacyPolicy;
    private TextView viewTos;
    private Button btnSignUp;
    private TextView viewCopyright;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.view_new_user_sign_up_privacy_policy:
                    openPrivacyPolicy();
                    return;

                case R.id.view_new_user_sign_up_tos:
                    openTos();
                    return;

                case R.id.btn_new_user_sign_up_sign_up:
                    signUp();
                    return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_firebase_new_user_sign_up);

        viewProgressBar = findViewById(R.id.view_new_user_sign_up_progress_bar);
        viewAppIcon = findViewById(R.id.view_new_user_sign_up_app_icon);
        viewAppTitle = findViewById(R.id.view_new_user_sign_up_app_title);
        viewAppAuthor = findViewById(R.id.view_new_user_sign_up_app_author);
        viewEmail = findViewById(R.id.view_new_user_sign_up_email);
        viewPassword = findViewById(R.id.view_new_user_sign_up_password);
        viewVerifyPassword = findViewById(R.id.view_new_user_sign_up_verify_password);
        //viewDisplayName = findViewById(R.id.view_new_user_sign_up_display_name);
        viewPrivacyTosCheckbox = findViewById(R.id.view_new_user_sign_up_privacy_tos_checkbox);
        viewPrivacyPolicy = findViewById(R.id.view_new_user_sign_up_privacy_policy);
        viewTos = findViewById(R.id.view_new_user_sign_up_tos);
        btnSignUp = findViewById(R.id.btn_new_user_sign_up_sign_up);
        viewCopyright = findViewById(R.id.view_new_user_sign_up_copyright);

        viewPrivacyPolicy.setOnClickListener(onClickListener);
        viewTos.setOnClickListener(onClickListener);
        btnSignUp.setOnClickListener(onClickListener);


    }

    private void signUp()
    {
        String email = viewEmail.getText().toString();
        String password = viewPassword.getText().toString();
        String verifyPassword = viewVerifyPassword.getText().toString();
        //final String displayName = viewDisplayName.getText().toString();

        if (email.equals(""))
        {
            Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.equals(""))
        {
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_LONG).show();
            return;
        }

        if (!verifyPassword.equals(password))
        {
            Toast.makeText(this, "Passwords do not match: Please verify that you entered the correct password", Toast.LENGTH_LONG).show();
            return;
        }

        /*
        if (displayName.equals(""))
        {
            Toast.makeText(this, "Please enter a display name", Toast.LENGTH_LONG).show();
            return;
        }
        */

        if (!viewPrivacyTosCheckbox.isChecked())
        {
            Toast.makeText(this, "You must have read the Privacy Policy and agree to the Terms of Service before signing up", Toast.LENGTH_LONG).show();
            return;
        }

        showProgressBar();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            /*
                            Intent intent = new Intent(FirebaseNewUserSignUpActivity.this, MainActivity.class);
                            intent.putExtra(FirebaseSignInActivity.CHECK_IF_NEW_USER, true);
                            intent.putExtra(DISPLAY_NAME, displayName);
                            startActivity(intent);
                            finish();
                            */
                            //updateDisplayName();
                            checkEmailVerified();
                        } else {
                            // If sign in fails, display a message to the user.
                            hideProgressBar();
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(FirebaseNewUserSignUpActivity.this, "Authentication failed: " +
                                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    public void openPrivacyPolicy()
    {
        Intent intent = new Intent(this, PrivacyPolicyActivity.class);
        startActivity(intent);
    }

    public void openTos()
    {
        Intent intent = new Intent(this, TermsOfServiceActivity.class);
        startActivity(intent);
    }

    /*
    public void updateDisplayName()
    {
        String displayName = viewDisplayName.getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName).build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Display Name added to Firebase user profile.");
                        }

                        checkEmailVerified();
                    }
                });
    }
    */

    public void checkEmailVerified()
    {
        //String displayName = viewDisplayName.getText().toString();

        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user.isEmailVerified())
        {
            hideProgressBar();
            Intent intent = new Intent(FirebaseNewUserSignUpActivity.this, MainActivity.class);
            intent.putExtra(FirebaseSignInActivity.CHECK_IF_NEW_USER, true);
            //intent.putExtra(DISPLAY_NAME, displayName);
            startActivity(intent);
            finish();
        }
        else
        {
            user.sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "Email verification sent.");
                                Toast.makeText(FirebaseNewUserSignUpActivity.this, "Please check your email to verify your account", Toast.LENGTH_LONG).show();
                                logoutUser();
                            }
                            else
                            {
                                hideProgressBar();
                                Log.w(TAG, "Failed to send verification email", task.getException());
                                Toast.makeText(FirebaseNewUserSignUpActivity.this, "Failed to send verification email. Please contact support", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    public void logoutUser()
    {
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressBar();
                        Intent intent = new Intent(FirebaseNewUserSignUpActivity.this, FirebaseSignInActivity.class);
                        startActivity(intent);
                        finishAffinity();
                    }
                });
    }

    private void showProgressBar()
    {
        viewProgressBar.setVisibility(View.VISIBLE);

        viewAppIcon.setVisibility(View.INVISIBLE);
        viewAppTitle.setVisibility(View.INVISIBLE);
        viewAppAuthor.setVisibility(View.INVISIBLE);
        viewEmail.setVisibility(View.INVISIBLE);
        viewPassword.setVisibility(View.INVISIBLE);
        viewVerifyPassword.setVisibility(View.INVISIBLE);
        viewPrivacyTosCheckbox.setVisibility(View.INVISIBLE);
        viewPrivacyPolicy.setVisibility(View.INVISIBLE);
        viewTos.setVisibility(View.INVISIBLE);
        btnSignUp.setVisibility(View.INVISIBLE);
        viewCopyright.setVisibility(View.INVISIBLE);
    }

    private void hideProgressBar()
    {
        viewProgressBar.setVisibility(View.INVISIBLE);

        viewAppIcon.setVisibility(View.VISIBLE);
        viewAppTitle.setVisibility(View.VISIBLE);
        viewAppAuthor.setVisibility(View.VISIBLE);
        viewEmail.setVisibility(View.VISIBLE);
        viewPassword.setVisibility(View.VISIBLE);
        viewVerifyPassword.setVisibility(View.VISIBLE);
        viewPrivacyTosCheckbox.setVisibility(View.VISIBLE);
        viewPrivacyPolicy.setVisibility(View.VISIBLE);
        viewTos.setVisibility(View.VISIBLE);
        btnSignUp.setVisibility(View.VISIBLE);
        viewCopyright.setVisibility(View.VISIBLE);
    }

}
