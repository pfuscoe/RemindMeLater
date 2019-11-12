package patrick.fuscoe.remindmelater;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.WebView;

/**
 * Manages UI display of terms of service page
 */
public class TermsOfServiceActivity extends AppCompatActivity {

    private WebView viewTermsOfServiceWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms_of_service);

        viewTermsOfServiceWebView = findViewById(R.id.view_terms_of_service_webview);
        viewTermsOfServiceWebView.loadUrl("file:///android_asset/terms_and_conditions.html");
    }
}
