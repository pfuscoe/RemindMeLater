package patrick.fuscoe.remindmelater;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;
import java.util.List;

public class FirebaseSignInActivity extends AppCompatActivity {

    public static final String TAG = "patrick.fuscoe.remindmelater.FirebaseSignInActivity";
    public static final String CHECK_IF_NEW_USER = "patrick.fuscoe.remindmelater.CHECK_IF_NEW_USER";
    public static final String DISPLAY_NAME = "patrick.fuscoe.remindmelater.DISPLAY_NAME";

    private static final int RC_SIGN_IN = 223;

    public static final String USER_ID = "patrick.fuscoe.remindmelater.USER_ID";

    private FirebaseAuth auth;

    public GoogleSignInClient googleSignInClient;

    private ProgressBar viewProgressBar;
    private ImageView viewAppIcon;
    private TextView viewAppTitle;
    private TextView viewAppAuthor;
    private EditText viewEmail;
    private EditText viewPassword;
    private TextView viewNewUserLink;
    private TextView viewForgotPasswordLink;
    private Button btnLogin;
    private com.google.android.gms.common.SignInButton btnSignInWithGoogle;
    private TextView viewCopyright;

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

                case R.id.btn_sign_in_with_google:
                    signInWithGoogle();
                    return;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_firebase_sign_in);

        auth = FirebaseAuth.getInstance();

        userMustEnterLoginInfo = false;

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.web_client_id))
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);

        viewProgressBar = findViewById(R.id.view_sign_in_progress_bar);
        viewAppIcon = findViewById(R.id.view_sign_in_app_icon);
        viewAppTitle = findViewById(R.id.view_sign_in_app_title);
        viewAppAuthor = findViewById(R.id.view_sign_in_app_author);
        viewEmail = findViewById(R.id.view_sign_in_email);
        viewPassword = findViewById(R.id.view_sign_in_password);
        viewNewUserLink = findViewById(R.id.view_sign_in_new_user_link);
        viewForgotPasswordLink = findViewById(R.id.view_sign_in_forgot_password_link);
        btnLogin = findViewById(R.id.btn_sign_in_login);
        btnSignInWithGoogle = findViewById(R.id.btn_sign_in_with_google);
        viewCopyright = findViewById(R.id.view_sign_in_copyright);

        viewNewUserLink.setOnClickListener(onClickListener);
        viewForgotPasswordLink.setOnClickListener(onClickListener);
        btnLogin.setOnClickListener(onClickListener);
        btnSignInWithGoogle.setOnClickListener(onClickListener);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = auth.getCurrentUser();

        if (user != null)
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void openNewUserSignUp()
    {
        Intent intent = new Intent(this, FirebaseNewUserSignUpActivity.class);
        startActivity(intent);
    }

    private void openForgotPassword()
    {
        final String email = viewEmail.getText().toString();

        if (email.equals(""))
        {
            Toast.makeText(this, "Please enter an email address to receive the password reset link", Toast.LENGTH_LONG).show();
            return;
        }

        showProgressBar();

        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        hideProgressBar();

                        if (task.isSuccessful()) {
                            Log.d(TAG, "Password Reset Email sent to: " + email);
                            Toast.makeText(FirebaseSignInActivity.this, "Password Reset Email sent to: " + email, Toast.LENGTH_LONG).show();
                        }
                        else
                        {
                            Toast.makeText(FirebaseSignInActivity.this, "Error sending password reset email to " + email + ": " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
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

        showProgressBar();

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            //updateUI(user);
                            if (user.isEmailVerified())
                            {
                                //hideProgressBar();
                                String userId = auth.getUid();
                                Intent intent = new Intent(FirebaseSignInActivity.this, MainActivity.class);
                                intent.putExtra(USER_ID, userId);
                                intent.putExtra(FirebaseSignInActivity.CHECK_IF_NEW_USER, true);
                                startActivity(intent);
                                finish();
                            }
                            else
                            {
                                Toast.makeText(FirebaseSignInActivity.this, "Please check your email to verify your account before logging in", Toast.LENGTH_LONG).show();
                                logoutUser();
                            }
                        } else {
                            // If sign in fails, display a message to the user.
                            hideProgressBar();
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(FirebaseSignInActivity.this, "Authentication failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                    }
                });
    }

    private void signInWithGoogle()
    {
        showProgressBar();

        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(this, "Google sign in failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        //showProgressBar();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            //hideProgressBar();
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //FirebaseUser user = auth.getCurrentUser();
                            String userId = auth.getUid();
                            Intent intent = new Intent(FirebaseSignInActivity.this, MainActivity.class);
                            intent.putExtra(USER_ID, userId);
                            intent.putExtra(FirebaseSignInActivity.CHECK_IF_NEW_USER, true);
                            //hideProgressBar();
                            startActivity(intent);
                            finish();
                        } else {
                            hideProgressBar();
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(FirebaseSignInActivity.this, "Google Authentication failed", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void logoutUser()
    {
        AuthUI.getInstance().signOut(this);
        hideProgressBar();
    }

    private void showProgressBar()
    {
        viewProgressBar.setVisibility(View.VISIBLE);

        viewAppIcon.setVisibility(View.INVISIBLE);
        viewAppTitle.setVisibility(View.INVISIBLE);
        viewAppAuthor.setVisibility(View.INVISIBLE);
        viewEmail.setVisibility(View.INVISIBLE);
        viewPassword.setVisibility(View.INVISIBLE);
        btnLogin.setVisibility(View.INVISIBLE);
        btnSignInWithGoogle.setVisibility(View.INVISIBLE);
        viewNewUserLink.setVisibility(View.INVISIBLE);
        viewForgotPasswordLink.setVisibility(View.INVISIBLE);
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
        btnLogin.setVisibility(View.VISIBLE);
        btnSignInWithGoogle.setVisibility(View.VISIBLE);
        viewNewUserLink.setVisibility(View.VISIBLE);
        viewForgotPasswordLink.setVisibility(View.VISIBLE);
        viewCopyright.setVisibility(View.VISIBLE);
    }

    /*
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
    */

}
