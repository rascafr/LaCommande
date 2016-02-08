package fr.bde_eseo.lacommande.tabs;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import fr.bde_eseo.lacommande.BuildConfig;
import fr.bde_eseo.lacommande.ClientListActivity;
import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.OrderGenericActivity;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.async.AsyncToken;
import fr.bde_eseo.lacommande.model.ClientItem;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.EncryptUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 11/11/2015.
 */
public class NewTab extends Fragment {

    // UI Objects
    private CardView cardViewOrder, cardViewClients;
    private AutoCompleteTextView autoCompleteTextView;

    // Data - Model
    private ArrayAdapter<String> adapter;
    private ArrayList<String> logins;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_new_order, container, false);

        // Get layout objects
        cardViewOrder = (CardView) rootView.findViewById(R.id.cardOrder);
        cardViewClients = (CardView) rootView.findViewById(R.id.cardUsers);

        // Set data
        fillEditTextData();
        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, logins);

        // On click listener (add new order -> user choose dialog)
        cardViewOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialDialog.Builder mdb = new MaterialDialog.Builder(getActivity())
                        .title("Commander")
                        .customView(R.layout.dialog_select_user, true)
                        .positiveText("VALIDER")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                String clientName = autoCompleteTextView.getText().toString();
                                ClientItem ci = DataStore.getInstance().searchForClient(clientName);
                                if (ci != null) {
                                    AsyncToken asyncToken = new AsyncToken(
                                            getActivity(),
                                            DataStore.getInstance().getClubMember().getLogin(),
                                            DataStore.getInstance().getClubMember().getPassword(),
                                            ci.getLogin(),
                                            BuildConfig.VERSION_NAME,
                                            clientName
                                    );
                                    asyncToken.execute();
                                } else {
                                    Toast.makeText(getActivity(), "Cette personne n'est pas enregistrée comme étant un client.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .cancelable(true);

                MaterialDialog md = mdb.show();
                View vDialog = md.getView();
                autoCompleteTextView = (AutoCompleteTextView) vDialog.findViewById(R.id.autocomplete);
                autoCompleteTextView.setAdapter(adapter);
            }
        });

        // On click listener (view clients)
        cardViewClients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), ClientListActivity.class);
                getActivity().startActivity(i);
            }
        });

        return rootView;
    }


    // Fill the autocomplete edittext with client's data
    private void fillEditTextData () {
        if (logins == null)
            logins = new ArrayList<>();
        else
            logins.clear();

        for (int i=0;i<DataStore.getInstance().getClientItems().size();i++) {
            logins.add(DataStore.getInstance().getClientItems().get(i).getFullname());
        }
    }
}
