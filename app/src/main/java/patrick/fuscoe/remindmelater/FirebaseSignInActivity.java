package patrick.fuscoe.remindmelater;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;

public class FirebaseSignInActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.FirebaseSignInActivity";
    public static final String CHECK_IF_NEW_USER = "patrick.fuscoe.remindmelater.CHECK_IF_NEW_USER";
    private static final int RC_SIGN_IN = 223;

    public static final String USER_ID = "patrick.fuscoe.remindmelater.USER_ID";

    private FirebaseAuth auth;

    private EditText viewEmail;
    private EditText viewPassword;
    private TextView viewNewUserLink;
    private TextView viewForgotPasswordLink;
    private Button btnLogin;

    private boolean userMustEnterLoginInfo;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId())
            {
                case R.id.view_sign_in_new_user_link:
                    openNewUserSignUp();
                    return;

                case R.id.view_sign_in_forgot_password_link:
                    openForgotPassword();
                    return;

                case R.id.btn_sign_in_login:
                    loginReturningUser();
                    return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();

        userMustEnterLoginInfo = false;

        if (auth.getCurrentUser() != null)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        setContentView(R.layout.activity_firebase_sign_in);

        viewEmail = findViewById(R.id.view_sign_in_email);
        viewPassword = findViewById(R.id.view_sign_in_password);
        viewNewUserLink = findViewById(R.id.view_sign_in_new_user_link);
        viewForgotPasswordLink = findViewById(R.id.view_sign_in_forgot_password_link);
        btnLogin = findViewById(R.id.btn_sign_in_login);

        viewNewUserLink.setOnClickListener(onClickListener);
        viewForgotPasswordLink.setOnClickListener(onClickListener);
        btnLogin.setOnClickListener(onClickListener);

        //checkSignIn();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void openNewUserSignUp()
    {
        Intent intent = new Intent(this, FirebaseNewUserSignUpActivity.class);
        startActivity(intent);
        finish();
    }

    private void openForgotPassword()
    {
        String email = viewEmail.getText().toString();

        if (email.equals(""))
        {
            Toast.makeText(this, "Please enter an email address to receive the password reset link", Toast.LENGTH_LONG).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password Reset Email sent.");
                        }
                    }
                });
    }

    private void loginReturningUser()
    {
        String email = viewEmail.getText().toString();

        if (email.equals(""))
        {
            Toast.makeText(FirebaseSignInActivity.this, "Please Enter Your Email Address", Toast.LENGTH_SHORT).show();
            return;
        }

        String password = viewPassword.getText().toString();

        if (password.equals(""))
        {
            Toast.makeText(FirebaseSignInActivity.this, "Please Enter Your Password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            //FirebaseUser user = auth.getCurrentUser();
                            //updateUI(user);
                            String userId = auth.getUid();
                            Intent intent = new Intent(FirebaseSignInActivity.this, MainActivity.class);
                            intent.putExtra(USER_ID, userId);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(FirebaseSignInActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    public void checkSignIn()
    {
        if (auth.getCurrentUser() != null)
        {
            // already signed in
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(CHECK_IF_NEW_USER, userMustEnterLoginInfo);
            startActivity(intent);

            finish();
        }
        else
        {
            // not signed in
            userMustEnterLoginInfo = true;
            beginSignIn();
        }
    }

    public void beginSignIn()
    {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build(),
                new AuthUI.IdpConfig.GoogleBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                //FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra(CHECK_IF_NEW_USER, userMustEnterLoginInfo);
                startActivity(intent);

                finish();

            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...

                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    Toast.makeText(this, R.string.sign_in_cancelled, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(this, R.string.no_internet_connection, Toast.LENGTH_SHORT).show();
                    return;
                }

                Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Sign-in error: ", response.getError());
            }
        }
    }

}
