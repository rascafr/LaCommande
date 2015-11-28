package fr.bde_eseo.lacommande.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.bde_eseo.lacommande.R;

/**
 * Created by Rascafr on 11/11/2015.
 */
public class EmptyTab extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_empty, container, false);

        return rootView;
    }
}
