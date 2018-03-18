package spit.ecell.encrypto.ui.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;

import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;
import spit.ecell.encrypto.ui.adapters.CurrencyAdapter;
import spit.ecell.encrypto.util.FireStoreUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment {
    private static final String TAG = "MarketFragment";

    private RecyclerView recyclerView;
    private TextView blankText;
    private ListenerRegistration registration;

    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_market, container, false);
        recyclerView = view.findViewById(R.id.market_recycler_view);
        blankText = view.findViewById(R.id.blankText);

        final FireStoreUtils.FireStoreUtilCallbacks callbacks =
                new FireStoreUtils.FireStoreUtilCallbacks() {

            @Override
            public void onSuccess(Object object) {
                updateUI((ArrayList<Currency>)object);
            }

            @Override
            public void onFailure(Object object) {
                Toast.makeText(getActivity(), "Market is not open for trading", Toast.LENGTH_SHORT).show();
            }
        };

        registration = FireStoreUtils.getCurrenciesRealTime(callbacks);
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
    }

    @Override
    public void onDestroy() {
        if(registration != null)
            registration.remove();
        super.onDestroy();
    }
}
