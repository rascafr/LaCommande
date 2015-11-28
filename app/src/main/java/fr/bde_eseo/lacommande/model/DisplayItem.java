package fr.bde_eseo.lacommande.model;

import java.text.DecimalFormat;

/**
 * Created by Rascafr on 12/11/2015.
 */
public class DisplayItem {

    private String title;
    private String price;
    private RootItem linkedItem;
    private boolean isSelected;

    public DisplayItem(RootItem ri) {
        this.title = ri.getName();
        this.price = ri.getFormattedPrice();
        this.linkedItem = ri;
        this.isSelected = false;
    }

    public String getTitle() {
        return title;
    }

    public String getPrice() {
        return linkedItem.getFormattedPrice();
    }

    public RootItem getLinkItem() {
        return linkedItem;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
