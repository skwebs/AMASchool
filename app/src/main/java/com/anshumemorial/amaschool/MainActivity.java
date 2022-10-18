package com.anshumemorial.amaschool;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class MainActivity extends AppCompatActivity {

//    variables declaration
    private WebView webView;
//    private ProgressBar progressBar;
    private ProgressBar horizontalProgressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout noInternetLayout, rlLoading;
    private boolean isWebViewLoaded;


    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        register broadcastReceiver
        registerReceiver(broadcastReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
//        String PACKAGE_NAME = getApplicationContext().getPackageName();
//        used package_name as userAgent
        String userAgent = getApplicationContext().getPackageName();
//        variables assignment
        webView = findViewById(R.id.webView);
        horizontalProgressBar = findViewById(R.id.horizontal_progressbar);
        horizontalProgressBar.setMax(100);
        swipeRefreshLayout = findViewById(R.id
                .swipeRefresh);
        noInternetLayout = findViewById(R.id.no_internet_layout);
        rlLoading = findViewById(R.id.loading_layout);

//        local variables declaration with assignment
        Button refreshBtn = findViewById(R.id.refresh_btn);
//        assigned openWifiSetting button
        Button openWifiSetting = findViewById(R.id.open_wifi_setting_btn);
//        assigned openMobileDataSetting button
        Button openMobileDataSetting = findViewById(R.id.open_mobile_data_setting_btn);
//        open wifi setting
        openWifiSetting.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));
//        open mobile data setting
        openMobileDataSetting.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS)));
        isWebViewLoaded = false;

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
//        set user agent
//        settings.setUserAgentString(userAgent);

        /* load web page function when app load */
        loadPage();

        refreshBtn.setOnClickListener(v -> loadPage());

        swipeRefreshLayout.setOnRefreshListener(this::loadPage);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //        unregister broadcastReceiver
        unregisterReceiver(broadcastReceiver);
    }

    protected void showWebView(){
        webView.setVisibility(View.VISIBLE);
        noInternetLayout.setVisibility(View.GONE);
    }
    private void loadPage(){

        if (isConnected()){
//            if internet available then show webView and hide noInternetLayout
            webView.setVisibility(View.VISIBLE);
            noInternetLayout.setVisibility(View.GONE);
            if (webView.canGoBack()){
//                if entered in another page then reload page
                webView.reload();
            } else {
//                webView.loadUrl("https://anshumemorial.in/android/");
                String appUrl = "https://anshumemorial.in/";
                webView.loadUrl(appUrl);
            }
        }else{
//            if internet is not connected then hide webView and show noInternetLayout
            webView.setVisibility(View.GONE);
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
            new AlertDialog.Builder(this)
                    .setMessage("Are you sure to exit?")
                    .setCancelable(false)
                    .setPositiveButton("Yes",((dialog, which) -> MainActivity.super.onBackPressed()))
                    .setNegativeButton("No",null)
                    .show();
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


    }

    private class myWebChromeClient extends WebChromeClient {
        @Override
        public void onProgressChanged(WebView view, int newProgress) {
            horizontalProgressBar.setProgress(newProgress);
            if(newProgress == 100){
                horizontalProgressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                rlLoading.setVisibility(View.GONE);
                isWebViewLoaded = true;
            }else {
                horizontalProgressBar.setVisibility(View.VISIBLE);
//                rlLoading.setVisibility(View.VISIBLE);
                isWebViewLoaded = false;
            }
        }

        //  for javaScript Alert : get from -
        //  https://stackoverflow.com/questions/38053779/android-webview-how-to-change-javascript-alert-title-text-in-android-webview
        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result){
            new AlertDialog.Builder(view.getContext())
//                    .setTitle("Title")
                    .setMessage(message)
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                    .setOnDismissListener((DialogInterface dialog) -> result.confirm())
                    .create()
                    .show();
            return true;
        }

        @Override
        public boolean onJsConfirm(WebView view, String url, String message, JsResult result){
            new AlertDialog.Builder(view.getContext())
//                    .setTitle("Application says:")
                    .setMessage(message)
                    .setPositiveButton("OK", (DialogInterface dialog, int which) -> result.confirm())
                    .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> result.cancel())
                    .setOnDismissListener((DialogInterface dialog) -> result.cancel())
                    .create()
                    .show();
            return true;
        }

    }

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            if internet is available and webVew already loaded then only show webView
//            else load webView
            if (isConnected()) if (isWebViewLoaded) showWebView();else loadPage();
        }
    };

}