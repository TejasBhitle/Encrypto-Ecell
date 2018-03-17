package spit.ecell.encrypto.ui.fragments;


import android.content.Context;
import android.content.SharedPreferences;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

import spit.ecell.encrypto.Constants;
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

    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_market, container, false);
        recyclerView = view.findViewById(R.id.market_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        blankText = view.findViewById(R.id.blankText);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getCurrencies();
            }
        });
        getCurrencies();
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

    public void getCurrencies() {
        final ArrayList<Currency> currencies = new ArrayList<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final SharedPreferences prefs = getActivity().getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);

        if (user == null) {
            Log.d(TAG, "user is null");
            return;
        }
        swipeRefreshLayout.setRefreshing(true);
        db.collection(Constants.FIRESTORE_CURRENCIES_KEY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "Success");
                            for (DocumentSnapshot document : task.getResult()) {
                                Map<String, Object> object = document.getData();
                                Log.e(TAG, object.toString());
                                String id = document.getId();
                                String symbol = (String) object.get("symbol");
                                String desc = (String) object.get("desc");
                                String name = (String) object.get("name");
                                Double value = Double.parseDouble(object.get("value-now").toString());
                                Double variation = Double.parseDouble(object.get("variation").toString());
                                Double circulation = Double.parseDouble(object.get("circulation").toString());
                                Double factor = Double.parseDouble(object.get("variation-factor").toString());
                                Integer owned = prefs.getInt("OWNED_" + symbol, 0);
                                currencies.add(new Currency(id, symbol, name, desc, value, variation, owned, factor, circulation));
                            }
                            updateUI(currencies);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "Market is not open for trading", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
