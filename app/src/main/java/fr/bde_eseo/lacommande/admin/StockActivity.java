package fr.bde_eseo.lacommande.admin;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.melnykov.fab.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.model.ClubMember;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;

/**
 * Created by Rascafr on 08/02/2016.
 */
public class StockActivity extends AppCompatActivity {

    // Model
    private ArrayList<StockItem> stockItems;

    // UI Layout
    private ProgressBar progressLoad;
    private RecyclerView recyList;
    private FloatingActionButton fabSave;

    // Adapter
    private StockAdapter mAdapter;

    // Android
    private Context context;

    // Flag dataset has changed
    private boolean hasChanged;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = StockActivity.this;

        // Set UI Main Layout
        setContentView(R.layout.activity_stock);

        // Arrow back to main activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get UI Objects
        progressLoad = (ProgressBar) findViewById(R.id.progressLoad);
        recyList = (RecyclerView) findViewById(R.id.recyList);
        progressLoad.setVisibility(View.GONE);
        recyList.setVisibility(View.GONE);
        fabSave = (FloatingActionButton) findViewById(R.id.fabSave);

        // Init model / adapter
        stockItems = new ArrayList<>();
        hasChanged = false;
        mAdapter = new StockAdapter();
        recyList.setHasFixedSize(false);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        recyList.setLayoutManager(manager);
        //LinearLayoutManager llm = new LinearLayoutManager(this);
        //llm.setOrientation(LinearLayoutManager.VERTICAL);
        //recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Attach floating action button
        fabSave.attachToRecyclerView(recyList);

        // Listener for save action
        fabSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AsyncUpdateStock().execute();
            }
        });

        /*GridLayoutManager glm = new GridLayoutManager(context, 3);
        glm.setOrientation(LinearLayoutManager.VERTICAL);
        glm.setReverseLayout(false);
        recyList.setLayoutManager(glm);*/

        // Fetch data from server at startup
        new AsyncListStock().execute();
    }

    /**
     * Custom adapter for stock items
     */
    private class StockAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_STOCK_ITEM_ELEM = 0;
        private final static int TYPE_STOCK_ITEM_INGR = 1;
        private final static int TYPE_STOCK_HEADER = 2;


        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_STOCK_ITEM_ELEM:
                    return new StockElementViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_stock_elem, parent, false));

                case TYPE_STOCK_ITEM_INGR:
                    return new StockIngredientViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_stock_ingr, parent, false));

                default:
                case TYPE_STOCK_HEADER:
                    return new StockHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_stocks, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            final StockItem si = stockItems.get(position);

            switch (getItemViewType(position)) {

                case TYPE_STOCK_ITEM_ELEM:
                    final StockElementViewHolder sevh = (StockElementViewHolder) holder;
                    sevh.vName.setText(si.getName());
                    sevh.etStock.setText(String.valueOf(si.getStock()));

                    sevh.etStock.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new MaterialDialog.Builder(context)
                                    .title(si.getName())
                                    .content("Entrez le stock disponible pour cet élément :")
                                    .inputType(InputType.TYPE_CLASS_NUMBER)
                                    .input("", String.valueOf(si.getStock()), new MaterialDialog.InputCallback() {
                                        @Override
                                        public void onInput(MaterialDialog dialog, CharSequence input) {
                                            String cs = input.toString();
                                            if (cs.length() == 0) cs = "0"; // Prevents "" parse error
                                            si.setStock(Integer.parseInt(cs));
                                            notifyDataSetChanged();

                                            // Flag dataset changed
                                            hasChanged = true;
                                        }
                                    }).show();

                        }
                    });

                    break;

                case TYPE_STOCK_ITEM_INGR:
                    final StockIngredientViewHolder sivh = (StockIngredientViewHolder) holder;
                    sivh.vName.setText(si.getName());
                    sivh.chkStock.setChecked(si.getStock() != 0);

                    sivh.chkStock.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            si.setStock(sivh.chkStock.isChecked() ? 1 : 0);
                            notifyDataSetChanged();

                            // Flag dataset changed
                            hasChanged = true;
                        }
                    });

                    break;

                case TYPE_STOCK_HEADER:
                    StockHeaderViewHolder shvh = (StockHeaderViewHolder) holder;
                    shvh.vName.setText(si.getName());
                    if (si.getName().length() == 0)
                        shvh.imgDown.setVisibility(View.INVISIBLE);
                    else
                        shvh.imgDown.setVisibility(View.VISIBLE);

                    StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) shvh.itemView.getLayoutParams();
                    layoutParams.setFullSpan(true);
                    break;
            }
        }

        @Override
        public int getItemCount() {
            return stockItems == null ? 0 : stockItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return stockItems.get(position).isHeader() ? TYPE_STOCK_HEADER : stockItems.get(position).isQuantifiable() ? TYPE_STOCK_ITEM_ELEM : TYPE_STOCK_ITEM_INGR;
        }

        // Classic View Holder for stock header
        public class StockHeaderViewHolder extends RecyclerView.ViewHolder {

            protected TextView vName;
            protected ImageView imgDown;

            public StockHeaderViewHolder(View v) {
                super(v);
                vName = (TextView) v.findViewById(R.id.tvHeaderStock);
                imgDown = (ImageView) v.findViewById(R.id.icoDown);
            }
        }

        // Classic View Holder for stock element item
        public class StockElementViewHolder extends RecyclerView.ViewHolder {

            protected TextView vName;
            protected TextView etStock;

            public StockElementViewHolder(View v) {
                super(v);
                vName = (TextView) v.findViewById(R.id.tvElemName);
                etStock = (TextView) v.findViewById(R.id.etElemStock);
            }
        }

        // Classic View Holder for stock ingredient item
        public class StockIngredientViewHolder extends RecyclerView.ViewHolder {

            protected TextView vName;
            protected CheckBox chkStock;

            public StockIngredientViewHolder(View v) {
                super(v);
                vName = (TextView) v.findViewById(R.id.tvIngrName);
                chkStock = (CheckBox) v.findViewById(R.id.chkIngrStock);
            }
        }
    }

    /**
     * Custom asynctask to download data from server
     */
    private class AsyncListStock extends AsyncTask<String,String,APIResponse> {

        @Override
        protected void onPreExecute() {
            progressLoad.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(APIResponse apiResponse) {
            progressLoad.setVisibility(View.GONE);
            recyList.setVisibility(View.VISIBLE);

            if (apiResponse.isValid()) {

                try {
                    // Get Element Array
                    ArrayList<StockItem> elems = new ArrayList<>();
                    JSONArray elemArray = apiResponse.getJsonData().getJSONArray("elements");
                    for (int i=0;i<elemArray.length();i++) {
                        elems.add(new StockItem(elemArray.getJSONObject(i), true));
                    }

                    // Get Ingredient Array
                    ArrayList<StockItem> ingredients = new ArrayList<>();
                    JSONArray ingredientArray = apiResponse.getJsonData().getJSONArray("ingredients");
                    for (int i=0;i<ingredientArray.length();i++) {
                        ingredients.add(new StockItem(ingredientArray.getJSONObject(i), false));
                    }

                    // Add headers and data, if any
                    if (elems.size() > 0) {
                        stockItems.add(new StockItem("Éléments"));
                        stockItems.addAll(elems);
                    }

                    if (ingredients.size() > 0) {
                        stockItems.add(new StockItem("Ingrédients"));
                        stockItems.addAll(ingredients);
                    }

                    // Add last item to make space
                    stockItems.add(new StockItem(""));

                    // Notify adapter
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(context, apiResponse.getError(), Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected APIResponse doInBackground(String... params) {
            return APIUtils.postAPIData(Constants.API_STOCK_LIST, null, context);
        }
    }

    /**
     * Custom task to send stock data to server
     */
    private class AsyncUpdateStock extends AsyncTask<String,String,String> {

        private MaterialDialog materialDialog;
        private ArrayList<StockItem> sendables;
        private ClubMember clubMember;

        @Override
        protected String doInBackground(String... params) {

            for (int i = 0; i < sendables.size(); i++) {
                publishProgress();
                HashMap<String, String> pairs = new HashMap<>();
                pairs.put("login", clubMember.getLogin());
                pairs.put("password", clubMember.getPassword());
                pairs.put("idstr", sendables.get(i).getId());
                pairs.put("category", sendables.get(i).isQuantifiable() ? "elements" : "ingredients");
                pairs.put("stock", String.valueOf(sendables.get(i).getStock()));
                APIResponse response = APIUtils.postAPIData(Constants.API_STOCK_UPDATE, pairs, context);
                if (!response.isValid()) {
                    return response.getError();
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            clubMember = DataStore.getInstance().getClubMember();
            sendables = new ArrayList<>();

            // Found sendables items
            for (int i=0;i<stockItems.size();i++) {

                // If not a header, add it
                if (!stockItems.get(i).isHeader()) {
                    sendables.add(stockItems.get(i));
                }
            }

            // Progress dialog
            materialDialog = new MaterialDialog.Builder(context)
                    .title("Mise à jour des stocks")
                    .content("Veuillez patienter ...")
                    .progress(false, sendables.size(), true)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected void onPostExecute(String response) {
            materialDialog.hide();
            if (response != null) Toast.makeText(context, "Erreur : " + response, Toast.LENGTH_SHORT).show();
            else {
                Toast.makeText(context, "Stocks synchronisés !", Toast.LENGTH_SHORT).show();
                finish();
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            materialDialog.incrementProgress(1);
        }
    }

    /**
     * Custom definition to handle Stock item
     */
    private class StockItem {
        private String id, name, price;
        private int stock; // 1 or 0 if ingredient, 0 ou more if element
        private boolean isQuantifiable; // True if element, false otherwise
        private boolean isHeader;

        public StockItem (JSONObject obj, boolean isQuantifiable) throws JSONException {
            this.name = obj.getString("name");
            this.id = obj.getString("idstr");
            this.price = new DecimalFormat("0.00").format(obj.getDouble("price"))+"€";
            this.stock = obj.getInt("stock");
            this.isQuantifiable = isQuantifiable;
            this.isHeader = false;
        }

        public StockItem (String name) {
            this.name = name;
            this.isHeader = true;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPrice() {
            return price;
        }

        public int getStock() {
            return stock;
        }

        public boolean isQuantifiable() {
            return isQuantifiable;
        }

        public boolean isHeader() {
            return isHeader;
        }

        public void setStock(int stock) {
            this.stock = stock;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                StockActivity.this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        if (hasChanged) {
            new MaterialDialog.Builder(context)
                    .title("Quitter ?")
                    .content("Vos modifications n'ont pas été sauvegardées sur le serveur !")
                    .positiveText("Quitter")
                    .negativeText("Annuler")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                            hasChanged = false;
                            StockActivity.this.onBackPressed();
                        }
                    })
                    .show();
        } else {
            super.onBackPressed();
        }
    }

}
