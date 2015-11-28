package fr.bde_eseo.lacommande.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 21/11/2015.
 */
public class ElementItem extends RootItem {

    private String idcat;
    private double pricemore;
    private boolean outOfMenu;
    private boolean inStock;
    private int ingredients;
    public static final String TYPE = "element";

    public ElementItem(JSONObject obj) throws JSONException {
        super(
                obj.getString("idstr"),
                obj.getString("name"),
                obj.getDouble("priceuni"),
                TYPE
        );

        this.idcat = obj.getString("idcat");
        this.pricemore = obj.getDouble("pricemore");
        this.outOfMenu = obj.getInt("outofmenu") == 1;
        this.inStock = obj.getInt("stock") != 0;
        this.ingredients = obj.getInt("hasingredients");

        Log.d("JSON", "ADD Element : " + getId() + " - " + getName() + " - " + getPrice() + "€");
    }

    public ElementItem(ElementItem elementItem) {
        super(elementItem.getId(), elementItem.getName(), elementItem.getPrice(), TYPE);
        idcat = elementItem.idcat;
        pricemore = elementItem.pricemore;
        outOfMenu = elementItem.outOfMenu;
        inStock = elementItem.inStock;
        ingredients = elementItem.ingredients;
    }

    public String getIdcat() {
        return idcat;
    }

    public int getIngredients() {
        return ingredients;
    }

    public boolean isInStock() {
        return inStock;
    }

    public double getPricemore() {
        return pricemore;
    }

    public boolean isOutOfMenu() {
        return outOfMenu;
    }
}
