package fr.bde_eseo.lacommande.async;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.OrderGenericActivity;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;

/**
 * Created by Rascafr on 03/02/2016.
 */
public class AsyncToken extends AsyncTask<String, String, APIResponse> {

    private Context context;
    private String loginClub, passwordClub, loginClient, version, clientName;
    private MaterialDialog materialDialog;

    public AsyncToken(Context context, String loginClub, String passwordClub, String loginClient, String version, String clientName) {
        this.context = context;
        this.loginClub = loginClub;
        this.passwordClub = passwordClub;
        this.loginClient = loginClient;
        this.version = version;
        this.clientName = clientName;
    }

    @Override
    protected APIResponse doInBackground(String... url) {
        HashMap<String, String> pairs = new HashMap<>();
        pairs.put("loginClub", loginClub);
        pairs.put("passwordClub", passwordClub);
        pairs.put("loginClient", loginClient);
        pairs.put("version", version);

        return APIUtils.postAPIData(Constants.API_ORDER_PREPARE, pairs, context);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        materialDialog = new MaterialDialog.Builder(context)
                .title("Préparation")
                .content("Veuillez patienter ...")
                .cancelable(false)
                .progress(true, 4)
                .progressIndeterminateStyle(false)
                .show();
    }

    @Override
    protected void onPostExecute(final APIResponse apiResponse) {

        // Wait a little
        new Handler().postDelayed(
                new Runnable() {

                    @Override
                    public void run() {

                        materialDialog.hide();

                        if (apiResponse.isValid()) {

                            try {
                                JSONObject obj = apiResponse.getJsonData();

                                DataStore.getInstance().setToken(obj.getString("token"));
                                Intent i = new Intent(context, OrderGenericActivity.class);
                                i.putExtra(Constants.KEY_NEW_ORDER_CLIENT, clientName);
                                context.startActivity(i);

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            materialDialog = new MaterialDialog.Builder(context)
                                    .title("Erreur")
                                    .content(apiResponse.getError())
                                    .cancelable(false)
                                    .negativeText("Fermer")
                                    .show();
                        }
                    }
                }, 1500);
    }
}