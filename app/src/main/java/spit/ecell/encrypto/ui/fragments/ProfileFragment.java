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

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.util.FireStoreUtils;

public class ProfileFragment extends Fragment {
    SharedPreferences userPrefs;
    private Double balance;
    private ListenerRegistration balanceListener;

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

        String full_name = userPrefs.getString(Constants.USER_NAME, getString(R.string.username)).trim();
        name.setText(full_name);
        email.setText(userPrefs.getString(Constants.USER_EMAIL, getString(R.string.email)));

        balanceListener = FireStoreUtils.getBalance(new FireStoreUtils.FireStoreUtilCallbacks() {
            @Override
            public void onSuccess(Object object) {
                balance = (Double) object;
                balanceTextView.setText(getString(R.string.balance) + ": " + formatter.format(balance));
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
    }
}