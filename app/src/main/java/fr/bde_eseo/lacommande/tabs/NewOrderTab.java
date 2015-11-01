package fr.bde_eseo.lacommande.tabs;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 19/10/2015.
 */
public class NewOrderTab extends Fragment {

    private AutoCompleteTextView autocomplete;
    private TextView tvInfo;
    private ArrayAdapter<String> adapter;

    private static final String[] COUNTRIES = new String[]{
            "Belgium", "France", "Italy", "Germany", "Spain"
    };

    private ArrayList<String> logins;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_new_order, container, false);

        logins = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_dropdown_item_1line, logins);

        autocomplete = (AutoCompleteTextView) rootView.findViewById(R.id.autocomplete);
        tvInfo = (TextView) rootView.findViewById(R.id.tvInfo);
        autocomplete.setAdapter(adapter);

        AsyncLogin asyncLogin = new AsyncLogin();
        asyncLogin.execute(Constants.URL_CLIENTS_LISTS);

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
            tvInfo.setText("Mise à jour ...");
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            tvInfo.setText("Mise à jour ... [OK]\n" + data.length() + " octets récupérés");

            if (Utilities.isNetworkDataValid(data)) {

                try {
                    JSONArray array = new JSONArray(data);
                    for (int i=0;i<array.length();i++) {
                        logins.add(array.getJSONObject(i).getString("login"));
                        Log.d("LOGIN", logins.get(i));
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
