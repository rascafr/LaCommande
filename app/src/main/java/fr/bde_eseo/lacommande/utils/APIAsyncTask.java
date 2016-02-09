package fr.bde_eseo.lacommande.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.util.HashMap;

/**
 * Created by Rascafr on 09/02/2016.
 * Custom definition of AsyncTask to make API communication easier
 */
public class APIAsyncTask extends AsyncTask<String,String,APIResponse> {

    protected HashMap<String,String> pairs;
    protected Context context;

    public APIAsyncTask (Context context) {
        this.context = context;
        this.pairs = new HashMap<>();
    }

    @Override
    protected APIResponse doInBackground(String... url) {
        return APIUtils.postAPIData(url[0], pairs, context);
    }
}
