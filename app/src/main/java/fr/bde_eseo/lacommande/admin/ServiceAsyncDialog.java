package fr.bde_eseo.lacommande.admin;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.text.InputType;
import android.util.Base64;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.model.ClubMember;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 08/02/2016.
 */
public class ServiceAsyncDialog extends AsyncTask<String, String, String> {

    private Context context;
    private MaterialDialog materialDialog;
    private ClubMember clubMember;

    public ServiceAsyncDialog(Context context) {
        this.context = context;
        this.clubMember = DataStore.getInstance().getClubMember();
    }

    @Override
    protected void onPreExecute() {
        materialDialog = new MaterialDialog.Builder(context)
                .title("Téléchargement du message")
                .content("Veuillez patienter ...")
                .cancelable(false)
                .progress(true, 0, false)
                .progressIndeterminateStyle(false)
                .show();
    }

    @Override
    protected void onPostExecute(final String data) {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                materialDialog.hide();

                if (Utilities.isNetworkDataValid(data)) {
                    try {

                        // List parameters
                        JSONObject obj = new JSONObject(data);
                        String service = obj.getString("service_dynamic");

                        // Show dialog
                        new MaterialDialog.Builder(context)
                                .title("Message de service")
                                .content("Inscrivez le message de service dans la case ci-dessous.\nNote : écrivez \"\\n\" pour effectuer un retour à la ligne.")
                                .inputType(InputType.TYPE_TEXT_VARIATION_LONG_MESSAGE)
                                .input("Exemple : Les nouvelles pizzas sont arrivées !", service, new MaterialDialog.InputCallback() {
                                    @Override
                                    public void onInput(MaterialDialog dialog, CharSequence input) {
                                        new AsyncUpdateService(input.toString()).execute();
                                    }
                                }).show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            }
        }, 500);
    }

    @Override
    protected String doInBackground(String... params) {
        return ConnexionUtils.postServerData(Constants.API_SERVICE_READ, null);
    }

    /**
     * Send data thought server
     */
    public class AsyncUpdateService extends AsyncTask<String, String, APIResponse> {

        private String service;

        public AsyncUpdateService (String service) {
            this.service = service;
        }

        @Override
        protected APIResponse doInBackground(String... params) {

            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("login", clubMember.getLogin());
            pairs.put("password", clubMember.getPassword());
            try {
                pairs.put("service", Base64.encodeToString(service.getBytes("UTF-8"), Base64.NO_WRAP));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return APIUtils.postAPIData(Constants.API_SERVICE_UPDATE, pairs, context);
        }

        @Override
        protected void onPreExecute() {
            materialDialog.hide();
            materialDialog = new MaterialDialog.Builder(context)
                    .title("Mise à jour des paramètres")
                    .content("Veuillez patienter ...")
                    .progressIndeterminateStyle(false)
                    .progress(true, 0, false)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected void onPostExecute(APIResponse response) {
            materialDialog.hide();
            if (!response.isValid()) Toast.makeText(context, "Erreur : " + response.getError(), Toast.LENGTH_SHORT).show();
            else Toast.makeText(context, "Données synchronisées !", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            materialDialog.setContent("Veuillez patienter ...");
        }
    }
}
