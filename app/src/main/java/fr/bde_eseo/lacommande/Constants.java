package fr.bde_eseo.lacommande;

/**
 * Created by Rascafr on 20/10/2015.
 */
public class Constants {

    /**
     * General
     */
    public static final String APP_VERSION = "1.0";

    /**
     * Intent keys
     */
    public static final String KEY_NEW_ORDER_CLIENT = "intent.clientlistactivity.clientname";

    /**
     * URL LIST
     */

    // Server
    public final static String URL_SERVER = "http://217.199.187.59/francoisle.fr/lacommande/";

    // Connecting
    //public final static String URL_LOGIN_CLUB = URL_SERVER + "apps-lacommande/v10/connectClub.php";

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

    // Cafeteria Data
    public final static String URL_DATA_GET = URL_SERVER + "apps-lacommande/v10/syncData.php";

    // Token recuperation
    public final static String URL_TOKEN_GET = URL_SERVER + "apps-lacommande/v10/syncDate-token.php";

    // Cart synchronization
    public final static String URL_CART_SYNC = URL_SERVER + "apps-lacommande/v10/syncOrder-token.php";

    /**
     * API Urls
     */

    // Base API URL
    public final static String URL_API = "http://217.199.187.59/francoisle.fr/lacommande/api/desk/";

    // Connect / login
    public final static String API_CLUB_LOGIN = URL_API + "club/login.php";

    // History
    public final static String API_ORDER_HISTORY = URL_API + "order/history.php";

    // Clients
    public final static String API_CLIENT_LIST = URL_API + "client/list.php";

    /**
     * Preferences
     */
    public final static String PREFS_IDENTIFIER = "fr.bde_eseo.lacommande.prefs";
    public final static String PREFS_KEY_LOGIN = "key.login";


}
