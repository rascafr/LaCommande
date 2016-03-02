package fr.bde_eseo.lacommande;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.webkit.WebView;

/**
 * Created by root on 29/02/16.
 */
public class HelpActivity extends AppCompatActivity {

    // Android
    private Context context;

    // UI Layout
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        context = this;

        // Set back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get views
        webView = (WebView) findViewById(R.id.webViewHelp);

        // Configure view
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        // Load data
        webView.loadUrl("file:///android_asset/help.html");
        webView.setPadding(10, 0, 10, 0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                HelpActivity.this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
