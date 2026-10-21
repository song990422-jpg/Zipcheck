package kr.zipcheck.prototype;

import android.app.Activity;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.ValueCallback;
import androidx.core.content.FileProvider;
import androidx.webkit.WebViewAssetLoader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private static final int FILE_CHOOSER_REQUEST = 501;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private Uri cameraImageUri;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(Color.rgb(247,250,253));
        getWindow().setNavigationBarColor(Color.rgb(247,250,253));
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        );
        webView = new WebView(this);
        webView.setBackgroundColor(Color.rgb(247,250,253));
        webView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        setContentView(webView);
        final WebViewAssetLoader assetLoader = new WebViewAssetLoader.Builder()
            .addPathHandler("/assets/", new WebViewAssetLoader.AssetsPathHandler(this))
            .build();
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(true);
        settings.setUserAgentString(settings.getUserAgentString() + " ZipCheckAndroid/1.0");
        webView.setWebViewClient(new WebViewClient() {
            @Override public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return assetLoader.shouldInterceptRequest(request.getUrl());
            }
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri u = request.getUrl();
                return !"appassets.androidplatform.net".equals(u.getHost());
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                cameraImageUri = null;
                Intent gallery = new Intent(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                gallery.addCategory(Intent.CATEGORY_OPENABLE);
                gallery.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, !params.isCaptureEnabled());
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                boolean cameraAvailable = camera.resolveActivity(getPackageManager()) != null;
                if (cameraAvailable) {
                    try {
                        File photo = File.createTempFile("zipcheck_capture_", ".jpg", getCacheDir());
                        cameraImageUri = FileProvider.getUriForFile(MainActivity.this,
                            getPackageName() + ".fileprovider", photo);
                        camera.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
                        camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        camera.setClipData(ClipData.newRawUri("photo", cameraImageUri));
                    } catch (IOException ex) {
                        cameraAvailable = false;
                        cameraImageUri = null;
                    }
                }
                Intent selection;
                if (params.isCaptureEnabled() && cameraAvailable) {
                    selection = camera;
                } else {
                    selection = Intent.createChooser(gallery, "사진 선택");
                    if (cameraAvailable) selection.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{camera});
                }
                try { startActivityForResult(selection, FILE_CHOOSER_REQUEST); }
                catch (Exception ex) { fileCallback.onReceiveValue(null); fileCallback=null; return false; }
                return true;
            }
        });
        if(savedInstanceState != null) webView.restoreState(savedInstanceState);
        else webView.loadUrl("https://appassets.androidplatform.net/assets/index.html");
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if (requestCode != FILE_CHOOSER_REQUEST || fileCallback == null) return;
        ArrayList<Uri> photos = new ArrayList<>();
        if (resultCode == RESULT_OK) {
            if (data != null && data.getClipData() != null) {
                ClipData clips = data.getClipData();
                for(int i=0;i<clips.getItemCount();i++)photos.add(clips.getItemAt(i).getUri());
            } else if (data != null && data.getData() != null) {
                photos.add(data.getData());
            } else if (cameraImageUri != null) {
                photos.add(cameraImageUri);
            }
        }
        fileCallback.onReceiveValue(photos.isEmpty() ? null : photos.toArray(new Uri[0]));
        fileCallback = null;
        cameraImageUri = null;
    }
    @Override public void onBackPressed() {
        if (webView != null && webView.canGoBack()) webView.goBack();
        else super.onBackPressed();
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        if(webView != null) webView.saveState(state);
        super.onSaveInstanceState(state);
    }
    @Override protected void onDestroy() {
        if(fileCallback != null){fileCallback.onReceiveValue(null);fileCallback=null;}
        if(webView != null){webView.destroy();webView=null;}
        super.onDestroy();
    }
}