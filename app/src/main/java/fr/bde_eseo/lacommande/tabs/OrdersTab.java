package fr.bde_eseo.lacommande.tabs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.listeners.RecyclerItemClickListener;
import fr.bde_eseo.lacommande.model.ClubMember;
import fr.bde_eseo.lacommande.model.DataStore;
import fr.bde_eseo.lacommande.model.OrderItem;
import fr.bde_eseo.lacommande.utils.APIAsyncTask;
import fr.bde_eseo.lacommande.utils.APIResponse;

/**
 * Created by Rascafr on 29/10/2015.
 */
public class OrdersTab extends Fragment {

    // Model
    private ArrayList<OrderItem> orderItems;

    // UI Layout
    private RecyclerView recyList;
    private ProgressBar progressBar;
    private TextView tvUpdateInfo;
    private TextView tvNoOrder;
    private ImageView imgNetworkInfo;
    private TextView tvNetworkInfo;
    private ProgressBar progressNetwork;

    // Adapter
    private OrderAdapter mAdapter;

    // Auto-update objects
    private static Handler mHandler;
    private static final int RUN_UPDATE = 2000;
    private static final int RUN_START = 500;
    private static boolean run;
    private static boolean firstDisplay = true;
    private static String lastFetchedData = "";
    private static long lastUpdate = 0;

    // Android
    private Context context;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        context = getActivity();

        View rootView = inflater.inflate(R.layout.tab_orders, container, false);

        // Create model
        orderItems = new ArrayList<>();

        // Create adapter
        mAdapter = new OrderAdapter();

        // Prepare UI Objects
        progressBar = (ProgressBar) rootView.findViewById(R.id.progressOrders);
        progressBar.setVisibility(View.GONE);
        recyList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recyList.setHasFixedSize(false);
        StaggeredGridLayoutManager manager = new StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL);
        recyList.setLayoutManager(manager);
        recyList.setItemAnimator(new DefaultItemAnimator());
        tvUpdateInfo = (TextView) rootView.findViewById(R.id.tvUpdateInfo);
        tvNetworkInfo = (TextView) rootView.findViewById(R.id.tvNetworkInfo);
        tvNoOrder = (TextView) rootView.findViewById(R.id.tvNoOrder);
        imgNetworkInfo = (ImageView) rootView.findViewById(R.id.icoNetwork);
        progressNetwork = (ProgressBar) rootView.findViewById(R.id.progressNetwork);
        progressNetwork.setVisibility(View.VISIBLE);
        tvNoOrder.setVisibility(View.INVISIBLE);
        /*GridLayoutManager glm = new GridLayoutManager(getActivity(), 4);
        glm.setOrientation(LinearLayoutManager.VERTICAL);
        glm.setReverseLayout(false);
        recyList.setLayoutManager(glm);*/
        recyList.setAdapter(mAdapter);

        // Header
        //manager.set

        // Download data
        // -> in thread handler

        // On click listener
        recyList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                final OrderItem oi = orderItems.get(position);

                MaterialDialog.Builder mdb = new MaterialDialog.Builder(getActivity())
                        .customView(R.layout.dialog_orders, false);
                //.content(oi.getClientname() + " - " + new DecimalFormat("0.00").format(oi.getPrice()) + "€" + "\nCommande passée à " + oi.getDate() + "\n\n" + oi.getFriendlyText());

                switch (oi.getOrderFullStatus()) {

                    // Vient d'arriver en cuisine ; en préparation, non payée
                    case OrderItem.ORDER_ITEM_PREPARING_UNPAID:
                        mdb.positiveText("Marquer comme prête"); // -> st = 1, paid = 0
                        mdb.negativeText("Encaisser");          // -> st = 0, paid = 1
                        mdb.onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                oi.setOrderFullStatus(OrderItem.ORDER_ITEM_READY_UNPAID);
                                AsyncSetStatus asyncSetStatus = new AsyncSetStatus(context, oi);
                                asyncSetStatus.execute(Constants.API_ORDER_UPDATE);
                            }
                        });
                        mdb.onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                oi.setOrderFullStatus(OrderItem.ORDER_ITEM_PREPARING_PAID);
                                AsyncSetStatus asyncSetStatus = new AsyncSetStatus(context, oi);
                                asyncSetStatus.execute(Constants.API_ORDER_UPDATE);
                            }
                        });
                        break;

                    // Vient d'être marquée comme prête ; mais pas encore payée
                    case OrderItem.ORDER_ITEM_READY_UNPAID:
                        mdb.negativeText("Encaisser et terminer"); // -> st = 2, paid = 0
                        mdb.onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                oi.setOrderFullStatus(OrderItem.ORDER_ITEM_DONE_PAID);
                                AsyncSetStatus asyncSetStatus = new AsyncSetStatus(context, oi);
                                asyncSetStatus.execute(Constants.API_ORDER_UPDATE);
                            }
                        });
                        break;

                    // Vient d'être marquée comme payée ; mais pas encore prête
                    case OrderItem.ORDER_ITEM_PREPARING_PAID:
                        mdb.negativeText("Marquer comme prête"); // -> st = 1, paid = 1
                        mdb.onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                oi.setOrderFullStatus(OrderItem.ORDER_ITEM_READY_PAID);
                                AsyncSetStatus asyncSetStatus = new AsyncSetStatus(context, oi);
                                asyncSetStatus.execute(Constants.API_ORDER_UPDATE);
                            }
                        });
                        break;

                    // Vient d'être marquée comme prête et est payée
                    case OrderItem.ORDER_ITEM_READY_PAID:
                        mdb.negativeText("Terminer");
                        mdb.onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                oi.setOrderFullStatus(OrderItem.ORDER_ITEM_DONE_PAID);
                                AsyncSetStatus asyncSetStatus = new AsyncSetStatus(context, oi);
                                asyncSetStatus.execute(Constants.API_ORDER_UPDATE);
                            }
                        });
                        break;
                }

                MaterialDialog md = mdb.show();

                View mdView = md.getView();
                if (mdView != null && oi != null) {
                    ((TextView) mdView.findViewById(R.id.tvOrderName)).setText(oi.getIdstr() + " " + new DecimalFormat("000").format(oi.getIdmod()));
                    ((TextView) mdView.findViewById(R.id.tvOrderDetails)).setText(oi.getClientname() + " - " + new DecimalFormat("0.00").format(oi.getPrice()) + "€");
                    ((TextView) mdView.findViewById(R.id.tvOrderMore)).setText(Html.fromHtml("Commande passée à " + oi.getDate() + "<br>&nbsp;<br>" + oi.getFriendlyText()));
                    (mdView.findViewById(R.id.rlBackDialogOrder)).setBackgroundColor(Color.parseColor(oi.getColorHtml()));
                }
            }
        }));

        return rootView;
    }

    /**
     * Custom definition for AsyncTask downloader
     */
    private class AsyncOrders extends APIAsyncTask {

        private long t1;

        public AsyncOrders(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();/*
            if (orderItems == null)
                orderItems = new ArrayList<>();
            else
                orderItems.clear();*/

            run = false;
            if (firstDisplay) {
                tvNoOrder.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressNetwork.setVisibility(View.VISIBLE);
                recyList.setVisibility(View.INVISIBLE);
                imgNetworkInfo.setVisibility(View.INVISIBLE);
                firstDisplay = false;
            }
            //recyList.setVisibility(View.GONE);
            //mAdapter.notifyDataSetChanged();

            t1 = System.currentTimeMillis();
        }

        @Override
        protected void onPostExecute(APIResponse apiResponse) {

            progressNetwork.setVisibility(View.INVISIBLE);
            imgNetworkInfo.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            String updateText;

            if (lastFetchedData.length() == 0) {
                updateText = "---";
            } else {
                long diff = (System.currentTimeMillis() - lastUpdate) / 1000;
                int min = (int) diff / 60;
                updateText = "Il y a " + (min != 0 ? min : "moins d'une") + " minute" + (min > 1 ? "s" : "");
            }

            tvUpdateInfo.setText(updateText);

            if (apiResponse.isValid()) {

                tvNetworkInfo.setText("Connecté");
                imgNetworkInfo.setImageResource(R.drawable.ic_connected);

                try {

                    // Get data
                    JSONArray dataArray = apiResponse.getJsonData().getJSONArray("orders");
                    String dataStr = dataArray.toString();

                    // If data differs from the last one, update UI, else, do nothing
                    // We need to pass through toString method cause equals doesn't works with JSONArray
                    if (!dataStr.equals(lastFetchedData)) {

                        // Update information
                        lastUpdate = System.currentTimeMillis();

                        // Prevent next iteration
                        lastFetchedData = dataStr;

                        ArrayList<OrderItem> listReady = new ArrayList<>();
                        ArrayList<OrderItem> listPreparing = new ArrayList<>();
                        int quantA = 0, quantB = 0;

                        orderItems.clear();
                        for (int i = 0; i < dataArray.length(); i++) {
                            //orderItems.add(new OrderItem(jsonArray.getJSONObject(i)));

                            OrderItem oi = new OrderItem(dataArray.getJSONObject(i));

                            if (oi.getStatus() == 0) {
                                if (listPreparing.size() == 0) {
                                    listPreparing.add(new OrderItem("EN PRÉPARATION"));
                                }
                                listPreparing.add(oi);
                                quantA++;
                            } else {
                                if (listReady.size() == 0) {
                                    listReady.add(new OrderItem("PRÊTES"));
                                }
                                listReady.add(oi);
                                quantB++;
                            }
                        }

                        orderItems.addAll(listPreparing);
                        orderItems.addAll(listReady);
                        orderItems.add(new OrderItem("")); // Empty header to add space at the end of the listview

                        if (orderItems.size() == 1) // 1 cause empty order item (end space, see below)
                            tvNoOrder.setVisibility(View.VISIBLE);
                        else
                            tvNoOrder.setVisibility(View.INVISIBLE);

                        if (quantA > 0) orderItems.get(0).setFriendlyText("EN PRÉPARATION (" + quantA + ")");
                        if (quantB > 0) orderItems.get(listPreparing.size()).setFriendlyText("PRÊTES (" + quantB + ")");

                        mAdapter.notifyDataSetChanged();
                        recyList.setVisibility(View.VISIBLE);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    tvNetworkInfo.setText("Déconnecté - Erreur serveur");
                    imgNetworkInfo.setImageResource(R.drawable.ic_disconnected);
                }
            } else {
                tvNetworkInfo.setText("Déconnecté");
                imgNetworkInfo.setImageResource(R.drawable.ic_disconnected);
            }

            mHandler.postDelayed(updateTimerThread, RUN_UPDATE);
            run = true;
        }
    }

    /**
     * Custom definition for Adapter
     */
    private class OrderAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_ORDER_ITEM = 0;
        private static final int TYPE_ORDER_HEADER = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_ORDER_ITEM:
                    return new OrderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_item_order, parent, false));

                default:
                case TYPE_ORDER_HEADER:
                    return new OrderHeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.header_orders, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            OrderItem oi = orderItems.get(position);

            switch (getItemViewType(position)) {

                case TYPE_ORDER_ITEM:

                    OrderViewHolder ovh = (OrderViewHolder) holder;
                    ((GradientDrawable) ovh.viewHead.getBackground()).setColor(Color.parseColor(oi.getColorHtml()));
                    ovh.vID.setText(oi.getIdstr() + " " + new DecimalFormat("000").format(oi.getIdmod()));
                    ovh.vClient.setText(oi.getClientname());
                    ovh.vClient.setTextColor(Color.parseColor(oi.getColorHtml()));
                    ovh.vDate.setText(oi.getDate());
                    ovh.vPrice.setText(new DecimalFormat("0.00").format(oi.getPrice()) + "€" + " • " + oi.getPaidStatus());

                    if (oi.getInstructions().length() == 0) {
                        ovh.vInstructions.setVisibility(View.GONE);
                    } else {
                        ovh.vInstructions.setVisibility(View.VISIBLE);
                    }
                    ovh.vInstructions.setText("\"" + oi.getInstructions() + "\"");

                    break;


                case TYPE_ORDER_HEADER:

                    OrderHeaderViewHolder ohvh = (OrderHeaderViewHolder) holder;
                    ohvh.vTitle.setText(oi.getFriendlyText());
                    if (oi.getFriendlyText().length() == 0) {
                        ohvh.vIco.setVisibility(View.GONE);
                    } else {
                        ohvh.vIco.setVisibility(View.VISIBLE);
                    }

                    StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) ohvh.itemView.getLayoutParams();
                    layoutParams.setFullSpan(true);

                    break;
            }


        }

        @Override
        public int getItemCount() {
            return orderItems == null ? 0 : orderItems.size();
        }

        @Override
        public int getItemViewType(int position) {
            return orderItems.get(position).isHeader() ? TYPE_ORDER_HEADER : TYPE_ORDER_ITEM;
        }

        // Classic View Holder for order item
        public class OrderViewHolder extends RecyclerView.ViewHolder {

            protected TextView vID, vClient, vPrice, vDate, vInstructions;
            protected View viewHead;

            public OrderViewHolder(View v) {
                super(v);
                vID = (TextView) v.findViewById(R.id.tvOrderID);
                vClient = (TextView) v.findViewById(R.id.tvOrderClientName);
                vPrice = (TextView) v.findViewById(R.id.tvOrderPrice);
                vDate = (TextView) v.findViewById(R.id.tvOrderDate);
                vInstructions = (TextView) v.findViewById(R.id.tvOrderInstructions);
                viewHead = v.findViewById(R.id.view);
            }
        }

        // Classic View Holder for order header
        public class OrderHeaderViewHolder extends RecyclerView.ViewHolder {

            protected TextView vTitle;
            protected ImageView vIco;

            public OrderHeaderViewHolder(View v) {
                super(v);
                vTitle = (TextView) v.findViewById(R.id.tvHeaderOrders);
                vIco = (ImageView) v.findViewById(R.id.icoDown);
            }
        }
    }

    /**
     * Custom Async Task to set order status
     */
    private class AsyncSetStatus extends APIAsyncTask {

        private MaterialDialog materialDialog;
        private OrderItem orderItem;
        private ClubMember clubMember;

        public AsyncSetStatus(Context context, OrderItem orderItem) {
            super(context);
            this.orderItem = orderItem;
            this.clubMember = DataStore.getInstance().getClubMember();
            this.pairs.put("status", String.valueOf(this.orderItem.getOrderFullStatus()));
            this.pairs.put("idcmd", String.valueOf(this.orderItem.getIdcmd()));
            this.pairs.put("login", clubMember.getLogin());
            this.pairs.put("password", clubMember.getPassword());
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            materialDialog = new MaterialDialog.Builder(context)
                    .title("Mise à jour")
                    .content("Veuillez patienter ...")
                    .progress(true, 0)
                    .progressIndeterminateStyle(false)
                    .cancelable(false)
                    .show();
        }

        @Override
        protected void onPostExecute(APIResponse apiResponse) {
            materialDialog.hide();

            if (apiResponse.isValid()) {
                Toast.makeText(context, "Commande synchronisée !", Toast.LENGTH_SHORT).show();
                mAdapter.notifyDataSetChanged();

                // Sync new data
                AsyncOrders asyncOrders = new AsyncOrders(context);
                asyncOrders.execute(Constants.API_ORDER_LIST);

            } else {
                Toast.makeText(context, apiResponse.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Android App Lifecycle
     */
    @Override
    public void onResume() {
        super.onResume();
        firstDisplay = true;
        // Delay to update data
        run = true;
        lastFetchedData = "";

        if (progressBar != null) progressBar.setVisibility(View.INVISIBLE);

        if (mHandler == null) {
            mHandler = new android.os.Handler();
            mHandler.postDelayed(updateTimerThread, RUN_START);
        } else {
            mHandler.removeCallbacks(updateTimerThread);
            mHandler.postDelayed(updateTimerThread, RUN_START);
        }
    }

    @Override
    public void onPause() {
        if (mHandler != null) {
            mHandler.removeCallbacks(updateTimerThread);
        }
        run = false;
        super.onPause();
    }

    /**
     * Background task to fetch data periodically from server
     */
    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            try {
                if (run) {
                    run = false;
                    AsyncOrders asyncOrders = new AsyncOrders(context);
                    asyncOrders.execute(Constants.API_ORDER_LIST);
                }
            } catch (NullPointerException e) { // Stop handler if fragment disappears
                mHandler.removeCallbacks(updateTimerThread);
                run = false;
            }
        }
    };
}
