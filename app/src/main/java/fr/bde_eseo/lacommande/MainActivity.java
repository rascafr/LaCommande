package fr.bde_eseo.lacommande;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.MaterialDialog;

import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.slidingtab.SlidingTabLayout;
import fr.bde_eseo.lacommande.slidingtab.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

    // Navigation Sliding Tabs
    private ViewPager mPager;
    private SlidingTabLayout mTabs;
    private ViewPagerAdapter mAdapter;
    private boolean isAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
                .content("Vous allez être déconnecté.\nN'oubliez pas de fermer la cafet depuis le menu \"Fermeture\" si c'est la fin de votre service.\n\nContinuer ?")
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
        }
        return super.onOptionsItemSelected(item);
    }
}
