package spit.ecell.encrypto.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;

import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Transaction;
import spit.ecell.encrypto.ui.adapters.TransactionAdapter;
import spit.ecell.encrypto.util.FireStoreUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionsFragment extends Fragment {
    private static final String TAG = "TransactionFragment";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView blankText;
    FireStoreUtils.FireStoreUtilCallbacks callbacks = new FireStoreUtils.FireStoreUtilCallbacks() {
        @Override
        public void onSuccess(Object object) {
            updateUI((ArrayList<Transaction>) object);
        }

        @Override
        public void onFailure(Object object) {
            Toast.makeText(getActivity(), "Failed to get transaction history", Toast.LENGTH_SHORT).show();
        }
    };

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.transactions_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        blankText = view.findViewById(R.id.blankText);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                FireStoreUtils.getTransactions(callbacks);
            }
        });

        swipeRefreshLayout.setRefreshing(true);
        FireStoreUtils.getTransactions(callbacks);

        return view;
    }

    public void updateUI(ArrayList<Transaction> transactions) {
        Log.d(TAG, "updating UI ....");
        if (transactions.size() != 0) {
            Collections.reverse(transactions);
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new TransactionAdapter(transactions, getContext()));
            blankText.setVisibility(View.GONE);
        } else {
            blankText.setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

}
