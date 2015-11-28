package fr.bde_eseo.lacommande;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;

import fr.bde_eseo.lacommande.model.ClubMember;
import fr.bde_eseo.lacommande.model.DataStore;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set UI Main Layout
        setContentView(R.layout.activity_login);

        // Assign layout object
        bpConnect = (Button) findViewById(R.id.bpConnect);
        etLogin = (EditText) findViewById(R.id.etLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);
        progressConnect = (ProgressBar) findViewById(R.id.progressConnect);

        // Listen for connexion intent
        bpConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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
    private class AsyncConnectClub extends AsyncTask<String, String, String> {

        private String login;
        private String password;
        private String version;
        private String hash;

        @Override
        protected String doInBackground(String... params) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("login", login);
            pairs.put("password", password);
            pairs.put("version", version);
            pairs.put("hash", hash);

            if (Utilities.isOnline(LoginActivity.this)) {
                return ConnexionUtils.postServerData(Constants.URL_LOGIN_CLUB, pairs);
            } else {
                return null;
            }
        }

        @Override
        protected void onPreExecute() {
            login = etLogin.getText().toString().toLowerCase(Locale.FRANCE).trim();
            etLogin.setText(login);
            password = EncryptUtils.sha256(etPassword.getText().toString() + getResources().getString(R.string.salt_password));
            version = BuildConfig.VERSION_NAME;
            hash = EncryptUtils.sha256(login+password+version+getResources().getString(R.string.salt_login_club));
        }

        @Override
        protected void onPostExecute(String result) {

            String message = "Impossible d'accéder au réseau.\nVeuillez vérifier l'état de la connexion internet.";
            int error = 1;
            progressConnect.setVisibility(View.INVISIBLE);

            if (Utilities.isNetworkDataValid(result)) {
                if (result.startsWith("1")) {

                    try {
                        JSONObject obj = new JSONObject(result.substring(1));
                        DataStore.getInstance().setClubMember(new ClubMember(obj.getString("name"), login, password, obj.getInt("level")));
                        error = 0;
                        Toast.makeText(LoginActivity.this, "Bienvenue, " +
                                DataStore.getInstance().getClubMember().getName() + " !", Toast.LENGTH_SHORT).show();
                        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
                        LoginActivity.this.startActivity(myIntent);
                        LoginActivity.this.finish();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (result.equals("-1")) {
                    message = "Le mot de passe saisi est incorrect.";
                } else if (result.equals("-2")) {
                    message = "L'identifiant saisi est incorrect.";
                } else if (result.equals("-3")) {
                    message = "Le compte auquel vous tentez d'accéder n'est pas activé.";
                } else {
                    message = "Code d'erreur : inconnu.\nContactez un développeur !";
                }

            }

            if (error == 1) {
                new MaterialDialog.Builder(LoginActivity.this)
                        .title("Erreur")
                        .content(message)
                        .cancelable(false)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);
                            }
                        })
                        .negativeText("Fermer")
                        .show();
            }
        }
    }
}
