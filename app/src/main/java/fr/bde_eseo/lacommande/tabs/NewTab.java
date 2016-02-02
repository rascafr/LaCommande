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
import fr.bde_eseo.lacommande.model.ClientItem;
import fr.bde_eseo.lacommande.model.DataStore;
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
                                AsyncToken asyncToken = new AsyncToken(
                                        DataStore.getInstance().getClubMember().getLogin(),
                                        DataStore.getInstance().getClubMember().getPassword(),
                                        DataStore.getInstance().searchForClient(clientName).getLogin(),
                                        BuildConfig.VERSION_NAME,
                                        clientName
                                );
                                asyncToken.execute(Constants.URL_TOKEN_GET);
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

    private class AsyncToken extends AsyncTask<String, String, String> {

        private String loginClub, passwordClub, loginClient, version, clientName;
        private MaterialDialog materialDialog;

        public AsyncToken(String loginClub, String passwordClub, String loginClient, String version, String clientName) {
            this.loginClub = loginClub;
            this.passwordClub = passwordClub;
            this.loginClient = loginClient;
            this.version = version;
            this.clientName = clientName;
        }

        @Override
        protected String doInBackground(String... url) {
            HashMap<String, String> pairs = new HashMap<>();
            String hash = EncryptUtils.sha256(
                    getString(R.string.salt_get_token) +
                            loginClub +
                            passwordClub +
                            loginClient +
                            version
            );
            pairs.put("loginClub", loginClub);
            pairs.put("passwordClub", passwordClub);
            pairs.put("loginClient", loginClient);
            pairs.put("version", version);
            pairs.put("hash", hash);

            return ConnexionUtils.postServerData(url[0], pairs);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            materialDialog = new MaterialDialog.Builder(getActivity())
                    .title("Préparation")
                    .content("Veuillez patienter ...")
                    .cancelable(false)
                    .progress(true, 4)
                    .progressIndeterminateStyle(false)
                    .show();
        }

        @Override
        protected void onPostExecute(final String data) {
            super.onPostExecute(data);

            // Wait a little
            new Handler().postDelayed(
                    new Runnable() {

                        @Override
                        public void run() {

                            materialDialog.hide();

                            if (Utilities.isNetworkDataValid(data)) {
                                try {
                                    JSONObject obj = new JSONObject(data);

                                    if (obj.getInt("result") == 1) {
                                        DataStore.getInstance().setToken(obj.getString("token"));
                                        Intent i = new Intent(getActivity(), OrderGenericActivity.class);
                                        i.putExtra(Constants.KEY_NEW_ORDER_CLIENT, clientName);
                                        getActivity().startActivity(i);
                                    } else {
                                        materialDialog = new MaterialDialog.Builder(getActivity())
                                                .title("Erreur")
                                                .content(obj.getString("cause"))
                                                .cancelable(false)
                                                .negativeText("Fermer")
                                                .show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(getActivity(), "Erreur serveur", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getActivity(), "Erreur réseau", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, 1500);
        }
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
