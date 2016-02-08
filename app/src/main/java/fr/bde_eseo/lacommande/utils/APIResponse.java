package fr.bde_eseo.lacommande.utils;

import org.json.JSONObject;

/**
 * Created by Rascafr on 14/01/2016.
 */
public class APIResponse {

    private int status;
    private String error;
    private JSONObject jsonData;

    public APIResponse(int status, String error, JSONObject jsonData) {
        this.status = status;
        this.error = error;
        this.jsonData = jsonData;
    }

    public APIResponse() {
        this.status = 0;
        this.error = "Réseau indisponible";
    }

    public boolean isValid() {
        return status == 1;
    }

    public boolean isNetworkBad() {
        return status == 0;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public JSONObject getJsonData() {
        return jsonData;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setJsonData(JSONObject jsonData) {
        this.jsonData = jsonData;
    }
}
