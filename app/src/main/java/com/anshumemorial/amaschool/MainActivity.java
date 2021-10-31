package com.anshumemorial.amaschool;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

//    variables declaration
    private WebView webView;
    private ProgressBar progressBar;
    private ProgressBar horizontalProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout noInternetLayout;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        variables assignment
        webView = findViewById(R.id.webView);
        progressBar = findViewById(R.id.progressBar);
        horizontalProgressBar = findViewById(R.id.horizontal_progressbar);
        horizontalProgressBar.setMax(100);
        swipeRefreshLayout = findViewById(R.id
                .swipeRefresh);
        noInternetLayout = findViewById(R.id.no_internet_layout);
//        local variables declaration with assignment
        Button refreshBtn = findViewById(R.id.refresh_btn);
//        ImageView goToRegPage = findViewById(R.id.contact);
//        go to register page
//        goToRegPage.setOnClickListener(v -> {
//            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
//            startActivity(intent);
//        });

//        webView settings
        webView.setWebViewClient(new myWebViewClient());
        webView.setWebChromeClient(new myWebChromeClient());
        webView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
//         Improve loading speed
        settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        settings.setUseWideViewPort(true);
        settings.setSaveFormData(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        /* load web page function when app load */
        loadWebPage();
        /* load web page on click refresh button or swipe layout */
        refreshBtn.setOnClickListener(v -> loadWebPage());
        swipeRefreshLayout.setOnRefreshListener(this::loadWebPage);

    }

    private void loadWebPage() {
        if(isConnected()) {
            webView.loadUrl("https://anshumemorial.in");
            webView.setVisibility(View.VISIBLE);
            noInternetLayout.setVisibility(View.GONE);
        }else{
            webView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            horizontalProgressBar.setVisibility(View.GONE);
            noInternetLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setRefreshing(false);
            Toast.makeText(this, R.string.check_internet, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed(){
        if (webView.canGoBack()){
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    public boolean isConnected(){

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        return wifi.isConnected() || mobile.isConnected();
    }

    private class myWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.startsWith("mailto:")) {
                startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse(url)));
            } else if(url.startsWith("tel:") || url.startsWith("whatsapp:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
                return true;
            }
            return false;
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            if (uri.toString().startsWith("mailto:")) {
                //Handle mail Urls
                startActivity(new Intent(Intent.ACTION_SENDTO, uri));
            } else if (uri.toString().startsWith("tel:")) {
                //Handle telephony Urls
                startActivity(new Intent(Intent.ACTION_DIAL, uri));
            } else if (uri.toString().startsWith("whatsapp:")) {
                //Handle whatsapp Urls
                startActivity(new Intent(Intent.ACTION_VIEW, uri));
            } else {
                //Handle Web Urls
                view.loadUrl(uri.toString());
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
//            below code are not need because it handle by another place
            progressBar.setVisibility(View.VISIBLE);
            horizontalProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(true);
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            progressBar.setVisibility(View.GONE);
            horizontalProgressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            super.onPageFinished(view, url);
        }
    }

    private class myWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            horizontalProgressBar.setProgress(newProgress);
            if(newProgress == 100){
                horizontalProgressBar.setVisibility(View.GONE);
            }else {
                horizontalProgressBar.setVisibility(View.VISIBLE);
            }
        }
    }
}