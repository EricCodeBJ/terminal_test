package com.kidevstudio.mobilemoneyhelper;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class PaymentWebViewActivity extends Activity {

    private static PaymentHelper.PaymentCallback paymentCallback;
    private ProgressBar progressBar;
    private WebView webView;

    public static void setPaymentCallback(PaymentHelper.PaymentCallback callback) {
        paymentCallback = callback;
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_web_view);

        // Initialize ProgressBar and WebView
        progressBar = findViewById(R.id.progressBar);
        webView = findViewById(R.id.webView);

        setupWebView();

        String paymentUrl = getIntent().getStringExtra("paymentUrl");
        String paymentAmount = String.valueOf(getIntent().getStringExtra("amount"));

        // Set WebViewClient to handle page loading
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String url = request.getUrl().toString();
                if (url.contains("callback")) {
                    String transactionId = extractTransactionId(url);
                    final Boolean[] status = {false};
                    if (paymentCallback != null) {
                        PaymentHelper.checkTransactionAsync(transactionId, Integer.parseInt(paymentAmount), new PaymentHelper.TransactionStatusCallback() {
                            @Override
                            public void onTransactionCheckComplete(boolean success, JSONObject result) {
                                status[0] = success;
                                paymentCallback.onTransactionCompleted(status[0], transactionId);
                            }
                        });
                    }
                    finish();
                    return true;
                }
                return false;
            }
        });

        webView.loadUrl(paymentUrl);
    }

    private String extractTransactionId(String url) {
        Uri uri = Uri.parse(url);
        return uri.getQueryParameter("transactionId");
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView() {
        WebSettings webSettings = webView.getSettings();

        // Enable JavaScript
        webSettings.setJavaScriptEnabled(true);

        // Enable zoom controls
        webSettings.setBuiltInZoomControls(true);
        webSettings.setDisplayZoomControls(false);

        // Enable file access within the WebView
        webSettings.setAllowFileAccess(true);

        // Enable loading mixed content (HTTP content on HTTPS sites)
        webSettings.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);

        // Enable DOM storage
        webSettings.setDomStorageEnabled(true);

        // Handle redirects and URL loading within the WebView
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                view.loadUrl(request.getUrl().toString());
                return true;
            }
        });

        // Enable advanced web features, such as alerts and dialogs
        webView.setWebChromeClient(new WebChromeClient());

        // For debugging, allow remote debugging (Remove in production)
        WebView.setWebContentsDebuggingEnabled(true);
    }
}
