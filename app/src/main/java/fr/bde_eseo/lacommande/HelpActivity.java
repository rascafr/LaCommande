package fr.bde_eseo.lacommande;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

/**
 * Created by root on 29/02/16.
 */
public class HelpActivity extends AppCompatActivity {

    // Android
    private Context context;

    // UI Layout
    private WebView webView;
    private ProgressBar progressWeb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        context = this;

        // Set back arrow
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get views
        webView = (WebView) findViewById(R.id.webViewHelp);
        progressWeb = (ProgressBar) findViewById(R.id.progressWeb);
        webView.getSettings().setJavaScriptEnabled(true); // enable javascript

        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Toast.makeText(context, "Erreur de chargement de l'aide (raison : " + description + ")", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                progressWeb.setVisibility(View.INVISIBLE);
            }
        });
        webView.loadUrl(Constants.API_HELP_GET);

        /*

        // Configure view
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);

        // Load data
        webView.loadUrl("file:///android_asset/help.html");
        webView.setPadding(10, 0, 10, 0);*/
    }

    /**
     * Menu : refresh
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_help, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            // Respond to the help action
            case R.id.action_refresh:
                webView.loadUrl(Constants.API_HELP_GET);
                webView.reload();
                Toast.makeText(context, "Mise à jour ...", Toast.LENGTH_SHORT).show();
                return true;

            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                HelpActivity.this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
