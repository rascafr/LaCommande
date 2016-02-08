package fr.bde_eseo.lacommande.admin;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.model.ClubMember;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 07/02/2016.
 */
public class ParametersAsyncDialog extends AsyncTask<String, String, String> {

    private Context context;
    private MaterialDialog materialDialog;
    private ArrayList<LacmdParameter> parameters;
    private ClubMember clubMember;

    public ParametersAsyncDialog(Context context) {
        this.context = context;
        this.parameters = new ArrayList<>();
        this.clubMember = DataStore.getInstance().getClubMember();
    }

    @Override
    protected void onPreExecute() {
        materialDialog = new MaterialDialog.Builder(context)
                .title("Téléchargement des paramètres")
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
                        JSONArray array = new JSONArray(data);
                        for (int i = 0; i < array.length(); i++) {
                            parameters.add(new LacmdParameter(array.getJSONObject(i)));
                        }

                        // Show strings / values
                        CharSequence items[] = new CharSequence[parameters.size()];
                        Integer values[] = new Integer[parameters.size()];
                        for (int i = 0; i < parameters.size(); i++) {
                            items[i] = parameters.get(i).getName();
                            values[i] = parameters.get(i).isChecked() ? i : -1;
                        }

                        // Show dialog
                        new MaterialDialog.Builder(context)
                                .title("Paramètres")
                                .items(items)
                                .itemsCallbackMultiChoice(values, new MaterialDialog.ListCallbackMultiChoice() {
                                    @Override
                                    public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {
                                        /**
                                         * If you use alwaysCallMultiChoiceCallback(), which is discussed below,
                                         * returning false here won't allow the newly selected check box to actually be selected.
                                         * See the limited multi choice dialog example in the sample project for details.
                                         **/
                                        for (int i = 0; i < parameters.size(); i++) {
                                            parameters.get(i).setValue(false);
                                        }

                                        for (Integer aWhich : which) {
                                            Log.d("MD", "Callback : " + aWhich);
                                            if (aWhich != -1) {
                                                parameters.get(aWhich).setValue(true);
                                            }
                                        }

                                        new AsyncUpdateParameters().execute();

                                        return true;
                                    }
                                })
                                .positiveText("Valider")
                                .negativeText("Annuler")
                                .show();

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
                }
            }
        }, 1000);
    }

    @Override
    protected String doInBackground(String... params) {
        return ConnexionUtils.postServerData(Constants.API_PARAMETERS_LIST, null);
    }

    /**
     * Checkable item
     */
    private class LacmdParameter {
        private String id, name;
        private boolean value;

        public LacmdParameter(JSONObject obj) throws JSONException {
            this.id = obj.getString("id");
            this.name = obj.getString("name");
            this.value = obj.getBoolean("value");
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public int getValue() {
            return value ? 1 : 0;
        }

        public boolean isChecked() {
            return value;
        }

        public void setValue(boolean value) {
            this.value = value;
        }
    }

    /**
     * Send data thought server
     */
    public class AsyncUpdateParameters extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < parameters.size(); i++) {
                publishProgress(parameters.get(i).getName() + " - " + (i+1) + "/" + parameters.size());
                HashMap<String, String> pairs = new HashMap<>();
                pairs.put("login", clubMember.getLogin());
                pairs.put("password", clubMember.getPassword());
                pairs.put("parameter", parameters.get(i).getId());
                pairs.put("value", String.valueOf(parameters.get(i).getValue()));
                APIResponse response = APIUtils.postAPIData(Constants.API_PARAMETERS_UPDATE, pairs, context);
                if (!response.isValid()) {
                    return response.getError();
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            materialDialog.hide();
            materialDialog = new MaterialDialog.Builder(context)
                    .title("Mise à jour des paramètres")
                    .content("Veuillez patienter ...")
                    .progressIndeterminateStyle(true)
                    .progress(true, parameters.size(), false)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected void onPostExecute(String response) {
            materialDialog.hide();
            if (response != null) Toast.makeText(context, "Erreur : " + response, Toast.LENGTH_SHORT).show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            materialDialog.setContent("Veuillez patienter ...\n\n" + values[0]);
        }
    }
}
