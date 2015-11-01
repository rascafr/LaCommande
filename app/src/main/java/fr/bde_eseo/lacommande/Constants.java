package fr.bde_eseo.lacommande;

/**
 * Created by Rascafr on 20/10/2015.
 */
public class Constants {

    /**
     * URL LIST
     */

    // Server
    public final static String URL_SERVER = "http://217.199.187.59/francoisle.fr/lacommande/";

    // Connecting
    public final static String URL_LOGIN_CLUB = URL_SERVER + "apps-lacommande/v10/connectClub.php";

    // Admin
    public final static String URL_CLUBS_LISTS = URL_SERVER + "apps-lacommande/v10/getClubList.php";
    public final static String URL_CLUBS_SYNC = URL_SERVER + "apps-lacommande/v10/syncClubs.php";
    public final static String URL_PLANNING_GET = URL_SERVER + "apps-lacommande/v10/getPlanning.php";
    public final static String URL_PLANNING_SYNC = URL_SERVER + "apps-lacommande/v10/syncPlanning.php";

    // New Client / Client managing
    public final static String URL_CLIENTS_LISTS = URL_SERVER + "apps-lacommande/v10/getClientsList.php";

    // Orders List
    public final static String URL_CURRENT_ORDERS_GET = URL_SERVER + "apps-lacommande/v10/getCurrentOrders.php";
    public final static String URL_CURRENT_ORDER_SYNC = URL_SERVER + "apps-lacommande/v10/syncCurrentOrder.php";

}
