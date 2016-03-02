package fr.bde_eseo.lacommande;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import fr.bde_eseo.lacommande.async.AsyncLock;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.slidingtab.SlidingTabLayout;
import fr.bde_eseo.lacommande.slidingtab.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    // Navigation Sliding Tabs
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private ViewPagerAdapter mAdapter;

    // Others / Android
    private boolean isAdmin;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = MainActivity.this;

        // Set UI Main Layout
        setContentView(R.layout.activity_main);

        // Set sliding tabs objects
        CharSequence mTitles[];
        if (DataStore.getInstance().getClubMember().getLevel() != 0) {
            mTitles = getResources().getStringArray(R.array.tabs_names_admin);
            isAdmin = true;
        } else {
            mTitles = getResources().getStringArray(R.array.tabs_names);
            isAdmin = false;
        }

        mPager = (ViewPager) findViewById(R.id.home_fragment_pager);
        mAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mTitles, mTitles.length, isAdmin);
        mPager.setAdapter(mAdapter);
        mTabs = (SlidingTabLayout) findViewById(R.id.home_fragment_tabs);
        mTabs.setDistributeEvenly(true);
        mTabs.setViewPager(mPager);

    }

    @Override
    public void onBackPressed() {

        new MaterialDialog.Builder(this)
                .title("Quitter")
                .content("Vous allez être déconnecté.\nN'oubliez pas de fermer la cafet depuis l'icône \"cadenas\" situé en haut à droite de l'écran si c'est la fin de votre service.\n\nContinuer ?")
                .positiveText("Déconnexion")
                .negativeText("Annuler")
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        MainActivity.super.onBackPressed();
                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                    }
                })
                .show();
    }

    /**
     * Menu : exit / close cafet icon in toolbar
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_logged, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case R.id.action_exit:
                this.onBackPressed();
                return true;

            // respond to the lock cafet action
            case R.id.action_lock:

                // Dialog to confirm
                MaterialDialog materialDialog = new MaterialDialog.Builder(context)
                        .title("Fermer la cafet")
                        .content("Toutes les commandes en cours seront :\n - soit marquées comme terminées\n - soit marquées comme impayées\n\nLes commandes n'ayant pas abouti seront supprimées.\n\nN'effectuez cette action qu'à la fin de votre service (vers 13h15).")
                        .negativeText("Annuler")
                        .positiveText("Confirmer")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                materialDialog.hide();
                                new AsyncLock(context).execute(Constants.API_ORDER_LOCK);
                            }
                        })
                        .show();

                return true;

            // respond to help action
            case R.id.action_help:

                Intent i = new Intent(context, HelpActivity.class);
                startActivity(i);

                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
