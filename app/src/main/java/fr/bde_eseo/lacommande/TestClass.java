package fr.bde_eseo.lacommande;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import fr.bde_eseo.lacommande.utils.APIAsyncTask;

/**
 * Created by Rascafr on 09/02/2016.
 */
public class TestClass extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private class MyImplementationOfAPIAsyncTask extends APIAsyncTask {

        public MyImplementationOfAPIAsyncTask (Context context) {
            super(context);
        }

    }
}
