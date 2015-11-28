package fr.bde_eseo.lacommande.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 21/11/2015.
 */
public class CategoryItem {

    private String name, catname;

    public CategoryItem(String name, String catname) {
        this.name = name;
        this.catname = catname;
    }

    public CategoryItem(JSONObject obj) throws JSONException {
        this.name = obj.getString("name");
        this.catname = obj.getString("catname");
        Log.d("JSON", "ADD Category : " + name + " , " + catname);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCatname() {
        return catname;
    }

    public void setCatname(String catname) {
        this.catname = catname;
    }
}
