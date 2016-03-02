package fr.bde_eseo.lacommande;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import fr.bde_eseo.lacommande.model.CartItem;
import fr.bde_eseo.lacommande.model.CategoryItem;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.model.DisplayItem;
import fr.bde_eseo.lacommande.model.ElementItem;
import fr.bde_eseo.lacommande.model.IngredientItem;
import fr.bde_eseo.lacommande.model.MenuItem;
import fr.bde_eseo.lacommande.model.RootItem;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;
import fr.bde_eseo.lacommande.utils.ConnexionUtils;
import fr.bde_eseo.lacommande.utils.Utilities;

/**
 * Created by Rascafr on 11/11/2015.
 * Version basique de l'interface et du comportement
 */
public class OrderGenericActivity extends AppCompatActivity {

    // UI Layout
    private RecyclerView recyListResume;
    private RecyclerView recyListCategories;
    private RecyclerView recyListDisplay;
    private TextView tvTotal;
    private TextView tvIndicator;
    private TextView tvResume;
    private EditText etInstr;
    private LinearLayout llLoad;

    // Adapters
    private CartAdapter cartAdapter;
    private CategoryAdapter categoryAdapter;
    private DisplayAdapter displayAdapter;

    // Model
    private ArrayList<CartItem> cartItems;
    private ArrayList<DisplayItem> displayItems;

    // Stacked buttons
    private Button buttonValid;
    private Button buttonCancel;

    // Others
    private int currentMode = MODE_NAVIGATE;
    private String clientName;
    private Context context;

    // Current object
    private RootItem selectedMenu;
    private RootItem selectedElement;

    // Mode definition
    public final static int MODE_NAVIGATE = 0;
    public final static int MODE_INGREDIENTS = 1;
    public final static int MODE_ELEMENTS_PRIMARY = 2;
    public final static int MODE_ELEMENTS_SECONDARY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = OrderGenericActivity.this;

        // Set UI Main Layout
        setContentView(R.layout.activity_generic_order);

        // Arrow back to main activity
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get intent's parameters
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras == null) {
                Toast.makeText(OrderGenericActivity.this, "Erreur de l'application (c'est pas normal)", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                clientName = extras.getString(Constants.KEY_NEW_ORDER_CLIENT);
                getSupportActionBar().setTitle(getString(R.string.activity_order) + " (" + clientName + ")");
            }
        } else {
            clientName = (String) savedInstanceState.getSerializable(Constants.KEY_NEW_ORDER_CLIENT);
            getSupportActionBar().setTitle(getString(R.string.activity_order) + " (" + clientName + ")");
        }

        // Get layout object
        recyListResume = (RecyclerView) findViewById(R.id.recyListResume);
        recyListCategories = (RecyclerView) findViewById(R.id.recyListCategories);
        recyListDisplay = (RecyclerView) findViewById(R.id.recyListItems);
        tvTotal = (TextView) findViewById(R.id.tvSumPrice);
        tvIndicator = (TextView) findViewById(R.id.tvStackMoreText);
        tvResume = (TextView) findViewById(R.id.tvResume);
        buttonValid = (Button) findViewById(R.id.buttonValid);
        buttonCancel = (Button) findViewById(R.id.buttonCancel);
        llLoad = (LinearLayout) findViewById(R.id.llLoadOrder);
        llLoad.setVisibility(View.INVISIBLE);

        // Init data
        currentMode = MODE_NAVIGATE;
        selectedMenu = null;
        selectedElement = null;

        // Remote arrays
        DataStore.getInstance().initCategoryArray();
        DataStore.getInstance().initElementArray();
        DataStore.getInstance().initCartArray();

        // Local arrays
        cartItems = new ArrayList<>();
        displayItems = new ArrayList<>();

        // Agency
        LinearLayoutManager llmCart = new LinearLayoutManager(this);
        llmCart.setOrientation(LinearLayoutManager.VERTICAL);

        LinearLayoutManager llmCategories = new LinearLayoutManager(this);
        llmCategories.setOrientation(LinearLayoutManager.VERTICAL);

        StaggeredGridLayoutManager glmDisplay = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);

        // Init adapters

        // Cart
        cartAdapter = new CartAdapter();
        recyListResume.setAdapter(cartAdapter);
        recyListResume.setHasFixedSize(true);
        recyListResume.setLayoutManager(llmCart);

        // Categories
        categoryAdapter = new CategoryAdapter();
        recyListCategories.setAdapter(categoryAdapter);
        recyListCategories.setHasFixedSize(true);
        recyListCategories.setLayoutManager(llmCategories);

        // General display
        displayAdapter = new DisplayAdapter();
        recyListDisplay.setAdapter(displayAdapter);
        recyListDisplay.setHasFixedSize(true);
        recyListDisplay.setLayoutManager(glmDisplay);

        // Init layout objects
        tvIndicator.bringToFront();
        initLayout();
        updateButtonProperties();

        // Notify adapters
        cartAdapter.notifyDataSetChanged();
        categoryAdapter.notifyDataSetChanged();
        displayAdapter.notifyDataSetChanged();

        // Download data from server
        AsyncData asyncData = new AsyncData();
        asyncData.execute(Constants.API_ORDER_ITEMS);

        // Buttons on-clic listener
        buttonValid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Check cart is not empty
                if (cartItems.size() == 0) {
                    new MaterialDialog.Builder(context)
                            .title("Panier vide")
                            .content("Vous devez ajouter au moins un élément pour envoyer la commande en cuisine.")
                            .negativeText("Fermer")
                            .show();
                } else {

                    switch (currentMode) {

                        // If mode is Ingredient, end it
                        case MODE_INGREDIENTS:

                            // Check if there is a menu
                            if (selectedMenu != null) {

                                // We're in a menu
                                // Check if we've filled first sandwich item
                                int nbSandw = ((MenuItem) selectedMenu).getNbSandw();
                                int nbElems = ((MenuItem) selectedMenu).getNbElems();

                                // Reach max of primary items : go to secondary if there is any, else close menu
                                if (selectedMenu.getItems().size() >= nbSandw) {

                                    if (nbElems > 0) {
                                        currentMode = MODE_ELEMENTS_SECONDARY;
                                        updateButtonProperties();
                                        updateDisplayArray(0);

                                        // Set helper text
                                        setHelperText("Choisissez l'élément secondaire " + (selectedMenu.getItems().size() - nbSandw + 1) + "/" + nbElems + " du menu " + selectedMenu.getName());

                                    } else {

                                        // End selection
                                        selectedMenu = null;
                                        // Helper default
                                        setHelperText("Sélectionnez une catégorie, puis ajoutez un menu ou un élément");
                                        // Navigation mode
                                        currentMode = MODE_NAVIGATE;
                                        // Reset display
                                        updateDisplayArray(0);
                                        categoryAdapter.notifyDataSetChanged();
                                    }
                                } else {

                                    // Else, we're missing another one sandwich / composed item
                                    // So go back to primary mode
                                    currentMode = MODE_ELEMENTS_PRIMARY;
                                    updateButtonProperties();
                                    updateDisplayArray(0);

                                    // Set helper text
                                    setHelperText("Choisissez l'élément principal " + (selectedMenu.getItems().size() + 1) + "/" + nbSandw + " du menu " + selectedMenu.getName());
                                }

                            } else {

                                // We're not in a menu : validation must validate items and go back
                                selectedElement = null;
                                // Helper default
                                setHelperText("Sélectionnez une catégorie, puis ajoutez un menu ou un élément");
                                // Navigation mode
                                currentMode = MODE_NAVIGATE;
                                // Reset display
                                updateDisplayArray(0);
                                categoryAdapter.setSelectedCategory(0);
                                updateButtonProperties();

                            }

                            break;

                        // If mode is Primary, end it
                        case MODE_ELEMENTS_PRIMARY:
                            currentMode = MODE_NAVIGATE;
                            break;

                        // If mode is Navigate, send order to cookers
                        case MODE_NAVIGATE:

                            MaterialDialog md = new MaterialDialog.Builder(OrderGenericActivity.this)
                                    .customView(R.layout.dialog_add_instructions, false)
                                    .title("Ajouter un commentaire ?")
                                    .positiveText("Finaliser la commande")
                                    .negativeText("Annuler")
                                    .cancelable(false)
                                    .positiveText("Confirmer")
                                    .negativeText("Annuler")
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                            DataStore.getInstance().setInstructions(
                                                    // Convert InputText into formatted string without Emojis / Unicode characters
                                                    etInstr
                                                            .getText()
                                                            .toString()
                                                            .trim()
                                            );
                                            AsyncPostCart asyncPostCart = new AsyncPostCart();
                                            asyncPostCart.execute();

                                        }
                                    })
                                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                            materialDialog.hide();
                                        }
                                    })
                                    .build();

                            etInstr = ((EditText) (md.getView().findViewById(R.id.etInstructions)));
                            md.show();

                            break;
                    }

                    updateButtonProperties();
                    updateDisplayArray(0);
                }
            }
        });

        /**
         * Action sur annulation (bouton annuler)
         *
         * - Si sur choix des éléments d'un menu : annuler = suppression du menu en cours
         * - Si sur choix des ingrédients d'un élément : annuler = suppression de l'élément en cours
         *
         * Dans tous les cas : retour en mode navigation, affichage de la catégorie 0
         */
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (currentMode) {

                    // If mode is Primary, end it, delete parent item (menu
                    case MODE_ELEMENTS_PRIMARY:
                        // If mode is Secondary, end it, delete parent item (menu)
                    case MODE_ELEMENTS_SECONDARY:

                        if (selectedMenu != null) {
                            Toast.makeText(context, selectedMenu.getName() + " supprimé du panier", Toast.LENGTH_SHORT).show();
                            DataStore.getInstance().getCartItems().remove(selectedMenu);
                            selectedMenu = null;
                        }

                        break;

                    // If mode is ingredients, delete parent item
                    // If parent's parent is menu, delete it
                    case MODE_INGREDIENTS:
                        if (selectedMenu != null) {
                            Toast.makeText(context, selectedMenu.getName() + " supprimé du panier", Toast.LENGTH_SHORT).show();
                            DataStore.getInstance().getCartItems().remove(selectedMenu);
                            selectedMenu = null;
                        } else {
                            if (selectedElement != null) {
                                Toast.makeText(context, selectedElement.getName() + " supprimé du panier", Toast.LENGTH_SHORT).show();
                                DataStore.getInstance().getCartItems().remove(selectedElement);
                            }
                        }
                        selectedElement = null;
                        break;

                }

                // Update data
                currentMode = MODE_NAVIGATE;
                setHelperText("Sélectionnez une catégorie, puis ajoutez un menu ou un élément");

                updateButtonProperties(); // update buttons
                updateDisplayArray(0);    // update display
                categoryAdapter.setSelectedCategory(0); // update category view, first category is selected
                updateCartArray(); // update cart view
            }
        });
    }

    private class CartAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private int selectedItemInCart = -1;

        public int getSelectedItemInCart() {
            return selectedItemInCart;
        }

        public void setSelectedItemInCart(int selectedItemInCart) {
            this.selectedItemInCart = selectedItemInCart;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            switch (viewType) {
                default:
                case 0:
                    return new CartHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_background, parent, false));
                case 1:
                    return new CartHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_background_1, parent, false));
                case 2:
                    return new CartHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_cart_background_2, parent, false));
            }

        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            final CartItem ci = cartItems.get(position);
            CartHolder ch = (CartHolder) holder;
            ch.tvName.setText(ci.getName());
            ch.tvPrice.setText(ci.getPrice());

            if (ci.getLevel() == 0) {
                ch.tvPrice.setVisibility(View.VISIBLE);
            } else {
                ch.tvPrice.setVisibility(View.INVISIBLE);
            }

            if (selectedItemInCart == position) {
                ch.rlBack.setBackgroundColor(getResources().getColor(R.color.md_orange_100));
            } else {
                switch (ci.getLevel()) {
                    case 0:
                        ch.rlBack.setBackgroundColor(getResources().getColor(R.color.md_blue_50));
                        break;
                    case 1:
                        ch.rlBack.setBackgroundColor(getResources().getColor(R.color.md_blue_100));
                        break;
                    case 2:
                        ch.rlBack.setBackgroundColor(getResources().getColor(R.color.md_blue_200));
                        break;
                }
            }

            ch.rlBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new MaterialDialog.Builder(OrderGenericActivity.this)
                            .title("Panier")
                            .content("Supprimer " + ci.getLinkedItem().getName() + " du panier ?")
                            .positiveText("Oui")
                            .negativeText("Annuler")
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                    if (currentMode != MODE_NAVIGATE) {
                                        new MaterialDialog.Builder(context)
                                                .title("Suppression impossible")
                                                .content("Pour supprimer un élément du panier, vous devez être en mode navigation.\n\nTerminez de personnaliser l'élément / menu actuel puis réessayez ...")
                                                .negativeText("Fermer")
                                                .show();
                                    } else if (ci.getLevel() != 0) {
                                        new MaterialDialog.Builder(context)
                                                .title("Suppression impossible")
                                                .content("Vous ne pouvez pas supprimer du panier autre chose qu'un un menu ou un élément qui n'est pas contenu dans un menu.")
                                                .negativeText("Fermer")
                                                .show();
                                    } else {
                                        DataStore.getInstance().getCartItems().remove(ci.getLinkedItem());
                                        updateCartArray();
                                    }
                                }
                            })
                            .show();
                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            return cartItems.get(position).getLevel();
        }

        @Override
        public int getItemCount() {
            return cartItems.size();
        }

        private class CartHolder extends RecyclerView.ViewHolder {

            private TextView tvName, tvPrice;
            private RelativeLayout rlBack;

            public CartHolder(View v) {
                super(v);
                tvName = (TextView) v.findViewById(R.id.tvName);
                tvPrice = (TextView) v.findViewById(R.id.tvPrice);
                rlBack = (RelativeLayout) v.findViewById(R.id.rlItem);
            }
        }
    }

    private class CategoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        // current selected category
        private int selectedCategory = 0;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CategoryHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_category, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            CategoryHolder ch = (CategoryHolder) holder;
            CategoryItem ci = DataStore.getInstance().getCategoryItems().get(position);
            ch.tvTitle.setText(ci.getName());
            if (selectedCategory == position) {
                ch.cardView.setCardBackgroundColor(getResources().getColor(R.color.md_orange_200));
            } else {
                if (currentMode == MODE_NAVIGATE)
                    ch.cardView.setCardBackgroundColor(getResources().getColor(R.color.md_blue_grey_50));
                else
                    ch.cardView.setCardBackgroundColor(getResources().getColor(R.color.md_grey_500));
            }

            ch.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Il n'est possible de changer la catégorie qu'en mode navigation
                    if (currentMode == MODE_NAVIGATE) {
                        selectedCategory = position;
                        categoryAdapter.notifyDataSetChanged();
                        updateDisplayArray(selectedCategory);
                    }
                }
            });
        }

        public void setSelectedCategory(int selectedCategory) {
            this.selectedCategory = selectedCategory;
            this.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return DataStore.getInstance().getCategoryItems().size();
        }

        private class CategoryHolder extends RecyclerView.ViewHolder {

            private TextView tvTitle;
            private CardView cardView;

            public CategoryHolder(View v) {
                super(v);
                tvTitle = (TextView) v.findViewById(R.id.categoryTitle);
                cardView = (CardView) v.findViewById(R.id.card_view);
            }
        }
    }

    private class DisplayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new DisplayHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            final DisplayItem di = displayItems.get(position);
            DisplayHolder dh = (DisplayHolder) holder;
            dh.vTitle.setText(di.getTitle());
            dh.vPrice.setText(di.getPrice());

            if (di.isSelected()) {
                dh.cardView.setCardBackgroundColor(getResources().getColor(R.color.md_orange_200));
            } else {
                dh.cardView.setCardBackgroundColor(getResources().getColor(R.color.md_grey_300));
            }

            dh.cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Perform operations (add to cart, etc)
                    actionDisplayTouch(di, di.getLinkItem());
                    updateButtonProperties();
                }
            });
        }

        @Override
        public int getItemCount() {
            return displayItems.size();
        }

        private class DisplayHolder extends RecyclerView.ViewHolder {

            private TextView vTitle, vPrice;
            private CardView cardView;

            public DisplayHolder(View v) {
                super(v);
                vTitle = (TextView) v.findViewById(R.id.itemTitle);
                vPrice = (TextView) v.findViewById(R.id.itemPrice);
                cardView = (CardView) v.findViewById(R.id.card_view);
            }
        }
    }

    private class AsyncData extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            return ConnexionUtils.postServerData(urls[0], null);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            llLoad.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);

            llLoad.setVisibility(View.INVISIBLE);

            if (Utilities.isNetworkDataValid(data)) {
                DataStore.getInstance().setCafetData(data);
                updateButtonProperties(); // update buttons
                updateDisplayArray(0);    // update display
                categoryAdapter.setSelectedCategory(0); // update category view, first category is selected
                updateCartArray(); // update cart view
            } else {
                Toast.makeText(OrderGenericActivity.this, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class AsyncPostCart extends AsyncTask<String, String, APIResponse> {

        private MaterialDialog materialDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            materialDialog = new MaterialDialog.Builder(context)
                    .title("Envoi de la commande")
                    .content("Veuillez patientier ...")
                    .cancelable(false)
                    .progress(true, 4)
                    .progressIndeterminateStyle(false)
                    .show();
        }

        @Override
        protected APIResponse doInBackground(String... params) {

            String JSONstr = DataStore.getInstance().outputJSON();
            String instr = DataStore.getInstance().getInstructions();

            HashMap<String, String> pairs = new HashMap<>();
            try {
                pairs.put("token", DataStore.getInstance().getToken());
                pairs.put("data", Base64.encodeToString(JSONstr.getBytes("UTF-8"), Base64.NO_WRAP));
                pairs.put("instructions", Base64.encodeToString(instr.getBytes("UTF-8"), Base64.NO_WRAP));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            return APIUtils.postAPIData(Constants.API_ORDER_SEND, pairs, context);
        }

        @Override
        protected void onPostExecute(final APIResponse apiResponse) {

            materialDialog.hide();

            if (apiResponse.isValid()) {
                materialDialog = new MaterialDialog.Builder(OrderGenericActivity.this)
                        .title("Commande envoyée !")
                        .content("Celle-ci est désormais visible en cuisine.\n" +
                                "Vous pouvez maintenant encaisser le client depuis l'onglet \"Liste\".")
                        .cancelable(false)
                        .negativeText("Fermer")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                OrderGenericActivity.this.finish();
                            }
                        })
                        .show();
            } else {
                materialDialog = new MaterialDialog.Builder(OrderGenericActivity.this)
                        .title("Erreur")
                        .content(apiResponse.getError())
                        .cancelable(false)
                        .negativeText("Fermer")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                if (apiResponse.isNetworkBad())
                                    materialDialog.hide();
                                else
                                    OrderGenericActivity.this.finish();
                            }
                        })
                        .show();
            }

        }
    }

    public void fillCartItem(ArrayList<RootItem> array, int level) {
        for (int i = 0; array != null && i < array.size(); i++) {
            cartItems.add(new CartItem(array.get(i), level));
            fillCartItem(array.get(i).getItems(), level + 1);
        }
    }

    // Updates :
    // - dataset for cart adapter
    // - cart item count
    // - cart total price
    public void updateCartArray() {
        cartItems.clear();
        fillCartItem(DataStore.getInstance().getCartItems(), 0);
        int quantity = DataStore.getInstance().getCartItems().size();
        tvResume.setText(
                "Panier (" + quantity + " élément" + (quantity > 0 ? "s" : "") + ") " +
                        new DecimalFormat("0.00").format(DataStore.getInstance().getCartPrice()) + "€"
        );
        //tvTotal.setText(new DecimalFormat("0.00").format(DataStore.getInstance().getCartPrice()) + "€");
        tvTotal.setVisibility(View.GONE);
        // scroll to last
        if (cartItems.size() > 0) {
            recyListResume.scrollToPosition(cartItems.size() - 1);
            cartAdapter.setSelectedItemInCart(cartItems.size() - 1);
        }
        cartAdapter.notifyDataSetChanged();
    }

    // Init UI objects
    public void initLayout() {
        updateCartArray();
        setHelperText("Sélectionnez une catégorie, puis ajoutez un menu ou un élément");
    }

    // Set helper text
    public void setHelperText(String text) {
        tvIndicator.setText("« " + text + " »");
    }

    // Add elements data into display item array
    public void updateDisplayArray(int selected) {

        String catname = DataStore.getInstance().getCategoryItems().get(selected).getCatname();

        displayItems.clear();

        // Navigation : displays all single items (menus, sandwiches, etc.) for a single category.
        if (currentMode == MODE_NAVIGATE) {

            // Menu ?
            for (int i = 0; i < DataStore.getInstance().getMenuItems().size(); i++) {
                MenuItem mi = DataStore.getInstance().getMenuItems().get(i);
                if (catname.equals(MenuItem.ID_CAT)) {
                    displayItems.add(new DisplayItem(mi));
                }
            }

            // Elements ?
            for (int i = 0; i < DataStore.getInstance().getElementItems().size(); i++) {
                ElementItem ei = DataStore.getInstance().getElementItems().get(i);
                if (catname.equals(ei.getIdcat())) {
                    displayItems.add(new DisplayItem(ei));
                }
            }

            // Ingredients : displays all ingredients a client could add to a sandwich / panini / etc
            // (items where ingredients property > 0)
        } else if (currentMode == MODE_INGREDIENTS) {

            // Ingredients
            // special display for ingredients (appears after a sandwich is selected)
            for (int i = 0; i < DataStore.getInstance().getIngredientItems().size(); i++) {
                IngredientItem ii = DataStore.getInstance().getIngredientItems().get(i);
                displayItems.add(new DisplayItem(ii));
            }

            // Primary Elements : displays all primary elements a client could add to a menu
            // (sandwich, panini, etc ... items where ingredients property > 0, and where outOfMenu is false)
        } else if (currentMode == MODE_ELEMENTS_PRIMARY) {

            // Primary Elements ?
            for (int i = 0; i < DataStore.getInstance().getElementItems().size(); i++) {
                ElementItem ei = DataStore.getInstance().getElementItems().get(i);
                if (ei.getIngredients() > 0 && !ei.isOutOfMenu()) {
                    displayItems.add(new DisplayItem(ei));
                }
            }

            // Secondary Elements : displays all secondary elements a client could add to a menu
            // (yogurt, cream, etc ... items where ingredients property is 0, and where outOfMenu is false)
        } else if (currentMode == MODE_ELEMENTS_SECONDARY) {

            // Secondary Elements ?
            for (int i = 0; i < DataStore.getInstance().getElementItems().size(); i++) {
                ElementItem ei = DataStore.getInstance().getElementItems().get(i);
                if (ei.getIngredients() == 0 && !ei.isOutOfMenu()) {
                    displayItems.add(new DisplayItem(ei));
                }
            }
        }

        displayAdapter.notifyDataSetChanged();
    }

    // Decide about what a display item touch does
    void actionDisplayTouch(DisplayItem displayItem, RootItem linkItem) {

        switch (linkItem.getType()) {

            // It's a menu ?
            case MenuItem.TYPE:

                // Copy the element
                MenuItem menuItem = new MenuItem((MenuItem) linkItem);

                // Count elements
                int nbElemActual = menuItem.getItems().size() + 1;
                int nbSandwMax = menuItem.getNbSandw();
                int nbElemMax = menuItem.getNbElems();

                // If no main element : switch to secondary element
                if (nbSandwMax == 0) {

                    // Set helper text
                    setHelperText("Choisissez l'élément secondaire " + (nbElemActual - nbSandwMax) + "/" + nbElemMax + " du menu " + menuItem.getName());

                    // Secondary Element chooser mode only
                    currentMode = MODE_ELEMENTS_SECONDARY;

                } else {
                    // else switch to main element

                    // Set helper text
                    setHelperText("Choisissez l'élément principal " + nbElemActual + "/" + nbSandwMax + " du menu " + menuItem.getName());

                    // Primary Element chooser mode first
                    currentMode = MODE_ELEMENTS_PRIMARY;
                }

                // Add current element into cart and set text in yellow
                DataStore.getInstance().getCartItems().add(menuItem);
                updateCartArray();

                // Memorize menu item
                selectedMenu = menuItem;

                // Update category
                categoryAdapter.notifyDataSetChanged();

                // Display primary elements
                updateDisplayArray(0);
                break;

            // It's an element ?
            case ElementItem.TYPE:

                // Copy the element
                ElementItem elementItem = new ElementItem((ElementItem) linkItem);

                // Element has ingredients ?
                if (elementItem.getIngredients() > 0) {

                    // Ingredient chooser mode
                    currentMode = MODE_INGREDIENTS;

                    // Set helper text
                    setHelperText("Ajoutez les ingredients pour l'élément " + elementItem.getName() + " (" + elementItem.getIngredients() + " max)");

                    // If a menu is selected, add element into
                    if (selectedMenu != null) {
                        selectedMenu.addItem(elementItem);
                    } else {

                        // Else, use directly cart array

                        // If element is selected : remove it
                        if (displayItem.isSelected()) {
                            displayItem.setIsSelected(false);
                            DataStore.getInstance().getCartItems().remove(
                                    searchForElement(selectedElement, displayItem.getLinkItem().getId())
                            );
                        } else {
                            // Else, add it
                            DataStore.getInstance().getCartItems().add(elementItem);

                            // Mark the display item as selected
                            displayItem.setIsSelected(true);
                        }

                        // Update screen
                        displayAdapter.notifyDataSetChanged();
                        updateCartArray();
                    }

                    // Memorize element item
                    selectedElement = elementItem;

                    // Update cart and category array
                    updateCartArray();
                    categoryAdapter.notifyDataSetChanged();

                    // Display ingredients
                    updateDisplayArray(0);

                } else {

                    // If a menu is selected, add element into if it's less than max
                    if (selectedMenu != null) {

                        selectedMenu.addItem(elementItem);

                        if (selectedMenu.getItems().size() < ((MenuItem) selectedMenu).getNbElems() + ((MenuItem) selectedMenu).getNbSandw()) {
                            // Set helper text
                            setHelperText("Choisissez l'élément secondaire " + ((selectedMenu.getItems().size() + 1) - ((MenuItem) selectedMenu).getNbElems() + 1) + "/" + ((MenuItem) selectedMenu).getNbElems() + " du menu " + selectedMenu.getName());
                        } else {
                            // End selection
                            selectedMenu = null;
                            // Helper default
                            setHelperText("Sélectionnez une catégorie, puis ajoutez un menu ou un élément");
                            // Navigation mode
                            currentMode = MODE_NAVIGATE;
                            // Reset display
                            updateDisplayArray(0);
                            categoryAdapter.notifyDataSetChanged();
                        }
                    } else {
                        // Else, add it directly to cart
                        DataStore.getInstance().getCartItems().add(elementItem);
                    }

                    // Add it to cart (no customisation)
                    updateCartArray();
                }

                break;

            // It's an ingredient ?
            case IngredientItem.TYPE:

                if (displayItem.isSelected()) {

                    selectedElement.getItems().remove(
                            searchForElement(selectedElement, displayItem.getLinkItem().getId())
                    );
                    displayItem.setIsSelected(false);
                    displayAdapter.notifyDataSetChanged();
                    updateCartArray();

                } else {

                    // Copy the ingredient object
                    IngredientItem ingredientItem = (IngredientItem) linkItem;

                    // Mark the display item as selected
                    displayItem.setIsSelected(true);
                    displayAdapter.notifyDataSetChanged();

                    // Add the ingredient into element
                    selectedElement.addItem(ingredientItem);

                    updateCartArray();
                }
                break;
        }
    }

    // Search for an element in a sub item
    // null → in cart
    // TODO
    public int searchForElement(RootItem parent, String idstr) {
        int rootPos = -1;
        if (parent != null) {
            if (parent.getItems() != null) {
                for (int i = 0; i < parent.getItems().size(); i++) {
                    if (parent.getItems().get(i).getId().equals(idstr)) {
                        rootPos = i;
                    }
                }
            }
        }
        return rootPos;
    }

    // Set the stacked buttons names and visibilities (depends of the current mode value)
    public void updateButtonProperties() {

        switch (currentMode) {

            case MODE_NAVIGATE:
                buttonValid.setText("Envoyer");
                buttonValid.setVisibility(View.VISIBLE);
                buttonCancel.setVisibility(View.GONE);
                break;

            // Primary mode :
            // → Next : press item (croc, sandw, etc)
            // → Cancel : remove item and go back to navigate mode
            case MODE_ELEMENTS_PRIMARY:
                buttonValid.setVisibility(View.GONE);
                buttonCancel.setText("Annuler");
                buttonCancel.setVisibility(View.VISIBLE);
                break;

            // Secondary mode, possibility is only cancel (autovalidate)
            case MODE_ELEMENTS_SECONDARY:
                buttonValid.setVisibility(View.GONE);
                buttonCancel.setText("Annuler");
                buttonCancel.setVisibility(View.VISIBLE);
                break;

            case MODE_INGREDIENTS:
                buttonValid.setText("Valider");
                buttonValid.setVisibility(View.VISIBLE);
                buttonCancel.setText("Annuler");
                buttonCancel.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * Menu : clear cart
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_order, menu);
        return true;
    }

    // Go back with menu
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                OrderGenericActivity.this.onBackPressed();
                return true;

            // Respond to the clean cart action
            case R.id.action_clean:
                new MaterialDialog.Builder(context)
                        .title("Vider le panier ?")
                        .content("Le contenu du panier sera supprimé.\nContinuer ?")
                        .positiveText("Oui")
                        .negativeText("Annuler")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                DataStore.getInstance().getCartItems().clear();
                                updateCartArray();

                                // End selection
                                selectedMenu = null;
                                selectedElement = null;
                                // Helper default
                                setHelperText("Sélectionnez une catégorie, puis ajoutez un menu ou un élément");
                                // Navigation mode
                                currentMode = MODE_NAVIGATE;
                                // Reset display
                                updateDisplayArray(0);
                                categoryAdapter.notifyDataSetChanged();
                            }
                        })
                        .show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        new MaterialDialog.Builder(context)
                .title("Annuler la commande ?")
                .content("Le contenu du panier sera supprimé.\nContinuer ?")
                .positiveText("Oui")
                .negativeText("Annuler")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        OrderGenericActivity.this.finish();
                    }
                })
                .show();
    }
}