package spit.ecell.encrypto.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;

import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Score;
import spit.ecell.encrypto.ui.adapters.LeaderboardAdapter;
import spit.ecell.encrypto.util.FireStoreUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class LeaderboardFragment extends Fragment {
    private static final String TAG = "LeaderboardFragment";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    FireStoreUtils.FireStoreUtilCallbacks callbacks = new FireStoreUtils.FireStoreUtilCallbacks() {
        @Override
        public void onSuccess(Object object) {
            updateUI((ArrayList<Score>) object);
        }

        @Override
        public void onFailure(Object object) {
            Toast.makeText(getActivity(), "Failed to get transaction history", Toast.LENGTH_SHORT).show();
        }
    };


    public LeaderboardFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        recyclerView = view.findViewById(R.id.leaderboard_recyclerView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                FireStoreUtils.getLeaderboard(callbacks);
            }
        });

        swipeRefreshLayout.setRefreshing(true);
        FireStoreUtils.getLeaderboard(callbacks);

        return view;
    }

    public void updateUI(ArrayList<Score> scores) {
        Log.d(TAG, "updating UI ....");
        if (scores.size() != 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new LeaderboardAdapter(scores, getContext()));
        }
        swipeRefreshLayout.setRefreshing(false);
    }

}
