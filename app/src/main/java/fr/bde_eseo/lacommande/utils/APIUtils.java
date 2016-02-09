package fr.bde_eseo.lacommande.utils;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Rascafr on 31/07/2015.
 * Use the class to connect to network (in AsyncTask)
 */
public class APIUtils {

    private static final String LOG_KEY_ERROR = "ConnexionUtils.ERROR";

    // Send data with POST method, and returns the server's response
    // V2.0 : no more '\n' char
    // V3.0 : android 6.0 support
    public static String postServerData(String sUrl, HashMap<String, String> postDataParams, Context ctx) {

        String result = "";
        URL url = null;

        if (Utilities.isOnline(ctx)) {
            try {
                url = new URL(sUrl);
                try {

                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setReadTimeout(10000);
                    httpURLConnection.setConnectTimeout(15000);
                    httpURLConnection.setDoInput(true);
                    httpURLConnection.setDoOutput(true);

                    if (postDataParams != null) {
                        OutputStream os = httpURLConnection.getOutputStream();
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                        writer.write(getPostDataString(postDataParams));

                        writer.flush();
                        writer.close();
                        os.close();
                    }
                    int responseCode = httpURLConnection.getResponseCode();

                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        String line;
                        BufferedReader br = new BufferedReader(new InputStreamReader(httpURLConnection.getInputStream()));
                        while ((line = br.readLine()) != null) {
                            result += line;
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private static String getPostDataString(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }

    public static APIResponse postAPIData (String sUrl, HashMap<String, String> postDataParams, Context ctx) {
        String str = postServerData(sUrl,postDataParams,ctx);
        APIResponse resp = new APIResponse();
        if (Utilities.isNetworkDataValid(str)) {
            try {
                JSONObject obj = new JSONObject(str);
                resp.setError(obj.getString("cause"));
                resp.setStatus(obj.getInt("status"));
                resp.setJsonData(obj.optJSONObject("data"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return resp;
    }
}
