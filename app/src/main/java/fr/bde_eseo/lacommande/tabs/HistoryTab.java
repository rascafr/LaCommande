package fr.bde_eseo.lacommande.tabs;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import fr.bde_eseo.lacommande.Constants;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.model.OrderItem;
import fr.bde_eseo.lacommande.utils.APIResponse;
import fr.bde_eseo.lacommande.utils.APIUtils;

/**
 * Created by Rascafr on 19/10/2015.
 */
public class HistoryTab extends Fragment {

    // Layout UI Objects
    private RecyclerView recyList;
    private HistoryAdapter mAdapter;
    private ProgressBar progress;
    private TextView tvPage;
    private ImageView actionStart, actionPrevious, actionNext, actionEnd;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.OnItemTouchListener disabler;

    // Model
    private ArrayList<OrderItem> orderItems;

    // Android
    private Context context;

    // Page
    private static int currentPage = 0;
    private static int nbPages = 0;

    // Progress circle visibility flag
    private static boolean firstSync;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_history, container, false);
        context = getActivity();

        // Assign UI layout
        orderItems = new ArrayList<>();
        mAdapter = new HistoryAdapter();
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.history_refresh);
        swipeRefreshLayout.setColorSchemeColors(R.color.colorPrimary);
        disabler = new RecyclerViewDisabler();
        recyList = (RecyclerView) rootView.findViewById(R.id.recyList);
        progress = (ProgressBar) rootView.findViewById(R.id.progressLoad);
        tvPage = (TextView) rootView.findViewById(R.id.tvPage);
        actionStart = (ImageView) rootView.findViewById(R.id.actionStart);
        actionPrevious = (ImageView) rootView.findViewById(R.id.actionPrevious);
        actionNext = (ImageView) rootView.findViewById(R.id.actionNext);
        actionEnd = (ImageView) rootView.findViewById(R.id.actionEnd);
        progress.setVisibility(View.INVISIBLE);
        recyList.setHasFixedSize(false);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Fill array with data
        firstSync = true;
        AsyncHistory asyncHistory = new AsyncHistory();
        asyncHistory.execute("0");

        // Action - listeners
        actionStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 0) {
                    currentPage = 0;
                    AsyncHistory asyncHistory = new AsyncHistory();
                    asyncHistory.execute(String.valueOf(currentPage));
                    Toast.makeText(context, "Chargement début historique ...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Limite atteinte", Toast.LENGTH_SHORT).show();
                }
            }
        });

        actionPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 0) {
                    currentPage--;
                    AsyncHistory asyncHistory = new AsyncHistory();
                    asyncHistory.execute(String.valueOf(currentPage));
                    Toast.makeText(context, "Chargement page précédente ...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Limite atteinte", Toast.LENGTH_SHORT).show();
                }
            }
        });

        actionNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < nbPages) {
                    currentPage++;
                    AsyncHistory asyncHistory = new AsyncHistory();
                    asyncHistory.execute(String.valueOf(currentPage));
                    Toast.makeText(context, "Chargement page suivante ...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Limite atteinte", Toast.LENGTH_SHORT).show();
                }
            }
        });

        actionEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < nbPages) {
                    currentPage = nbPages;
                    AsyncHistory asyncHistory = new AsyncHistory();
                    asyncHistory.execute(String.valueOf(currentPage));
                    Toast.makeText(context, "Chargement fin historique ...", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, "Limite atteinte", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Swipe-to-refresh implementation
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                AsyncHistory asyncHistory = new AsyncHistory(); // no circle here (already in SwipeLayout)
                asyncHistory.execute(String.valueOf(currentPage));
            }
        });

        return rootView;
    }


    // Scroll listener to prevent issue 77846
    public class RecyclerViewDisabler implements RecyclerView.OnItemTouchListener {

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            return true;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }


    // Simple adapter for history
    public class HistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private final static int TYPE_ITEM = 0;
        private final static int TYPE_HEADER = 1;
        private final static int TYPE_HEADER_TOP = 2;

        @Override
        public int getItemCount() {
            return orderItems == null ? 0 : orderItems.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            OrderItem oi = orderItems.get(position);
            int type = getItemViewType(position);

            if (type == TYPE_ITEM) {
                HistoryViewHolder hvh = (HistoryViewHolder) viewHolder;
                hvh.vNumero.setText(oi.getIdstr() + " " + new DecimalFormat("000").format(oi.getIdmod()));
                hvh.vPrice.setText(new DecimalFormat("0.00").format(oi.getPrice()) + "€");
                hvh.vClient.setText(oi.getClientname());
                hvh.vData.setText(Html.fromHtml(oi.getFriendlyText()));
                hvh.vDate.setText(oi.getDate());
                ((GradientDrawable) hvh.vColor.getBackground()).setColor(Color.parseColor(oi.getColorHtml()));
            } else if (type == TYPE_HEADER || type == TYPE_HEADER_TOP) {
                HistoryHeaderViewHolder hhvh = (HistoryHeaderViewHolder) viewHolder;
                hhvh.vHeader.setText(oi.getFriendlyText());
            }
        }

        @Override
        public int getItemViewType(int position) {
            return orderItems == null ? TYPE_ITEM : (orderItems.get(position).isHeader() ? position == 0 ? TYPE_HEADER_TOP : TYPE_HEADER : TYPE_ITEM);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            if (viewType == TYPE_ITEM)
                return new HistoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_history, viewGroup, false));
            else if (viewType == TYPE_HEADER)
                return new HistoryHeaderViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_header_history, viewGroup, false));
            else
                return new HistoryHeaderViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_header_history_top, viewGroup, false));
        }

        // Classic View Holder for history item
        public class HistoryViewHolder extends RecyclerView.ViewHolder {

            protected TextView vNumero, vClient, vDate, vData, vPrice;
            protected View vColor;

            public HistoryViewHolder(View v) {
                super(v);
                vNumero = (TextView) v.findViewById(R.id.historyNumero);
                vClient = (TextView) v.findViewById(R.id.historyClient);
                vDate = (TextView) v.findViewById(R.id.historyDate);
                vData = (TextView) v.findViewById(R.id.historyData);
                vPrice = (TextView) v.findViewById(R.id.historyPrice);
                vColor = v.findViewById(R.id.view);
            }
        }

        // Classic View Holder for history header
        public class HistoryHeaderViewHolder extends RecyclerView.ViewHolder {

            protected TextView vHeader;

            public HistoryHeaderViewHolder(View v) {
                super(v);
                vHeader = (TextView) v.findViewById(R.id.historyHeader);
            }
        }
    }

    /**
     * Async task to fetch history data
     */
    private class AsyncHistory extends AsyncTask<String, String, APIResponse> {

        @Override
        protected APIResponse doInBackground(String... params) {
            HashMap<String, String> pairs = new HashMap<>();
            pairs.put("page", params[0]);
            return APIUtils.postAPIData(Constants.API_ORDER_HISTORY, pairs, context);
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

                    // Order history
                    JSONArray array = apiResponse.getJsonData().getJSONArray("history");

                    // Last date for header positioning
                    String lastDate = "";

                    for (int i = 0; i < array.length(); i++) {

                        OrderItem oi = new OrderItem(array.getJSONObject(i));

                        String subDate = oi.getDate().substring(0, oi.getDate().indexOf("-") - 1);

                        if (!lastDate.equals(subDate)) {
                            orderItems.add(new OrderItem(subDate)); // Is header ? date as name
                            lastDate = subDate;
                        }

                        // Then add element → JSON data as values
                        orderItems.add(new OrderItem(array.getJSONObject(i)));
                    }

                    // Set to 0
                    if (orderItems.size() > 0)
                        recyList.scrollToPosition(0);

                    // Current page
                    nbPages = apiResponse.getJsonData().getInt("pages") - 1; // cause 0 = page n°1
                    tvPage.setText((currentPage + 1) + " / " + (nbPages + 1));

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

}
