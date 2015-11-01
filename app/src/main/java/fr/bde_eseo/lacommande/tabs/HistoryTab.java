package fr.bde_eseo.lacommande.tabs;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import fr.bde_eseo.lacommande.R;

/**
 * Created by Rascafr on 19/10/2015.
 */
public class HistoryTab extends Fragment {

    // Layout UI Objects
    private RecyclerView recyList;
    private TrackHistoryAdapter mAdapter;

    // Model
    private ArrayList<String> historyArray;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_history, container, false);

        // Assign UI layout
        historyArray = new ArrayList<>();
        mAdapter = new TrackHistoryAdapter();
        recyList = (RecyclerView) rootView.findViewById(R.id.recyList);
        recyList.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recyList.setLayoutManager(llm);
        recyList.setAdapter(mAdapter);

        // Fill array with data
        for (int i=0;i<15;i++) {
            historyArray.add("Title Item " + i);
        }

        mAdapter.notifyDataSetChanged();

        return rootView;
    }


    // Simple adapter for history
    public class TrackHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @Override
        public int getItemCount() {
            return historyArray == null ? 0 : historyArray.size();
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            String s = historyArray.get(position);
            int type = getItemViewType(position);

            HistoryViewHolder hvh = (HistoryViewHolder) viewHolder;
            hvh.vTitle.setText(s);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

            return new HistoryViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card_history, viewGroup, false));
        }

        // Classic View Holder for history item
        public class HistoryViewHolder extends RecyclerView.ViewHolder {

            protected TextView vTitle;
            protected ImageView vIcon;

            public HistoryViewHolder(View v) {
                super(v);
                vTitle = (TextView) v.findViewById(R.id.historyTitle);
                //vIcon = (ImageView) v.findViewById(R.id.historyIcon);
            }
        }
    }

}
