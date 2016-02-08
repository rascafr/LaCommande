package fr.bde_eseo.lacommande.tabs;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import fr.bde_eseo.lacommande.MainActivity;
import fr.bde_eseo.lacommande.R;
import fr.bde_eseo.lacommande.admin.ClubListActivity;
import fr.bde_eseo.lacommande.admin.ParametersAsyncDialog;
import fr.bde_eseo.lacommande.admin.PlanningActivity;
import fr.bde_eseo.lacommande.admin.ServiceAsyncDialog;
import fr.bde_eseo.lacommande.listeners.RecyclerItemClickListener;

/**
 * Created by Rascafr on 24/10/2015.
 */
public class AdminTab extends Fragment {

    // Model
    private ArrayList<AdminItem> adminItems;

    // UI Layout
    private RecyclerView recyList;
    private AdminItemAdapter mAdapter;

    // Action
    private static final int ACTION_CLUBS = 0;
    private static final int ACTION_PLANNING = 1;
    private static final int ACTION_SETTINGS = 2;
    private static final int ACTION_MESSAGE = 3;
    private static final int ACTION_MENUS = 4;
    private static final int ACTION_STOCKS = 5;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_admin, container, false);

        // Fill model array with icons data
        CharSequence titles[] = getResources().getStringArray(R.array.items_admin_titles);
        CharSequence desc[] = getResources().getStringArray(R.array.items_admin_descriptions);
        TypedArray icons = getResources().obtainTypedArray(R.array.items_admin_icons);
        adminItems = new ArrayList<>();
        for (int i = 0; i < titles.length; i++) {
            adminItems.add(new AdminItem((String) titles[i], (String) desc[i], icons.getResourceId(i, -1)));
        }
        icons.recycle();

        // Assign UI layout
        mAdapter = new AdminItemAdapter();
        recyList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recyList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

        // On click listener
        recyList.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                Intent myIntent = null;

                switch (position) {
                    case ACTION_CLUBS:
                        myIntent = new Intent(getActivity(), ClubListActivity.class);
                        break;

                    case ACTION_PLANNING:
                        myIntent = new Intent(getActivity(), PlanningActivity.class);
                        break;

                    case ACTION_SETTINGS:
                        new ParametersAsyncDialog(getActivity()).execute();
                        break;

                    case ACTION_MESSAGE:
                        new ServiceAsyncDialog(getActivity()).execute();
                        break;

                    case ACTION_MENUS:
                        Toast.makeText(getActivity(), "Fonctionnalité non disponible", Toast.LENGTH_SHORT).show();
                        break;

                    case ACTION_STOCKS:
                        Toast.makeText(getActivity(), "Fonctionnalité non disponible", Toast.LENGTH_SHORT).show();
                        break;
                }

                if (myIntent != null)
                    getActivity().startActivity(myIntent);

            }
        }));

        return rootView;
    }

    /**
     * Custom definition for model : icon + text + text
     */
    private class AdminItem {

        private String title, description;
        private int iconResource;

        public AdminItem(String title, String description, int iconResource) {
            this.title = title;
            this.description = description;
            this.iconResource = iconResource;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public int getIconResource() {
            return iconResource;
        }
    }

    /**
     * Custom definition for model adapter
     */
    private class AdminItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AdminItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_admin_set, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            AdminItem ai = adminItems.get(position);
            AdminItemViewHolder aivh = (AdminItemViewHolder) holder;
            aivh.vTitle.setText(ai.getTitle());
            aivh.vDesc.setText(ai.getDescription());
            aivh.vIcon.setImageResource(ai.getIconResource());
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public int getItemCount() {
            return adminItems == null ? 0 : adminItems.size();
        }

        // Classic View Holder for admin item
        public class AdminItemViewHolder extends RecyclerView.ViewHolder {

            protected TextView vTitle, vDesc;
            protected ImageView vIcon;

            public AdminItemViewHolder(View v) {
                super(v);
                vTitle = (TextView) v.findViewById(R.id.adminTitle);
                vDesc = (TextView) v.findViewById(R.id.adminDesc);
                vIcon = (ImageView) v.findViewById(R.id.adminIcon);
            }
        }
    }
}