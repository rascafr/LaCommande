package fr.bde_eseo.lacommande;

/**
 * Created by Rascafr on 20/10/2015.
 * Contient les définitions statiques suivantes :
 * - URL de l'API serveur
 * - Clefs Intents
 * - Clef Preferences
 */
public class Constants {

    /**
     * URL LIST
     */

    // Server
    public final static String URL_SERVER = "http://217.199.187.59/francoisle.fr/lacommande/";

    // Admin
    public final static String URL_CLUBS_LISTS = URL_SERVER + "apps-lacommande/v10/getClubList.php";
    public final static String URL_CLUBS_SYNC = URL_SERVER + "apps-lacommande/v10/syncClubs.php";
    public final static String URL_PLANNING_GET = URL_SERVER + "apps-lacommande/v10/getPlanning.php";
    public final static String URL_PLANNING_SYNC = URL_SERVER + "apps-lacommande/v10/syncPlanning.php";

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
    public final static String API_CLIENT_ADD = URL_API + "client/add.php";
    public final static String API_CLIENT_LIST = URL_API + "client/list.php";

    // Order
    public final static String API_ORDER_PREPARE = URL_API + "order/prepare.php";
    public final static String API_ORDER_ITEMS = URL_API + "order/items.php";
    public final static String API_ORDER_SEND = URL_API + "order/send.php";
    public final static String API_ORDER_LOCK = URL_API + "order/lock.php";
    public final static String API_ORDER_UPDATE = URL_API + "order/update.php";
    public final static String API_ORDER_LIST = URL_API + "order/list.php";
    public final static String API_ORDER_UNPAID = URL_API + "order/unpaid.php";
    public final static String API_ORDER_PAY = URL_API + "order/pay.php";

    // Parameters
    public final static String API_PARAMETERS_LIST = URL_API + "parameters/list.php";
    public final static String API_PARAMETERS_UPDATE = URL_API + "parameters/update.php";

    // Service
    public final static String API_SERVICE_READ = URL_API + "service/read.php";
    public final static String API_SERVICE_UPDATE = URL_API + "service/update.php";

    // Stock
    public final static String API_STOCK_LIST = URL_API + "stock/list.php";
    public final static String API_STOCK_UPDATE = URL_API + "stock/update.php";

    /**
     * Intent keys
     */
    public static final String KEY_NEW_ORDER_CLIENT = "intent.clientlistactivity.clientname";

    /**
     * Preferences
     */
    public final static String PREFS_IDENTIFIER = "fr.bde_eseo.lacommande.prefs";
    public final static String PREFS_KEY_LOGIN = "key.login";
    public final static String PREFS_KEY_PASSWORD = "key.password";

}
