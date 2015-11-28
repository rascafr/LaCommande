package fr.bde_eseo.lacommande.admin;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.EncryptUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 26/10/2015.
 */
public class ClubListActivity extends AppCompatActivity {

    // Model
    private ArrayList<ClubItem> clubItems;

    // UI Layout
    private ProgressBar progressLoad;
    private RecyclerView recyList;
    private ClubAdapter mAdapter;

    // Floating Action Buttons
    private FloatingActionButton fabAdd;
    private FloatingActionButton fabSave;

    // Add dialog UI layout
    private EditText etAddLogin, etAddName, etAddPassword;
    private CheckBox checkAddAdmin, checkAddPassword, checkAddEnabled;
    private RelativeLayout bpAddRandom;

    // JSON data as String
    private String clubJSONstr;

    // Modification happened ?
    private boolean hasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set UI Main Layout
        setContentView(R.layout.activity_club_list);

        // Arrow back to main activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get UI Objects
        progressLoad = (ProgressBar) findViewById(R.id.progressLoad);
        recyList = (RecyclerView) findViewById(R.id.recyList);
        progressLoad.setVisibility(View.GONE);
        recyList.setVisibility(View.GONE);
        fabAdd = (FloatingActionButton) findViewById(R.id.fabAdd);
        fabSave = (FloatingActionButton) findViewById(R.id.fabSave);

        // Prepare model
        clubItems = new ArrayList<>();

        // Set adapter with ListView
        mAdapter = new ClubAdapter();
        recyList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Download data and fill array
        AsyncClubs asyncClubs = new AsyncClubs();
        asyncClubs.execute(Constants.URL_CLUBS_LISTS);

        // Add new club action
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MaterialDialog dialog = new MaterialDialog.Builder(ClubListActivity.this)
                        .title("Créer un compte")
                        .customView(R.layout.dialog_club, true)
                        .positiveText("Valider")
                        .negativeText("Annuler")
                        .neutralText("Partager")
                        .cancelable(false)
                        .autoDismiss(false)

                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {

                                etAddLogin.setText(etAddLogin.getText().toString().trim().toLowerCase(Locale.FRANCE));

                                if (!isEditTextValueOk(etAddLogin, etAddName, etAddPassword)) {

                                    Toast.makeText(ClubListActivity.this, "Merci de bien vouloir renseigner tous les champs !", Toast.LENGTH_SHORT).show();

                                } else if (isLoginAvailable(etAddLogin.getText().toString(), "")) {
                                    super.onPositive(dialog);

                                    // Add club to dataset
                                    clubItems.add(
                                            new ClubItem(
                                                    etAddName.getText().toString(),
                                                    etAddLogin.getText().toString(),
                                                    EncryptUtils.sha256(etAddPassword.getText().toString() + getResources().getString(R.string.salt_password)),
                                                    "0000-00-00 00:00:00",
                                                    checkAddEnabled.isChecked(),
                                                    checkAddAdmin.isChecked() ? 1 : 0
                                            )
                                    );

                                    reOrderDataSet();
                                    mAdapter.notifyDataSetChanged();

                                    Toast.makeText(ClubListActivity.this, "Club ajouté !", Toast.LENGTH_SHORT).show();

                                    // Flag dataset changed
                                    hasChanged = true;

                                    // Quit dialog
                                    dialog.dismiss();
                                } else {
                                    Toast.makeText(ClubListActivity.this, "Cet identifiant est déjà utilisé !", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onNegative(MaterialDialog dialog) {
                                super.onNegative(dialog);

                                // Quit dialog
                                dialog.dismiss();
                            }

                            @Override
                            public void onNeutral(MaterialDialog dialog) {
                                super.onNeutral(dialog);
                                String shareBody = "Votre compte \"" + etAddName.getText().toString() +
                                        "\" :\n\nLogin : " + etAddLogin.getText().toString() +
                                        "\nMot de passe : " + (etAddPassword.getText().toString().length() == 0 ? "Inchangé" : etAddPassword.getText().toString());

                                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                sharingIntent.setType("text/plain");
                                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[ESEOmega] Rappel identifiants club");
                                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
                            }
                        })
                        .build();

                View view = dialog.getCustomView();
                etAddName = (EditText) view.findViewById(R.id.etClubName);
                etAddLogin = (EditText) view.findViewById(R.id.etClubLogin);
                etAddPassword = (EditText) view.findViewById(R.id.etClubPassword);
                checkAddAdmin = (CheckBox) view.findViewById(R.id.checkAdmin);
                checkAddEnabled = (CheckBox) view.findViewById(R.id.checkEnabled);
                checkAddPassword = (CheckBox) view.findViewById(R.id.checkPassword);
                bpAddRandom = (RelativeLayout) view.findViewById(R.id.rlRandom);
                etAddPassword.setHint("Nouveau mot de passe");
                checkAddPassword.setChecked(false);
                checkAddAdmin.setChecked(false);
                checkAddEnabled.setChecked(true);
                checkAddPassword.setEnabled(false);

                etAddPassword.addTextChangedListener(
                        new TextWatcher(){

                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                if (etAddPassword.getText().toString().length() > 0) {
                                    checkAddPassword.setEnabled(true);
                                } else {
                                    checkAddPassword.setEnabled(false);
                                }
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        }
                );

                bpAddRandom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        etAddPassword.setText(EncryptUtils.latinPassword(ClubListActivity.this, 2));
                        etAddPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        checkAddPassword.setChecked(true);
                    }
                });

                checkAddPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            etAddPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        } else {
                            etAddPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        }
                    }
                });

                dialog.show();
            }
        });

        // Save data into server
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create JSON String
                clubJSONstr = "";
                for (int i=0;i<clubItems.size();i++) {
                    if (!clubItems.get(i).isHeader()) {
                        if (clubJSONstr.length() > 0) {
                            clubJSONstr += ",";
                        }
                        clubJSONstr += clubItems.get(i).toJSONstr();
                    }
                }
                clubJSONstr = "[" + clubJSONstr + "]";

                // Send data to server
                AsyncServer asyncServer = new AsyncServer();
                asyncServer.execute(Constants.URL_CLUBS_SYNC);
            }
        });

    }

    @Override
    public void onBackPressed() {

        if (hasChanged) {
            new MaterialDialog.Builder(ClubListActivity.this)
                    .title("Quitter ?")
                    .content("Vos modifications n'ont pas été sauvegardées sur le serveur !")
                    .positiveText("Quitter")
                    .negativeText("Annuler")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            hasChanged = false;
                            ClubListActivity.this.onBackPressed();
                        }

                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Custom Async task to send data to server
     */
    private class AsyncServer extends AsyncTask<String, String, String> {

        private MaterialDialog dialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new MaterialDialog.Builder(ClubListActivity.this)
                    .title("Mise à jour")
                    .content("Veuillez patienter ...")
                    .cancelable(false)
                    .progressIndeterminateStyle(false)
                    .progress(true, 4, false)
                    .show();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            dialog.hide();

            if (Utilities.isNetworkDataValid(data) && data.startsWith("1")) {
                Toast.makeText(ClubListActivity.this, "Données synchronisées !", Toast.LENGTH_SHORT).show();

                // Flag dataset cleared
                hasChanged = false;

                // Quit app
                ClubListActivity.this.onBackPressed();

            } else {
                Toast.makeText(ClubListActivity.this, "Échec de la synchronisation", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... url) {
            HashMap<String, String> pairs = new HashMap<>();
            try {
                String b64data = Base64.encodeToString(clubJSONstr.getBytes("UTF-8"), Base64.NO_WRAP);
                pairs.put("data", b64data);
                pairs.put("hash", EncryptUtils.md5(b64data + getResources().getString(R.string.salt_sync_club)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return ConnexionUtils.postServerData(url[0], pairs);
        }
    }


    /**
     * Custom Async task downloader
     */
    private class AsyncClubs extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (clubItems == null)
                clubItems = new ArrayList<>();
            else
                clubItems.clear();

            progressLoad.setVisibility(View.VISIBLE);
            recyList.setVisibility(View.GONE);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            progressLoad.setVisibility(View.GONE);

            if (Utilities.isNetworkDataValid(data)) {
                try {
                    JSONArray jsonArray = new JSONArray(data);
                    for (int i = 0; i < jsonArray.length(); i++) {
                        clubItems.add(new ClubItem(jsonArray.getJSONObject(i)));
                    }
                    reOrderDataSet();

                    mAdapter.notifyDataSetChanged();
                    recyList.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(ClubListActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(ClubListActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... url) {
            return ConnexionUtils.postServerData(url[0], null);
        }
    }

    /**
     * Check edittext's content
     */
    private boolean isEditTextValueOk (EditText etLogin, EditText etName, EditText etPassword) {
        return etName.getText().toString().length() > 0 && etPassword.getText().toString().length() > 0 && etLogin.getText().toString().length() > 0;
    }

    /**
     * Check doublons
     */
    private boolean isLoginAvailable (String login, String currentLogin) {
        boolean exists = false;
        for (int i=0;i<clubItems.size();i++) {
            if (!clubItems.get(i).isHeader() &&
                    clubItems.get(i).getLogin().equalsIgnoreCase(login) &&
                    !clubItems.get(i).getLogin().equalsIgnoreCase(currentLogin)
                    ) {
                exists = true;
            }
        }
        return !exists;
    }

    /**
     * Function to order dataset
     */
    private void reOrderDataSet() {
        ArrayList<ClubItem> copy = new ArrayList<>();
        copy.add(new ClubItem("COMPTES ACTIVÉS"));
        for (int i=0;i<clubItems.size();i++) {
            if (clubItems.get(i).isEnabled() && !clubItems.get(i).isHeader()) {
                copy.add(clubItems.get(i));
            }
        }
        copy.add(new ClubItem("COMPTES DÉSACTIVÉS"));
        for (int i=0;i<clubItems.size();i++) {
            if (!clubItems.get(i).isEnabled() && !clubItems.get(i).isHeader()) {
                copy.add(clubItems.get(i));
            }
        }
        clubItems = copy;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                ClubListActivity.this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**
     * Custom definition to handle club model
     */
    private class ClubItem {
        private String name, login, password, lastconnect;
        private boolean isHeader, enabled;
        private int level;

        // For header
        public ClubItem(String name) {
            this.name = name;
            this.isHeader = true;
        }

        // For club item
        public ClubItem(String name, String login, String password, String lastconnect, boolean enabled, int level) {
            this.name = name;
            this.login = login;
            this.password = password;
            this.lastconnect = lastconnect;
            this.enabled = enabled;
            this.level = level;
            this.isHeader = false;
        }

        // For club item with JSON data
        public ClubItem(JSONObject obj) throws JSONException {
            this(
                    obj.getString("name"),
                    obj.getString("idclub"),
                    obj.getString("password"),
                    obj.getString("lastconnect"),
                    obj.getInt("enabled") == 1,
                    obj.getInt("level")
            );
        }

        public boolean isAdmin () {
            return login.equalsIgnoreCase("admin");
        }

        public String getName() {
            return name;
        }

        public String getLogin() {
            return login;
        }

        public String getPassword() {
            return password;
        }

        public String getLastconnect() {
            return lastconnect;
        }

        public boolean isHeader() {
            return isHeader;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public int getLevel() {
            return level;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setLogin(String login) {
            this.login = login;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setLastconnect(String lastconnect) {
            this.lastconnect = lastconnect;
        }

        public void setIsHeader(boolean isHeader) {
            this.isHeader = isHeader;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public String toJSONstr () {
            String json = "{";
            json += "\"idclub\":\"" + this.login + "\",";
            json += "\"password\":\"" + this.password + "\",";
            json += "\"name\":\"" + this.name + "\",";
            json += "\"lastconnect\":\"" + this.lastconnect + "\",";
            json += "\"enabled\":\"" + (this.enabled ? 1:0) + "\",";
            json += "\"level\":\"" + this.level + "\"";
            json += "}";

            return json;
        }
    }

    /**
     * Custom definition for recycler view's adapter
     */
    private class ClubAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_CLUB_ITEM = 0;
        private final static int TYPE_CLUB_HEADER = 1;
        private EditText etName, etLogin, etPassword;
        private CheckBox checkAdmin, checkEnabled, checkPassword;
        private RelativeLayout bpRandom;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            switch (viewType) {
                case TYPE_CLUB_ITEM:
                    return new ClubItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_club_item, parent, false));

                default:
                case TYPE_CLUB_HEADER:
                    return new ClubHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_club, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final ClubItem ci = clubItems.get(position);

            switch (getItemViewType(position)) {
                case TYPE_CLUB_ITEM:

                    final ClubItemViewHolder civh = (ClubItemViewHolder) holder;
                    civh.vTitle.setText(ci.getName());
                    civh.vDesc.setText((ci.getLevel() > 0 ? "Administrateur" : "Compte normal") + " • Dernière connexion : " + ci.getLastconnect());

                    // Modify Listener
                    civh.vModify.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            civh.vModify.setTextColor(0xffff9060);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    civh.vModify.setTextColor(0xff747474);
                                }
                            }, 150);

                            // Un club non administrateur tente de modifier le compte administrateur
                            if (ci.isAdmin() && !DataStore.getInstance().getClubMember().isAdmin()) {
                                Toast.makeText(ClubListActivity.this, "Vous n'avez pas le droit de modifier le compte administrateur.", Toast.LENGTH_SHORT).show();
                            } else {

                                MaterialDialog dialog = new MaterialDialog.Builder(ClubListActivity.this)
                                        .title("Modifier le compte")
                                        .customView(R.layout.dialog_club, true)
                                        .positiveText("Valider")
                                        .negativeText("Annuler")
                                        .neutralText("Partager")
                                        .cancelable(false)
                                        .autoDismiss(false)
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {

                                                etLogin.setText(etLogin.getText().toString().trim().toLowerCase(Locale.FRANCE));

                                                if (!isEditTextValueOk(etLogin, etName, etName)) {

                                                    Toast.makeText(ClubListActivity.this, "Merci de bien vouloir renseigner tous les champs !", Toast.LENGTH_SHORT).show();

                                                } else if (isLoginAvailable(etLogin.getText().toString(), clubItems.get(position).getLogin())) {
                                                    super.onPositive(dialog);
                                                    clubItems.get(position).setName(etName.getText().toString());
                                                    clubItems.get(position).setLogin(etLogin.getText().toString());

                                                    if (etPassword.getText().toString().length() > 0) {
                                                        clubItems.get(position).setPassword(EncryptUtils.sha256(etPassword.getText().toString() + getResources().getString(R.string.salt_password)));
                                                    } else {
                                                        clubItems.get(position).setPassword(ci.getPassword());
                                                    }

                                                    clubItems.get(position).setEnabled(checkEnabled.isChecked());
                                                    clubItems.get(position).setLevel(checkAdmin.isChecked() ? 1 : 0);

                                                    reOrderDataSet();
                                                    mAdapter.notifyDataSetChanged();

                                                    Toast.makeText(ClubListActivity.this, "Modifications enregistrées !", Toast.LENGTH_SHORT).show();

                                                    // Flag dataset changed
                                                    hasChanged = true;

                                                    // Quit dialog
                                                    dialog.dismiss();
                                                } else {
                                                    Toast.makeText(ClubListActivity.this, "Cet identifiant est déjà utilisé !", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onNegative(MaterialDialog dialog) {
                                                super.onNegative(dialog);

                                                // Quit dialog
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onNeutral(MaterialDialog dialog) {
                                                super.onNeutral(dialog);
                                                String shareBody = "Votre compte \"" + etName.getText().toString() +
                                                        "\" :\n\nLogin : " + etLogin.getText().toString() +
                                                        "\nMot de passe : " + (etPassword.getText().toString().length() == 0 ? "Inchangé" : etPassword.getText().toString());

                                                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                                                sharingIntent.setType("text/plain");
                                                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[ESEOmega] Rappel identifiants club");
                                                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                                                startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)));
                                            }
                                        })
                                        .build();

                                View view = dialog.getCustomView();
                                etName = (EditText) view.findViewById(R.id.etClubName);
                                etLogin = (EditText) view.findViewById(R.id.etClubLogin);
                                etPassword = (EditText) view.findViewById(R.id.etClubPassword);
                                checkAdmin = (CheckBox) view.findViewById(R.id.checkAdmin);
                                checkEnabled = (CheckBox) view.findViewById(R.id.checkEnabled);
                                checkPassword = (CheckBox) view.findViewById(R.id.checkPassword);
                                bpRandom = (RelativeLayout) view.findViewById(R.id.rlRandom);
                                etName.setText(ci.getName());
                                etLogin.setText(ci.getLogin());
                                etPassword.setHint("Nouveau mot de passe");
                                checkAdmin.setChecked(ci.getLevel() != 0);
                                checkEnabled.setChecked(ci.isEnabled());

                                // Le compte administrateur est forcément administrateur et activé
                                // Seul le mot de passe peut être changé
                                if (ci.isAdmin()) {
                                    checkAdmin.setEnabled(false);
                                    checkEnabled.setEnabled(false);
                                    etLogin.setEnabled(false);
                                    etName.setEnabled(false);
                                }

                                if (etPassword.getText().toString().length() == 0) {
                                    checkPassword.setEnabled(false);
                                }

                                etPassword.addTextChangedListener(
                                        new TextWatcher() {

                                            @Override
                                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                                            }

                                            @Override
                                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                                if (etPassword.getText().toString().length() > 0) {
                                                    checkPassword.setEnabled(true);
                                                } else {
                                                    checkPassword.setEnabled(false);
                                                }
                                            }

                                            @Override
                                            public void afterTextChanged(Editable s) {

                                            }
                                        }
                                );

                                bpRandom.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        etPassword.setText(EncryptUtils.latinPassword(ClubListActivity.this, 2));
                                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                                        checkPassword.setChecked(true);
                                    }
                                });

                                checkPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        if (isChecked) {
                                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                                        } else {
                                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT |
                                                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                        }
                                    }
                                });

                                dialog.show();
                            }
                        }
                    });

                    // Delete Listener
                    civh.vDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            civh.vDelete.setTextColor(0xffff9060);
                            final Handler handler = new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    civh.vDelete.setTextColor(0xff747474);
                                }
                            }, 150);

                            if (ci.isAdmin()) {
                                Toast.makeText(ClubListActivity.this, "Impossible de supprimer le compte administrateur.", Toast.LENGTH_SHORT).show();
                            } else {

                                new MaterialDialog.Builder(ClubListActivity.this)
                                        .title(ci.getName())
                                        .content("Ce club n'aura plus accès à la cafet\nConfirmer ?")
                                        .positiveText("Oui")
                                        .negativeText("Annuler")
                                        .callback(new MaterialDialog.ButtonCallback() {
                                            @Override
                                            public void onPositive(MaterialDialog dialog) {
                                                super.onPositive(dialog);
                                                clubItems.remove(position);
                                                mAdapter.notifyDataSetChanged();
                                                Toast.makeText(ClubListActivity.this, "Accès club supprimé", Toast.LENGTH_SHORT).show();

                                                // Flag dataset changed
                                                hasChanged = true;
                                            }

                                            @Override
                                            public void onNegative(MaterialDialog dialog) {
                                                super.onNegative(dialog);
                                            }
                                        })
                                        .show();
                            }
                        }
                    });

                    break;

                default:
                case TYPE_CLUB_HEADER:

                    ClubHeaderViewHolder chvh = (ClubHeaderViewHolder) holder;
                    chvh.vTitle.setText(ci.getName());

                    break;
            }
        }

        @Override
        public int getItemViewType(int position) {
            return clubItems.get(position).isHeader() ? TYPE_CLUB_HEADER : TYPE_CLUB_ITEM;
        }

        @Override
        public int getItemCount() {
            return clubItems == null ? 0 : clubItems.size();
        }

        // Classic View Holder for club item
        public class ClubItemViewHolder extends RecyclerView.ViewHolder {

            protected TextView vTitle, vDesc, vModify, vDelete;

            public ClubItemViewHolder(View v) {
                super(v);
                vTitle = (TextView) v.findViewById(R.id.clubTitle);
                vDesc = (TextView) v.findViewById(R.id.clubDesc);
                vModify = (TextView) v.findViewById(R.id.tvModify);
                vDelete = (TextView) v.findViewById(R.id.tvDelete);
            }
        }

        // Classic View Holder for club header
        public class ClubHeaderViewHolder extends RecyclerView.ViewHolder {

            protected TextView vTitle;

            public ClubHeaderViewHolder(View v) {
                super(v);
                vTitle = (TextView) v.findViewById(R.id.tvHeaderClub);
            }
        }

    }

}
