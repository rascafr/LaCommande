package fr.bde_eseo.lacommande.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Rascafr on 24/10/2015.
 */
public class DataStore {

    private static DataStore instance;

    private DataStore() {}

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    /**
     * Club item with informations
     */
    private ClubMember clubMember;

    public ClubMember getClubMember() {
        return clubMember;
    }

    public void setClubMember(ClubMember clubMember) {
        this.clubMember = clubMember;
    }

    /**
     * ESEO's client logins
     */
    private ArrayList<ClientItem> clientItems;

    public void initClientArray () {
        if (clientItems == null)
            clientItems = new ArrayList<>();
        clientItems.clear();
    }

    public ArrayList<ClientItem> getClientItems() {
        return clientItems;
    }

    // Returns a client object if name is found
    public ClientItem searchForClient (String name) {
        ClientItem clientItem = null;
        for (int i=0;i<clientItems.size() && clientItem == null;i++) {
            if (clientItems.get(i).getFullname().equals(name)) {
                clientItem = clientItems.get(i);
            }
        }
        return clientItem;
    }

    /**
     * Cart
     */
    private ArrayList<RootItem> cartItems;

    public void initCartArray () {
        if (cartItems == null)
            cartItems = new ArrayList<>();
        cartItems.clear();
    }

    public ArrayList<RootItem> getCartItems() {
        return cartItems;
    }

    /**
     * Cafet Data (JSONArray as string)
     */
    public void setCafetData(String data) {

        // Init arrays
        initCategoryArray();
        initMenuArray();
        initElementArray();
        initIngredientArray();

        try {

            JSONArray jsonArray = new JSONArray(data);
            for (int a=0;a<jsonArray.length();a++) {

                JSONObject jsonObject = jsonArray.getJSONObject(a);

                // Categories
                if (jsonObject.has("lacmd-categories")) {
                    JSONArray jsonCategories = jsonObject.getJSONArray("lacmd-categories");
                    for (int i=0;i<jsonCategories.length();i++) {
                        categoryItems.add(new CategoryItem(jsonCategories.getJSONObject(i)));
                    }
                }

                // Menus
                else if (jsonObject.has("lacmd-menus")) {
                    JSONArray jsonMenus = jsonObject.getJSONArray("lacmd-menus");
                    for (int i=0;i<jsonMenus.length();i++) {
                        menuItems.add(new MenuItem(jsonMenus.getJSONObject(i)));
                    }
                }

                // Elements
                else if (jsonObject.has("lacmd-elements")) {
                    JSONArray jsonElements = jsonObject.getJSONArray("lacmd-elements");
                    for (int i=0;i<jsonElements.length();i++) {
                        elementItems.add(new ElementItem(jsonElements.getJSONObject(i)));
                    }
                }

                // Ingredients
                else if (jsonObject.has("lacmd-ingredients")) {
                    JSONArray jsonIngredients = jsonObject.getJSONArray("lacmd-ingredients");
                    for (int i=0;i<jsonIngredients.length();i++) {
                        ingredientItems.add(new IngredientItem(jsonIngredients.getJSONObject(i)));
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cafet Categories
     */
    private ArrayList<CategoryItem> categoryItems;

    public void initCategoryArray () {
        if (categoryItems == null)
            categoryItems = new ArrayList<>();
        categoryItems.clear();
    }

    public ArrayList<CategoryItem> getCategoryItems() {return categoryItems;}

    /**
     * Cafet Menus
     */
    private ArrayList<MenuItem> menuItems;

    public void initMenuArray () {
        if (menuItems == null)
            menuItems = new ArrayList<>();
        menuItems.clear();
    }

    public ArrayList<MenuItem> getMenuItems() {return menuItems;}

    /**
     * Cafet Elements
     */
    private ArrayList<ElementItem> elementItems;

    public void initElementArray () {
        if (elementItems == null)
            elementItems = new ArrayList<>();
        elementItems.clear();
    }

    public ArrayList<ElementItem> getElementItems() {return elementItems;}

    /**
     * Cafet Ingredients
     */
    private ArrayList<IngredientItem> ingredientItems;

    public void initIngredientArray () {
        if (ingredientItems == null)
            ingredientItems = new ArrayList<>();
        ingredientItems.clear();
    }

    public ArrayList<IngredientItem> getIngredientItems() {return ingredientItems;}

    // Calculates the cart's price
    public double getCartPrice () {
        double tot = 0.0;
        for (int i=0;i<cartItems.size();i++)
            tot += cartItems.get(i).calcRootPrice(false);

        return tot;
    }

    /**
     * Order token
     */
    private String token;

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    /**
     * For Cart JSON output
     */
    public String outputJSON () {
        String json = "["; // Array begin

        for (int i=0;i<cartItems.size();i++) {
            if (i!=0) json += ", ";
            json += cartItems.get(i).toJSONString();
        }

        json += "]";

        return json;
    }

    /**
     * Instructions
     */
    private String instructions;

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }
}
