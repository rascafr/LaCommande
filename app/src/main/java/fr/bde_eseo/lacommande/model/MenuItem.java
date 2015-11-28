package fr.bde_eseo.lacommande.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by Rascafr on 21/11/2015.
 */
public class MenuItem extends RootItem {

    public static final String ID_CAT = "cat_menus";
    public static final String TYPE = "menu";

    private ArrayList<String> mainElem;
    private int nbSandw, nbElems;

    public MenuItem (JSONObject obj) throws JSONException {
        super(
                obj.getString("idstr"),
                obj.getString("name"),
                obj.getDouble("price"),
                TYPE
        );

        mainElem = new ArrayList<>();
        StringTokenizer stringTokenizer = new StringTokenizer(obj.getString("mainElemStr"), "|");
        for (int i=0;i<stringTokenizer.countTokens();i++) {
            mainElem.add(stringTokenizer.nextToken());
        }

        this.nbSandw = obj.getInt("nbMainElem");
        this.nbElems = obj.getInt("nbSecoElem");

        Log.d("MENUS", getName() + ", " + getId() + ", " + getPrice() + ", " + nbSandw + ", " + nbElems);
    }

    public MenuItem (MenuItem menuItem) {
        super(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), TYPE);
        this.nbSandw = menuItem.nbSandw;
        this.nbElems = menuItem.nbElems;

        mainElem = new ArrayList<>();
        for (int i=0;i<menuItem.mainElem.size();i++) {
            this.mainElem.add(menuItem.mainElem.get(i));
        }
    }

    public ArrayList<String> getMainElem() {
        return mainElem;
    }

    public void setMainElem(ArrayList<String> mainElem) {
        this.mainElem = mainElem;
    }

    public int getNbSandw() {
        return nbSandw;
    }

    public void setNbSandw(int nbSandw) {
        this.nbSandw = nbSandw;
    }

    public int getNbElems() {
        return nbElems;
    }

    public void setNbElems(int nbElems) {
        this.nbElems = nbElems;
    }
}
