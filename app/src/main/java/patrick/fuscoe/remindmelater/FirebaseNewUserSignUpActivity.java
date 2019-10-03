package patrick.fuscoe.remindmelater;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
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

        auth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_firebase_new_user_sign_up);

        viewEmail = findViewById(R.id.view_new_user_sign_up_email);
        viewPassword = findViewById(R.id.view_new_user_sign_up_password);
        viewDisplayName = findViewById(R.id.view_new_user_sign_up_display_name);
        btnSignUp = findViewById(R.id.btn_new_user_sign_up_sign_up);

        btnSignUp.setOnClickListener(onClickListener);
    }

    private void signUp()
    {
        String email = viewEmail.getText().toString();
        String password = viewPassword.getText().toString();
        String displayName = viewDisplayName.getText().toString();

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

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            //FirebaseUser user = mAuth.getCurrentUser();
                            //updateUI(user);
                            Intent intent = new Intent(FirebaseNewUserSignUpActivity.this, MainActivity.class);
                            intent.putExtra(FirebaseSignInActivity.CHECK_IF_NEW_USER, true);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(FirebaseNewUserSignUpActivity.this, "Authentication failed: " +
                                            task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            //updateUI(null);
                        }
                    }
                });
    }
}
