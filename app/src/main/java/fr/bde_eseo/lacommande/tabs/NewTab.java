package fr.bde_eseo.lacommande.tabs;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.util.ArrayList;

import fr.bde_eseo.lacommande.ClientListActivity;
import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.OrderGenericActivity;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
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
        logins = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, logins);

        AsyncLogin asyncLogin = new AsyncLogin();
        asyncLogin.execute(Constants.URL_CLIENTS_LISTS);

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
                                Toast.makeText(getActivity(), "Select : " + autoCompleteTextView.getText().toString(), Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(getActivity(), OrderGenericActivity.class);
                                getActivity().startActivity(i);
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

    private class AsyncLogin extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... url) {
            return ConnexionUtils.postServerData(url[0], null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONArray array = new JSONArray(data);
                    for (int i=0;i<array.length();i++) {
                        logins.add(array.getJSONObject(i).getString("fullname"));
                    }
                    Toast.makeText(getActivity(), logins.size() + " logins", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
