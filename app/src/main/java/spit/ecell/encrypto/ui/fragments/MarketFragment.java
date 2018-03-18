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
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import spit.ecell.encrypto.FireStoreUtil;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;
import spit.ecell.encrypto.ui.adapters.CurrencyAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment {
    private static final String TAG = "MarketFragment";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView blankText;
    private FireStoreUtil fireStoreUtil;

    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fireStoreUtil = new FireStoreUtil(getActivity());

        View view = inflater.inflate(R.layout.fragment_market, container, false);
        recyclerView = view.findViewById(R.id.market_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        blankText = view.findViewById(R.id.blankText);

        final FireStoreUtil.FireStoreUtilCallbacks callbacks = new FireStoreUtil.FireStoreUtilCallbacks() {

            @Override
            public void onSuccess(Object object) {
                updateUI((ArrayList<Currency>)object);
            }

            @Override
            public void onFailure(Object object) {
                Toast.makeText(getActivity(), "Market is not open for trading", Toast.LENGTH_SHORT).show();
            }
        };

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                fireStoreUtil.getCurrencies(callbacks);
            }
        });
        swipeRefreshLayout.setRefreshing(true);
        fireStoreUtil.getCurrencies(callbacks);
        return view;
    }

    public void updateUI(ArrayList<Currency> currencies) {
        Log.d(TAG, "updating UI ....");
        if (currencies.size() != 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new CurrencyAdapter(currencies, getContext()));
            blankText.setVisibility(View.GONE);
        } else {
            blankText.setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

}
