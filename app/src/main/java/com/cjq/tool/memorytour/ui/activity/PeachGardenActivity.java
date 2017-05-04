package com.cjq.tool.memorytour.ui.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.cjq.tool.memorytour.R;

public class PeachGardenActivity extends AppCompatActivity {

    private WebView wvPeachGarden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peach_garden);

        wvPeachGarden = (WebView)findViewById(R.id.wv_peach_garden);
        wvPeachGarden.getSettings().setJavaScriptEnabled(true);
        wvPeachGarden.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
        wvPeachGarden.loadUrl("http://www.baidu.com");
    }
}
