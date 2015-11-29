package fr.bde_eseo.lacommande.model;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Rascafr on 11/11/2015.
 */
public class RootItem {

    private String name, id;
    private double price;
    private ArrayList<RootItem> items;
    private String type;
    private int level;

    public RootItem (String id, String name, double price, String type) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.type = type;
        items = new ArrayList<>();
    }

    public ArrayList<RootItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<RootItem> items) {
        this.items = items;
    }

    public void addItem (RootItem rootItem) {
        rootItem.level++;
        items.add(rootItem);
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public double getPrice() {
        return price;
    }

    public String getFormattedPrice() {
        return new DecimalFormat("0.00").format(price) + "€";
    }

    public String getType() {
        return type;
    }

    public int getLevel() {
        return level;
    }

    // Returns the price as double
    // If root is ingredient : price = element's priceuni
    // If root is element : (priceuni or, if parent is menu, price more) + (nbitems-hasingredients) -> > 0 * itemprice
    // If root is menu : menu price + elements
    public double calcRootPrice (boolean parentIsMenu) {

        double realPrice = 0.0;

        switch (type) {
            case IngredientItem.TYPE:
                realPrice += price;
                break;

            case ElementItem.TYPE:
                if (items != null) {
                    for (int i=0;i<items.size();i++) {
                        if (i >= ((ElementItem)this).getIngredients()) {
                            realPrice += items.get(i).getPrice();
                        }
                    }
                }
                if (!parentIsMenu) { // Lonely item : price is priceuni -> price in Parent Object
                    realPrice += price;
                } else { // Element in a menu : price is pricemore
                    realPrice += ((ElementItem) this).getPricemore();
                }
                break;

            case MenuItem.TYPE:
                if (items != null) {
                    for (int i = 0; i < items.size(); i++) {
                        realPrice += items.get(i).calcRootPrice(true);
                    }
                }
                realPrice += price;
                break;
        }

        return realPrice;
    }

    // Returns a JSON object as String
    public String toJSONString () {
        String strJson = "{";
        switch (type) {
            case ElementItem.TYPE:
                strJson += "\"element\":\"" + id + "\"";
                if (items != null && items.size() > 0) {
                    strJson += ", \"items\":[";
                    for (int i=0;i<items.size();i++) {
                        if (i!=0) strJson += ", ";
                        strJson += "\"" + items.get(i).id + "\"";
                    }
                    strJson += "]";
                }
                break;

            case MenuItem.TYPE:
                strJson += "\"menu\":\"" + id + "\"";
                if (items != null && items.size() > 0) {
                    strJson += ", \"items\":[";
                    for (int i=0;i<items.size();i++) {
                        if (i!=0) strJson += ", ";
                        strJson += items.get(i).toJSONString();
                    }
                    strJson += "]";
                }
                break;
        }
        strJson += "}";
        return strJson;
    }
}
