package fr.bde_eseo.lacommande.model;

/**
 * Created by Rascafr on 21/11/2015.
 */
public class CartItem {
    private String name;
    private String price;
    private int level;
    private RootItem linkedItem;

    public CartItem (RootItem ri, int level) {
        this.name = ri.getName();
        this.price = ri.getFormattedPrice();
        this.linkedItem = ri;
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public int getLevel() {
        return level;
    }

    public RootItem getLinkedItem() {
        return linkedItem;
    }
}
