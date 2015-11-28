package fr.bde_eseo.lacommande;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import fr.bde_eseo.lacommande.model.ClientItem;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 11/11/2015.
 */
public class ClientListActivity extends AppCompatActivity {

    // UI Layout
    private ProgressBar progressClient;
    private RecyclerView recyList;

    // Toolbar
    private EditText etSearch;
    private ImageView imgClear;

    // Adapter
    private ClientItemAdapter mAdapter;

    // Model
    private ArrayList<ClientItem> displayItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set UI Main Layout
        setContentView(R.layout.activity_clients);

        // Get objects
        progressClient = (ProgressBar) findViewById(R.id.progressClients);
        recyList = (RecyclerView) findViewById(R.id.recyList);
        progressClient.setVisibility(View.INVISIBLE);

        // Init model
        displayItems = new ArrayList<>();

        // Assign text input to toolbar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setCustomView(R.layout.toolbar_search);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM
                | ActionBar.DISPLAY_SHOW_HOME);
        View actionBarView = actionBar.getCustomView();
        etSearch = (EditText) actionBarView.findViewById(R.id.etToolbar);
        imgClear = (ImageView) actionBarView.findViewById(R.id.crossClear);

        imgClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
                fillHeaderArray();
                mAdapter.notifyDataSetChanged();
            }
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (etSearch.getText().toString().length() == 0) {
                    imgClear.setVisibility(View.INVISIBLE);
                    fillHeaderArray();
                    mAdapter.notifyDataSetChanged();
                } else {
                    imgClear.setVisibility(View.VISIBLE);
                    searchInArray(s.toString());
                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        // Init model
        DataStore.getInstance().initClientArray();

        // Init adapter and recycler object
        mAdapter = new ClientItemAdapter();
        recyList.setAdapter(mAdapter);
        recyList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);

        // Download data from server
        AsyncLogin asyncLogin = new AsyncLogin();
        asyncLogin.execute(Constants.URL_CLIENTS_LISTS);

    }

    private class AsyncLogin extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... url) {
            return ConnexionUtils.postServerData(url[0], null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressClient.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            progressClient.setVisibility(View.INVISIBLE);
            if (Utilities.isNetworkDataValid(data)) {

                try {

                    // Fill array with raw data (unsorted names)
                    JSONArray array = new JSONArray(data);
                    for (int i=0;i<array.length();i++) {
                        DataStore.getInstance().getClientItems().add(new ClientItem(array.getJSONObject(i)));
                    }

                    // Sort data by names
                    Collections.sort(DataStore.getInstance().getClientItems(), new Comparator<ClientItem>() {
                        @Override
                        public int compare(ClientItem lhs, ClientItem rhs) {
                            return lhs.getName().compareToIgnoreCase(rhs.getName());
                        }
                    });

                    fillHeaderArray();

                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(ClientListActivity.this, "Erreur réseau !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class ClientItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_ITEM = 0;
        private static final int TYPE_HEADER = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {

                case TYPE_ITEM:
                    return new ClientItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client, parent, false));

                default:
                case TYPE_HEADER:
                    return new ClientHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_client_header, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ClientItem ci = displayItems.get(position);

            switch (getItemViewType(position)) {

                case TYPE_ITEM:

                    ClientItemViewHolder civh = (ClientItemViewHolder) holder;
                    civh.vName.setText(ci.getName() + " " + ci.getFirstname());
                    civh.vLogin.setText(ci.getLogin());
                    if (ci.isShowDivider()) {
                        civh.vDivier.setVisibility(View.VISIBLE);
                    } else {
                        civh.vDivier.setVisibility(View.GONE);
                    }

                    break;

                case TYPE_HEADER:

                    ClientHeaderViewHolder chvh = (ClientHeaderViewHolder) holder;
                    chvh.vLetter.setText(ci.getName());

                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return displayItems.get(position).isHeader() ? TYPE_HEADER : TYPE_ITEM;
        }


        @Override
        public int getItemCount() {
            return displayItems != null ? displayItems.size() : 0;
        }

        // Classic View Holder for client item
        public class ClientItemViewHolder extends RecyclerView.ViewHolder {

            protected TextView vLogin, vName;
            protected View vDivier;

            public ClientItemViewHolder(View v) {
                super(v);
                vLogin = (TextView) v.findViewById(R.id.clientLogin);
                vName = (TextView) v.findViewById(R.id.clientName);
                vDivier = v.findViewById(R.id.divider);
            }
        }

        // Classic View Holder for header item
        public class ClientHeaderViewHolder extends RecyclerView.ViewHolder {

            protected TextView vLetter;

            public ClientHeaderViewHolder(View v) {
                super(v);
                vLetter = (TextView) v.findViewById(R.id.clientHeaderLetter);
            }
        }
    }

    // Method to add headers and data into displayItems array
    private void fillHeaderArray () {

        String sLetter = "A";
        boolean bLetter = true;
        int cnt = 0;

        // Add headers into list
        displayItems.clear();
        int size = DataStore.getInstance().getClientItems().size();
        for (int i=0;i<size;i++) {
            ClientItem ci = DataStore.getInstance().getClientItems().get(i);

            if (bLetter || !ci.getName().startsWith(sLetter)) {
                if (cnt > 0) {
                    displayItems.get(cnt-1).setShowDivider(false);
                }
                sLetter = ci.getName().charAt(0) + "";
                displayItems.add(new ClientItem(sLetter));
                cnt++;
                bLetter = false;
            }

            displayItems.add(ci);
            cnt++;
        }

        /*
        for (int i=0;i<size;i++) {
            ClientItem ci = DataStore.getInstance().getClientItems().get(i);
            if (bLetter || !ci.getName().startsWith(sLetter)) {
                if (i > 0) {
                    displayItems.get(i - 1).setShowDivider(false);
                }
                sLetter = ci.getName().charAt(0) + "";
                displayItems.add(i, new ClientItem(sLetter));
                cnt++;
                bLetter = false;
            }
        }*/
    }

    // Restrict data in array (search mode)
    private void searchInArray (String search) {
        String sLow = search.toLowerCase(Locale.FRANCE);
        displayItems.clear();
        int size = DataStore.getInstance().getClientItems().size();
        for (int i=0;i<size;i++) {
            ClientItem ci = DataStore.getInstance().getClientItems().get(i);
            if (ci.getFullname().toLowerCase(Locale.FRANCE).contains(sLow)) {
                displayItems.add(ci);
            }
        }
    }
}
