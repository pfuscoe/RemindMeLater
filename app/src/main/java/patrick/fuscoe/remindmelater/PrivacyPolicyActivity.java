package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

/**
 * Manages UI display of privacy policy page
 */
public class PrivacyPolicyActivity extends AppCompatActivity {

private WebView viewPrivacyPolicyWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_policy);

        viewPrivacyPolicyWebView = findViewById(R.id.view_privacy_policy_webview);
        viewPrivacyPolicyWebView.loadUrl("file:///android_asset/privacy_policy.html");
    }
}
