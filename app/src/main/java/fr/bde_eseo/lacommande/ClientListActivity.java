package fr.bde_eseo.lacommande;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;

import fr.bde_eseo.lacommande.async.AsyncToken;
import fr.bde_eseo.lacommande.model.ClientItem;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 11/11/2015.
 */
public class ClientListActivity extends AppCompatActivity {

    // UI Layout
    private ProgressBar progressClient;
    private RecyclerView recyList;
    private FloatingActionButton fab;

    // Toolbar
    private EditText etSearch;
    private ImageView imgClear;

    // Adapter
    private ClientItemAdapter mAdapter;

    // Model
    private ArrayList<ClientItem> displayItems;

    // Android
    private Context context;

    // Local dialog for client add
    private MaterialDialog newClientDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = ClientListActivity.this;

        // Set UI Main Layout
        setContentView(R.layout.activity_clients);

        // Arrow back to main activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Test");

        // Get objects
        progressClient = (ProgressBar) findViewById(R.id.progressClients);
        recyList = (RecyclerView) findViewById(R.id.recyList);
        progressClient.setVisibility(View.INVISIBLE);
        fab = (FloatingActionButton) findViewById(R.id.fab);

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
        //DataStore.getInstance().initClientArray();

        // Init adapter and recycler object
        mAdapter = new ClientItemAdapter();
        recyList.setAdapter(mAdapter);
        recyList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);

        // Attach floating button
        fab.attachToRecyclerView(recyList);

        // New client listener
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newClientDialog = new MaterialDialog.Builder(context)
                        .title("Ajouter un nouveau client")
                        .customView(R.layout.dialog_new_client, true)
                        .positiveText("Créer")
                        .negativeText("Annuler")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                View view = materialDialog.getCustomView();
                                EditText etName = (EditText) view.findViewById(R.id.etClientName);
                                EditText etLogin = (EditText) view.findViewById(R.id.etClientLogin);
                                AsyncAddClient asyncAddClient = new AsyncAddClient(
                                        etName.getText().toString(),
                                        etLogin.getText().toString()
                                );
                                asyncAddClient.execute();
                            }
                        })
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                materialDialog.hide();
                            }
                        })
                        .cancelable(false)
                        .autoDismiss(false)
                        .show();
            }
        });

        // Download data from server
        fillHeaderArray();
        mAdapter.notifyDataSetChanged();

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
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            final ClientItem ci = displayItems.get(position);

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

                    // On click → dialog
                    civh.llClient.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new MaterialDialog.Builder(context)
                                    .title("Action client")
                                    .content("Que souhaitez vous faire pour " + ci.getFullname() + " ?")
                                    .positiveText("Passer commande")
                                    .negativeText("Annuler")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                            AsyncToken asyncToken = new AsyncToken(
                                                    context,
                                                    DataStore.getInstance().getClubMember().getLogin(),
                                                    DataStore.getInstance().getClubMember().getPassword(),
                                                    displayItems.get(position).getLogin(),
                                                    BuildConfig.VERSION_NAME,
                                                    displayItems.get(position).getFullname()
                                            );
                                            asyncToken.execute();
                                        }
                                    })
                                    .show();
                        }
                    });

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
            protected LinearLayout llClient;

            public ClientItemViewHolder(View v) {
                super(v);
                vLogin = (TextView) v.findViewById(R.id.clientLogin);
                vName = (TextView) v.findViewById(R.id.clientName);
                llClient = (LinearLayout) v.findViewById(R.id.llClient);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                ClientListActivity.this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    // Asynctask to insert client into database
    private class AsyncAddClient extends AsyncTask<String,String,APIResponse> {

        private String name, login;
        private MaterialDialog progressDialog;

        public AsyncAddClient(String name, String login) {
            try {
                this.name = Base64.encodeToString(name.getBytes("UTF-8"), Base64.NO_WRAP);
                this.login = Base64.encodeToString(login.getBytes("UTF-8"), Base64.NO_WRAP);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPreExecute() {
            progressDialog = new MaterialDialog.Builder(context)
                    .title("Veuillez patienter")
                    .content("L'opération est en cours ...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected void onPostExecute(APIResponse apiResponse) {

            progressDialog.hide();

            if (apiResponse.isValid()) {
                if (newClientDialog != null) newClientDialog.hide();
                try {

                    // On récupère le nom / login du client ajouté pour ensuite modifier le dataset (plus écolo que de tout recharger)
                    final String insertedLogin = apiResponse.getJsonData().getString("inserted_login");
                    final String insertedName = apiResponse.getJsonData().getString("inserted_name");

                    // Dialogue de confirmation
                    progressDialog = new MaterialDialog.Builder(context)
                            .title("Client ajouté !")
                            .content("Le client " + insertedName + " a été ajouté avec le login \"" + insertedLogin + "\"\nQue souhaitez-vous faire maintenant ?")
                            .positiveText("Passer commande")
                            .negativeText("Fermer")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    AsyncToken asyncToken = new AsyncToken(
                                            context,
                                            DataStore.getInstance().getClubMember().getLogin(),
                                            DataStore.getInstance().getClubMember().getPassword(),
                                            insertedLogin,
                                            BuildConfig.VERSION_NAME,
                                            insertedName
                                    );
                                    asyncToken.execute();
                                }
                            })
                            .show();

                    // Modification du dataset général
                    DataStore.getInstance().getClientItems().add(new ClientItem(insertedLogin, insertedName));
                    DataStore.getInstance().sortClientArray();

                    // Rechargement du dataset d'affichage
                    fillHeaderArray();
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                new MaterialDialog.Builder(context)
                        .title("Erreur")
                        .content(apiResponse.getError())
                        .cancelable(false)
                        .negativeText("Fermer")
                        .show();
            }
        }

        @Override
        protected APIResponse doInBackground(String... params) {
            HashMap<String,String> pairs = new HashMap<>();
            pairs.put("login", DataStore.getInstance().getClubMember().getLogin());
            pairs.put("password", DataStore.getInstance().getClubMember().getPassword());
            pairs.put("base64name", name);
            pairs.put("base64login", login);
            return APIUtils.postAPIData(Constants.API_CLIENT_ADD, pairs, context);
        }
    }

}
