package spit.ecell.encrypto.ui.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.DecimalFormat;
import java.util.HashMap;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.util.FireStoreUtils;

public class ProfileFragment extends Fragment {
    SharedPreferences userPrefs;
    private Double balance, valuation;
    private ListenerRegistration balanceListener, valuationListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userPrefs = view.getContext().getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        final DecimalFormat formatter = new DecimalFormat("0.00");

        TextView name = view.findViewById(R.id.name);
        TextView email = view.findViewById(R.id.email);
        final TextView balanceTextView = view.findViewById(R.id.balance);
        final TextView netWorthTextView = view.findViewById(R.id.worth);

        String full_name = userPrefs.getString(Constants.USER_NAME, getString(R.string.username)).trim();
        name.setText(full_name);
        email.setText(userPrefs.getString(Constants.USER_EMAIL, getString(R.string.email)));

        balanceListener = FireStoreUtils.getBalance(new FireStoreUtils.FireStoreUtilCallbacks() {
            @Override
            public void onSuccess(Object object) {
                balance = (Double) object;
                balanceTextView.setText(getString(R.string.balance_placeholder, formatter.format(balance)));
            }

            @Override
            public void onFailure(Object object) {
            }
        });

        valuationListener = FireStoreUtils.getTotalValuation(new FireStoreUtils.FireStoreUtilCallbacks() {
            @Override
            public void onSuccess(Object object) {
                valuation = (Double) object;
                netWorthTextView.setText(getString(R.string.net_worth_placeholder, formatter.format(valuation)));
            }

            @Override
            public void onFailure(Object object) {
            }
        });

        // the dirtiest implementation of portfolio ever
        FireStoreUtils.getOwnedCurrencies(new FireStoreUtils.FireStoreUtilCallbacks() {
            @Override
            public void onSuccess(Object object) {
                HashMap<String, Long> purchased_currencies = (HashMap<String, Long>) object;
                if (purchased_currencies.containsKey(FireStoreUtils.currencyIdNameMap.get("Bitcoin"))) {
                    ((TextView) view.findViewById(R.id.btc))
                            .setText(String.valueOf(purchased_currencies.get(FireStoreUtils.currencyIdNameMap.get("Bitcoin")).intValue()));
                }
                if (purchased_currencies.containsKey(FireStoreUtils.currencyIdNameMap.get("Ethereum"))) {
                    ((TextView) view.findViewById(R.id.eth))
                            .setText(String.valueOf(purchased_currencies.get(FireStoreUtils.currencyIdNameMap.get("Ethereum")).intValue()));
                }
                if (purchased_currencies.containsKey(FireStoreUtils.currencyIdNameMap.get("Monero"))) {
                    ((TextView) view.findViewById(R.id.xmr))
                            .setText(String.valueOf(purchased_currencies.get(FireStoreUtils.currencyIdNameMap.get("Monero")).intValue()));
                }
                if (purchased_currencies.containsKey(FireStoreUtils.currencyIdNameMap.get("Dash"))) {
                    ((TextView) view.findViewById(R.id.dash))
                            .setText(String.valueOf(purchased_currencies.get(FireStoreUtils.currencyIdNameMap.get("Dash")).intValue()));
                }
                if (purchased_currencies.containsKey(FireStoreUtils.currencyIdNameMap.get("e-Sikka"))) {
                    ((TextView) view.findViewById(R.id.sik))
                            .setText(String.valueOf(purchased_currencies.get(FireStoreUtils.currencyIdNameMap.get("e-Sikka")).intValue()));
                }
                if (purchased_currencies.containsKey(FireStoreUtils.currencyIdNameMap.get("Litecoin"))) {
                    ((TextView) view.findViewById(R.id.ltc))
                            .setText(String.valueOf(purchased_currencies.get(FireStoreUtils.currencyIdNameMap.get("Litecoin")).intValue()));
                }
            }

            @Override
            public void onFailure(Object object) {

            }
        });
        
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (balanceListener != null)
            balanceListener.remove();
        if (valuationListener != null)
            valuationListener.remove();
    }
}