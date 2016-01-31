package fr.bde_eseo.lacommande;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import fr.bde_eseo.lacommande.model.ClientItem;
import fr.bde_eseo.lacommande.model.ClubMember;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.EncryptUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 19/10/2015.
 */
public class LoginActivity extends AppCompatActivity {

    // UI objects
    private EditText etLogin;
    private EditText etPassword;
    private Button bpConnect;
    private ProgressBar progressConnect;
    private TextView tvProgress;

    // Android
    private Context context;

    // Preferences
    private SharedPreferences prefs;
    private SharedPreferences.Editor prefs_Write;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = LoginActivity.this;

        // Preferences
        prefs = getSharedPreferences(Constants.PREFS_IDENTIFIER, 0);
        prefs_Write = prefs.edit();

        // Set UI Main Layout
        setContentView(R.layout.activity_login);

        // Assign layout object
        bpConnect = (Button) findViewById(R.id.bpConnect);
        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        progressConnect = (ProgressBar) findViewById(R.id.progressConnect);
        tvProgress = (TextView) findViewById(R.id.tvProgress);

        // Set view
        etLogin.setText(prefs.getString(Constants.PREFS_KEY_LOGIN, ""));

        // Listen for connexion intent
        bpConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tvProgress.setText("Préparation ... ");
                progressConnect.setVisibility(View.VISIBLE);
                Utilities.hideSoftKeyboard(LoginActivity.this);

                new Handler().postDelayed(
                        new Runnable() {

                            @Override
                            public void run() {
                                AsyncConnectClub asyncConnectClub = new AsyncConnectClub();
                                asyncConnectClub.execute();
                            }
                        }, 600);
            }
        });
    }


    // Custom Async Task to connect club
    private class AsyncConnectClub extends AsyncTask<String, String, APIResponse> {

        private String login;
        private String password;
        private String version;

        @Override
        protected APIResponse doInBackground(String... params) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("login", login);
            pairs.put("password", password);
            pairs.put("version", version);

            return APIUtils.postAPIData(Constants.API_CLUB_LOGIN, pairs, context);
        }

        @Override
        protected void onPreExecute() {
            login = etLogin.getText().toString().toLowerCase(Locale.FRANCE).trim();
            etLogin.setText(login);
            password = EncryptUtils.sha256(etPassword.getText().toString() + getResources().getString(R.string.salt_password));
            version = BuildConfig.VERSION_NAME;
            tvProgress.setVisibility(View.VISIBLE);
            tvProgress.setText("Authentification ...");
        }

        @Override
        protected void onPostExecute(final APIResponse apiResponse) {

            if (apiResponse.isValid()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            // Login ok
                            tvProgress.setText("Connecté");
                            DataStore.getInstance().setClubMember(new ClubMember(login, password, apiResponse.getJsonData()));

                            // Save login
                            prefs_Write.putString(Constants.PREFS_KEY_LOGIN, login);
                            prefs_Write.apply();

                            // Update client data
                            AsyncClients asyncClients = new AsyncClients();
                            asyncClients.execute(Constants.URL_CLIENTS_LISTS);

                        } catch (JSONException e) {
                            tvProgress.setText("Erreur serveur");
                            progressConnect.setVisibility(View.GONE);
                            e.printStackTrace();
                        }
                    }
                }, 1500);
            } else {
                tvProgress.setText(apiResponse.getError());
                progressConnect.setVisibility(View.GONE);
            }
        }
    }

    // Custom task to download data about clients from server's database
    // (best moment to do it !)
    private class AsyncClients extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... url) {
            return ConnexionUtils.postServerData(url[0], null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressConnect.setVisibility(View.VISIBLE);
            tvProgress.setVisibility(View.VISIBLE);
            tvProgress.setText("Mise à jour des données clients ...");
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            progressConnect.setVisibility(View.INVISIBLE);
            tvProgress.setVisibility(View.INVISIBLE);

            if (Utilities.isNetworkDataValid(data)) {

                try {

                    // Init model
                    DataStore.getInstance().initClientArray();

                    // Fill array with raw data (unsorted names)
                    JSONArray array = new JSONArray(data);
                    for (int i = 0; i < array.length(); i++) {
                        DataStore.getInstance().getClientItems().add(new ClientItem(array.getJSONObject(i)));
                    }

                    // Sort data by names
                    Collections.sort(DataStore.getInstance().getClientItems(), new Comparator<ClientItem>() {
                        @Override
                        public int compare(ClientItem lhs, ClientItem rhs) {
                            return lhs.getName().compareToIgnoreCase(rhs.getName());
                        }
                    });

                    // Finish connexion
                    Toast.makeText(LoginActivity.this, "Bienvenue, " +
                            DataStore.getInstance().getClubMember().getName() + " !", Toast.LENGTH_SHORT).show();
                    Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                    LoginActivity.this.startActivity(myIntent);
                    LoginActivity.this.finish();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(LoginActivity.this, "Erreur serveur !", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Erreur réseau !", Toast.LENGTH_SHORT).show();
            }
        }
    }

}
