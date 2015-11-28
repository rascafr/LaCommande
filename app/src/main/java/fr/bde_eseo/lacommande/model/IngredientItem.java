package fr.bde_eseo.lacommande.model;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 21/11/2015.
 */
public class IngredientItem extends RootItem {

    public static final String TYPE = "ingredient";

    public IngredientItem(JSONObject obj) throws JSONException {
        super(
                obj.getString("idstr"),
                obj.getString("name"),
                obj.getDouble("priceuni"),
                TYPE
        );
    }
}
