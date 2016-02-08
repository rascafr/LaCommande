package fr.bde_eseo.lacommande.admin;

import android.app.TimePickerDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.EncryptUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 28/10/2015.
 */
public class PlanningActivity extends AppCompatActivity {

    // Model
    private ArrayList<DayItem> dayItems;

    // UI Layout
    private ProgressBar progressLoad;
    private RecyclerView recyList;
    private DayAdapter mAdapter;

    // Floating Action Buttons
    private FloatingActionButton fabSave;

    // JSON data as String
    private String clubJSONstr;

    // Modification happened ?
    private boolean hasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set UI Main Layout
        setContentView(R.layout.activity_planning);

        // Arrow back to main activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get UI Objects
        progressLoad = (ProgressBar) findViewById(R.id.progressLoad);
        recyList = (RecyclerView) findViewById(R.id.recyList);
        progressLoad.setVisibility(View.GONE);
        recyList.setVisibility(View.GONE);
        fabSave = (FloatingActionButton) findViewById(R.id.fabSave);

        // Prepare model
        dayItems = new ArrayList<>();

        // Set adapter with ListView
        mAdapter = new DayAdapter();
        recyList.setHasFixedSize(false);
        GridLayoutManager glm = new GridLayoutManager(this, 3);
        glm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(glm);
        recyList.setAdapter(mAdapter);

        // Download data and fill array
        AsyncPlanning asyncPlanning = new AsyncPlanning();
        asyncPlanning.execute(Constants.URL_PLANNING_GET);

        // Save action
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Create JSON String
                clubJSONstr = "";
                for (int i=0;i<dayItems.size();i++) {
                    if (clubJSONstr.length() > 0) {
                        clubJSONstr += ",";
                    }
                    clubJSONstr += dayItems.get(i).toJSONstr(i);
                }
                clubJSONstr = "[" + clubJSONstr + "]";

                // Send data to server
                AsyncServer asyncServer = new AsyncServer();
                asyncServer.execute(Constants.URL_PLANNING_SYNC);

            }
        });

    }

    @Override
    public void onBackPressed() {

        if (hasChanged) {
            new MaterialDialog.Builder(PlanningActivity.this)
                    .title("Quitter ?")
                    .content("Vos modifications n'ont pas été sauvegardées sur le serveur !")
                    .positiveText("Quitter")
                    .negativeText("Annuler")
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            super.onPositive(dialog);
                            hasChanged = false;
                            PlanningActivity.this.onBackPressed();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                PlanningActivity.this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Custom definition for day item
     */
    private class DayItem {
        private String name;
        private boolean isOpen;
        private String openHour, closeHour;
        private Date openDate, closeDate;

        public DayItem(String name, boolean isOpen, String openHour, String closeHour) {
            this.name = name;
            this.isOpen = isOpen;
            this.openHour = openHour;
            this.closeHour = closeHour;
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.FRANCE);
            try {
                this.openDate = format.parse(this.openHour);
                this.closeDate = format.parse(this.closeHour);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // For club item with JSON data
        public DayItem(JSONObject obj) throws JSONException {
            this(
                    obj.getString("name"),
                    obj.getInt("isopen") == 1,
                    obj.getString("openhour"),
                    obj.getString("closehour")
            );
        }

        public String toJSONstr(int id) {
            String json = "{";
            json += "\"idday\":\"" + id + "\",";
            json += "\"name\":\"" + this.name + "\",";
            json += "\"isopen\":\"" + (this.isOpen ? 1:0) + "\",";
            json += "\"openhour\":\"" + this.openHour + "\",";
            json += "\"closehour\":\"" + this.closeHour + "\"";
            json += "}";

            return json;
        }

        public void setIsOpen(boolean isOpen) {
            this.isOpen = isOpen;
        }

        public void setOpenHour(String openHour) {
            this.openHour = openHour;
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.FRANCE);
            try {
                this.openDate = format.parse(this.openHour);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public void setCloseHour(String closeHour) {
            this.closeHour = closeHour;
            SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.FRANCE);
            try {
                this.closeDate = format.parse(this.closeHour);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        public String getName() {
            return name;
        }

        public boolean isOpen() {
            return isOpen;
        }

        public String getOpenHour() {
            return openHour;
        }

        public String getCloseHour() {
            return closeHour;
        }

        public Date getOpenDate() {
            return openDate;
        }

        public Date getCloseDate() {
            return closeDate;
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
            dialog = new MaterialDialog.Builder(PlanningActivity.this)
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
                Toast.makeText(PlanningActivity.this, "Données synchronisées !", Toast.LENGTH_SHORT).show();

                // Flag dataset cleared
                hasChanged = false;

                // Quit app
                PlanningActivity.this.onBackPressed();

            } else {
                Toast.makeText(PlanningActivity.this, "Échec de la synchronisation", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... url) {
            HashMap<String, String> pairs = new HashMap<>();
            try {
                String b64data = Base64.encodeToString(clubJSONstr.getBytes("UTF-8"), Base64.NO_WRAP);
                pairs.put("data", b64data);
                pairs.put("hash", EncryptUtils.md5(b64data + getResources().getString(R.string.salt_sync_planning)));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return ConnexionUtils.postServerData(url[0], pairs);
        }
    }


    /**
     * Custom async task downloader for planning list
     */
    private class AsyncPlanning extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (dayItems == null)
                dayItems = new ArrayList<>();
            else
                dayItems.clear();

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
                        dayItems.add(new DayItem(jsonArray.getJSONObject(i)));
                    }

                    mAdapter.notifyDataSetChanged();
                    recyList.setVisibility(View.VISIBLE);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(PlanningActivity.this, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(PlanningActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected String doInBackground(String... url) {
            return ConnexionUtils.postServerData(url[0], null);
        }
    }

    /**
     * Custom adapter for planning
     */
    private class DayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DayItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_day, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

            final DayItem di = dayItems.get(position);
            final DayItemViewHolder divh = (DayItemViewHolder) holder;

            divh.vTitle.setText(di.getName());

            divh.vOpen.setText(di.getOpenHour());

            divh.vOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new TimePickerDialog(PlanningActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            di.setOpenHour(
                                    new DecimalFormat("00").format(hourOfDay) + ":" +
                                            new DecimalFormat("00").format(minute) + ":00");
                            mAdapter.notifyDataSetChanged();
                            hasChanged = true;
                        }
                    }, di.getOpenDate().getHours(), di.getOpenDate().getMinutes(), true).show();
                }
            });

            divh.vClose.setText(di.getCloseHour());

            divh.vClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new TimePickerDialog(PlanningActivity.this, new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            di.setCloseHour(
                                    new DecimalFormat("00").format(hourOfDay) + ":" +
                                            new DecimalFormat("00").format(minute) + ":00");
                            mAdapter.notifyDataSetChanged();
                            hasChanged = true;
                        }
                    }, di.getCloseDate().getHours(), di.getCloseDate().getMinutes(), true).show();
                }
            });

            divh.checkOpen.setChecked(di.isOpen());

            divh.checkOpen.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    di.setIsOpen(divh.checkOpen.isChecked());
                    hasChanged = true;
                }
            });
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return dayItems == null ? 0 : dayItems.size();
        }

        // Classic View Holder for day header
        public class DayItemViewHolder extends RecyclerView.ViewHolder {

            protected TextView vTitle, vOpen, vClose;
            protected CheckBox checkOpen;

            public DayItemViewHolder(View v) {
                super(v);
                vTitle = (TextView) v.findViewById(R.id.tvDayName);
                vOpen = (TextView) v.findViewById(R.id.tvOpen);
                vClose = (TextView) v.findViewById(R.id.tvClose);
                checkOpen = (CheckBox) v.findViewById(R.id.checkOpen);
            }
        }
    }
}
