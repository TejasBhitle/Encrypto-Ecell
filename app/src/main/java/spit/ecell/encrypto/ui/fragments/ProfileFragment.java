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

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;

public class ProfileFragment extends Fragment {
    SharedPreferences userPrefs;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_profile, container, false);

        userPrefs = view.getContext().getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);

        TextView name = view.findViewById(R.id.name);
        TextView email = view.findViewById(R.id.email);
        TextView balance = view.findViewById(R.id.balance);

        String full_name = userPrefs.getString(Constants.USER_NAME, getString(R.string.username)).trim();
        name.setText(full_name);
        email.setText(userPrefs.getString(Constants.USER_EMAIL, getString(R.string.email)));
        balance.setText(getString(R.string.balance) + ": " + userPrefs.getFloat(Constants.FS_USER_BALANCE_KEY, 0));

        return view;
    }

}