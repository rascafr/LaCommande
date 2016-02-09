package fr.bde_eseo.lacommande.async;

import android.content.Context;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONException;

import fr.bde_eseo.lacommande.model.ClubMember;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.APIAsyncTask;
import fr.bde_eseo.lacommande.utils.APIResponse;

/**
 * Created by Rascafr on 09/02/2016.
 * Ferme la caféteria côté serveur en supprimant les commandes n'ayant pas aboutit / en les marquant comme impayées si non réglées
 */
public class AsyncLock extends APIAsyncTask {

    private ClubMember clubMember;
    private MaterialDialog materialDialog;

    public AsyncLock(Context context) {
        super(context);
        clubMember = DataStore.getInstance().getClubMember();
        pairs.put("login", clubMember.getLogin());
        pairs.put("password", clubMember.getPassword());
    }

    @Override
    protected void onPreExecute() {
        materialDialog = new MaterialDialog.Builder(context)
                .title("Fermeture de la cafet")
                .content("Opération en cours ...")
                .cancelable(false)
                .progress(true, 0)
                .progressIndeterminateStyle(false)
                .show();
    }

    @Override
    protected void onPostExecute(APIResponse apiResponse) {
        materialDialog.hide();

        if (apiResponse.isValid()) {
            try {
                int unpaid = apiResponse.getJsonData().getInt("unpaid");
                materialDialog = new MaterialDialog.Builder(context)
                        .title("Caféteria fermée")
                        .content(unpaid == 0 ? "Aucune commande impayée" : unpaid + " commande" + (unpaid == 1 ? "":"s") + " impayée"  + (unpaid == 1 ? "":"s"))
                        .negativeText("Fermer")
                        .show();
            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, apiResponse.getError(), Toast.LENGTH_SHORT).show();
        }
    }
}
