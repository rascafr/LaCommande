package fr.bde_eseo.lacommande.slidingtab;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import fr.bde_eseo.lacommande.tabs.AdminTab;
import fr.bde_eseo.lacommande.tabs.EmptyTab;
import fr.bde_eseo.lacommande.tabs.HistoryTab;
import fr.bde_eseo.lacommande.tabs.NewTab;
import fr.bde_eseo.lacommande.tabs.OrdersTab;


public class ViewPagerAdapter extends FragmentStatePagerAdapter {

    private CharSequence titles[]; // This will Store the titles of the Tabs which are Going to be passed when ViewPagerAdapter is created
    private int numbOfTabs; // Store the number of tabs, this will also be passed when the ViewPagerAdapter is created
    private boolean isAdmin;

    // Build a Constructor and assign the passed Values to appropriate values in the class
    public ViewPagerAdapter(FragmentManager fm, CharSequence titles[], int mNumbOfTabsumb, boolean isAdmin) {
        super(fm);

        this.titles = titles;
        this.numbOfTabs = mNumbOfTabsumb;
        this.isAdmin = isAdmin;

    }

    //This method return the fragment for the every position in the View Pager
    @Override
    public Fragment getItem(int position) {

        if (isAdmin)
            position--;

        switch (position) {
            case -1:
                return new AdminTab();

            case 0:
                return new NewTab();

            case 1:
                return new OrdersTab();

            case 2:
                return new HistoryTab();

            case 3:
                return new EmptyTab();
        }
        return null;
    }

    // This method return the titles for the Tabs in the Tab Strip
    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];

    }

    // This method return the Number of tabs for the tabs Strip
    @Override
    public int getCount() {
        return numbOfTabs;
    }

    public void setCartTitle(CharSequence title) {
        titles[1] = title;
        notifyDataSetChanged();
    }
}