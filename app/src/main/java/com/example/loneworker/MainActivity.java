package com.example.loneworker;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private CheckBox rememberMe;
    private Button verifyButton;
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        rememberMe = findViewById(R.id.rememberMe);
        verifyButton = findViewById(R.id.verifyButton);

        // Setup WebView to run JavaScript validation
        webView = new WebView(this);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new WebAppInterface(), "Android");
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // WebView is ready
            }
        });
        String html = "<html><head><script src=\"validation.js\"></script></head><body></body></html>";
        webView.loadDataWithBaseURL("file:///android_asset/", html, "text/html", "utf-8", null);

        verifyButton.setOnClickListener(v -> {
            // String email = emailInput.getText().toString();
            // String password = passwordInput.getText().toString();
            
            // Call JavaScript function for validation
            // String script = String.format("javascript:Android.onValidationResult(validateLogin('%s', '%s'))", email, password);
            // webView.loadUrl(script);

            // Bypass validation for testing
            Toast.makeText(MainActivity.this, "Login Successful! (Bypass)", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity.this, MenuActivity.class);
            startActivity(intent);
            finish();
        });
    }

    public class WebAppInterface {
        @JavascriptInterface
        public void onValidationResult(String result) {
            runOnUiThread(() -> {
                if ("OK".equals(result)) {
                    Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(MainActivity.this, MenuActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, result, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
