package fr.bde_eseo.lacommande.model;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rascafr on 11/11/2015.
 */
public class ClientItem {

    private String login, fullname, firstname, name;
    private boolean isHeader, showDivider;

    public ClientItem(String login, String fullname) {
        this.login = login;
        this.fullname = fullname;
        this.showDivider = true;
        presentByFamilyName();
    }

    public ClientItem (String name) {
        this.name = name;
        this.isHeader = true;
    }

    public ClientItem(JSONObject obj) throws JSONException {
        this.login = obj.getString("login");
        this.fullname = obj.getString("fullname");
        this.showDivider = true;
        presentByFamilyName();
    }

    public String getLogin() {
        return login;
    }

    public String getFullname() {
        return fullname;
    }

    public void presentByFamilyName () {
        int ind = fullname.indexOf(" - ");
        if (ind != -1) {
            fullname = fullname.replace(" - ", "-");
        }
        ind = fullname.indexOf(" ");
        firstname = fullname.substring(0, ind);
        name = fullname.substring(ind+1);
    }

    public String getFirstname() {
        return firstname;
    }

    public String getName() {
        return name;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public boolean isShowDivider() {
        return showDivider;
    }

    public void setShowDivider(boolean showDivider) {
        this.showDivider = showDivider;
    }
}
