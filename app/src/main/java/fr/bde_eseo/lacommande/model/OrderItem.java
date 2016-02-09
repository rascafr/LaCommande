package fr.bde_eseo.lacommande.model;

/**
 * Created by Rascafr on 31/01/2016.
 */

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Custom definition for Order object
 */
public class OrderItem {
    private String idstr, clientname, friendlyText, date, instructions, colorHtml, clientlogin;
    private int status, idmod, idcmd;
    private boolean paidbefore, isHeader, paidlydia;
    private double price;

    public static final int ORDER_ITEM_PREPARING_UNPAID = 0;
    public static final int ORDER_ITEM_READY_UNPAID = 1;
    public static final int ORDER_ITEM_DONE_PAID = 2;
    public static final int ORDER_ITEM_PREPARING_PAID = 4;
    public static final int ORDER_ITEM_READY_PAID = 5;

    public OrderItem (JSONObject obj) throws JSONException {
        this.idmod = obj.getInt("idmod");
        this.idcmd = obj.getInt("idcmd");
        this.status = obj.getInt("status");
        this.paidbefore = obj.getInt("paidbefore") == 1;
        this.paidlydia = obj.getInt("paidlydia") == 1;
        this.idstr = obj.getString("idstr");
        this.clientlogin = obj.getString("clientlogin");
        this.clientname = obj.getString("clientname");
        this.date = obj.getString("date");
        this.price = obj.getDouble("price");
        this.friendlyText = obj.getString("friendlyText");
        this.instructions = obj.getString("instructions");
        if (this.instructions.equals("?")) this.instructions = "";
        this.colorHtml = obj.getString("color");
        this.isHeader = false;
    }

    public OrderItem (String name) {
        this.friendlyText = name;
        this.isHeader = true;
    }

    public void setFriendlyText(String friendlyText) {
        this.friendlyText = friendlyText;
    }

    public String getClientlogin() {
        return clientlogin;
    }

    public int getIdcmd() {
        return idcmd;
    }

    public void setOrderFullStatus (int stpaid) {
        this.status = stpaid % 4;
        this.paidbefore = stpaid >= 4;
    }

    public boolean isHeader() {
        return isHeader;
    }

    public int getOrderFullStatus () {
        return this.status + (this.paidbefore ? 4:0);
    }

    public String getPaidStatus() {
        return this.paidbefore ? this.paidlydia ? "Payée (Lydia)" : "Payée (Liquide)" : "Non payée";
    }

    public String getColorHtml() {
        return colorHtml;
    }

    public String getIdstr() {
        return idstr;
    }

    public String getClientname() {
        return clientname;
    }

    public String getFriendlyText() {
        return friendlyText;
    }

    public String getDate() {
        return date;
    }

    public String getInstructions() {
        return instructions;
    }

    public int getStatus() {
        return status;
    }

    public int getIdmod() {
        return idmod;
    }

    public boolean isPaidbefore() {
        return paidbefore;
    }

    public double getPrice() {
        return price;
    }
}