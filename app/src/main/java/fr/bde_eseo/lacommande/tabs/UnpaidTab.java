package fr.bde_eseo.lacommande.tabs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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
 * Created by Rascafr on 11/11/2015.
 * Same as history but without headers
 */
public class UnpaidTab extends Fragment {

    // Layout UI Objects
    private RecyclerView recyList;
    private UnpaidAdapter mAdapter;
    private ProgressBar progress;
    private TextView tvNoUnpaid;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.OnItemTouchListener disabler;

    // Model
    private ArrayList<OrderItem> orderItems;

    // Android
    private Context context;

    // Progress circle visibility flag
    private static boolean firstSync;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_unpaid, container, false);
        context = getActivity();

        // Assign UI layout
        orderItems = new ArrayList<>();
        mAdapter = new UnpaidAdapter();
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.order_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);
        disabler = new RecyclerViewDisabler();
        recyList = (RecyclerView) rootView.findViewById(R.id.recyList);
        progress = (ProgressBar) rootView.findViewById(R.id.progressLoad);
        progress.setVisibility(View.INVISIBLE);
        recyList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(context);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);
        tvNoUnpaid = (TextView) rootView.findViewById(R.id.tvNoOrder);
        tvNoUnpaid.setVisibility(View.INVISIBLE);

        // Fill array with data
        firstSync = true;
        AsyncUnpaid asyncUnpaid = new AsyncUnpaid(context);
        asyncUnpaid.execute(Constants.API_ORDER_UNPAID);

        // Swipe-to-refresh implementation
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncUnpaid asyncUnpaid = new AsyncUnpaid(context); // no circle here (already in SwipeLayout)
                asyncUnpaid.execute(Constants.API_ORDER_UNPAID);
            }
        });

        // On click listener
        recyList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final OrderItem oi = orderItems.get(position);
                new MaterialDialog.Builder(context)
                        .title("Régulariser le client ?")
                        .content(oi.getClientname() + " pourra de nouveau commander à la cafétéria.\n" +
                                "Vérifiez d'avoir bien encaissé la somme de " + new DecimalFormat("0.00").format(oi.getPrice()) + "€" + ", puis confirmez.")
                        .positiveText("Confirmer")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                                AsyncPayUnpaid asyncPayUnpaid = new AsyncPayUnpaid(context, oi);
                                asyncPayUnpaid.execute(Constants.API_ORDER_PAY);
                            }
                        })
                        .negativeText("Annuler")
                        .show();
            }
        }));

        return rootView;
    }


    // Scroll listener to prevent issue 77846
    public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return true;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {}

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {}
    }

    // Simple adapter for history
    public class UnpaidAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_ITEM = 0;

        @Override
        public int getItemCount() {
            return orderItems == null ? 0 : orderItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            OrderItem oi = orderItems.get(position);

            UnpaidViewHolder uvh = (UnpaidViewHolder) viewHolder;
            uvh.vNumero.setText(oi.getIdstr() + " " + new DecimalFormat("000").format(oi.getIdmod()));
            uvh.vPrice.setText(new DecimalFormat("0.00").format(oi.getPrice()) + "€");
            uvh.vClient.setText(oi.getClientname());
            uvh.vData.setText(Html.fromHtml(oi.getFriendlyText()));
            uvh.vDate.setText(oi.getDate());
            ((GradientDrawable) uvh.vColor.getBackground()).setColor(Color.parseColor(oi.getColorHtml()));
        }

        @Override
        public int getItemViewType(int position) {
            return TYPE_ITEM;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            return new UnpaidViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_history, viewGroup, false));
        }

        // Classic View Holder for unpaid item
        public class UnpaidViewHolder extends RecyclerView.ViewHolder {

            protected TextView vNumero, vClient, vDate, vData, vPrice;
            protected View vColor;

            public UnpaidViewHolder(View v) {
                super(v);
                vNumero = (TextView) v.findViewById(R.id.historyNumero);
                vClient = (TextView) v.findViewById(R.id.historyClient);
                vDate = (TextView) v.findViewById(R.id.historyDate);
                vData = (TextView) v.findViewById(R.id.historyData);
                vPrice = (TextView) v.findViewById(R.id.historyPrice);
                vColor = v.findViewById(R.id.view);
            }
        }
    }

    /**
     * Async task to fetch unpaid data
     */
    private class AsyncUnpaid extends APIAsyncTask {

        public AsyncUnpaid(Context context) {
            super(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            orderItems.clear();
            if (firstSync) progress.setVisibility(View.VISIBLE);
            recyList.addOnItemTouchListener(disabler);
        }

        @Override
        protected void onPostExecute(APIResponse apiResponse) {

            progress.setVisibility(View.INVISIBLE);
            recyList.setVisibility(View.VISIBLE);

            if (apiResponse.isValid()) {

                if (firstSync) firstSync = false; // disable circle flag if data is valid

                try {

                    // Orders unpaid
                    JSONArray array = apiResponse.getJsonData().getJSONArray("unpaid");

                    for (int i = 0; i < array.length(); i++) {

                        // Add element → JSON data as values
                        orderItems.add(new OrderItem(array.getJSONObject(i)));
                    }

                    if (orderItems.size() == 0)
                        tvNoUnpaid.setVisibility(View.VISIBLE);
                    else
                        tvNoUnpaid.setVisibility(View.INVISIBLE);

                    mAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Erreur serveur", Toast.LENGTH_SHORT).show();
                }
                mAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(context, "Erreur réseau", Toast.LENGTH_SHORT).show();
            }

            swipeRefreshLayout.setRefreshing(false);
            recyList.removeOnItemTouchListener(disabler);
        }
    }


    /**
     * AsyncTask to regularize order
     */
    private class AsyncPayUnpaid extends APIAsyncTask {

        private MaterialDialog materialDialog;
        private ClubMember clubMember;

        public AsyncPayUnpaid(Context context, OrderItem orderItem) {
            super(context);
            clubMember = DataStore.getInstance().getClubMember();
            this.pairs.put("idcmd", String.valueOf(orderItem.getIdcmd()));
            this.pairs.put("login", clubMember.getLogin());
            this.pairs.put("password", clubMember.getPassword());
        }

        @Override
        protected void onPreExecute() {
            materialDialog = new MaterialDialog.Builder(context)
                    .title("Régularisation du client")
                    .content("Veuillez patienter ...")
                    .progressIndeterminateStyle(false)
                    .progress(true, 0)
                    .cancelable(true)
                    .show();
        }

        @Override
        protected void onPostExecute(APIResponse apiResponse) {

            materialDialog.hide();

            if (apiResponse.isValid()) {

                // Fetch new data from server
                AsyncUnpaid asyncUnpaid = new AsyncUnpaid(context); // no circle here (already in SwipeLayout)
                asyncUnpaid.execute(Constants.API_ORDER_UNPAID);

                // Confirm
                Toast.makeText(context, "Client régularisé !", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, apiResponse.getError(), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
