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

import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

import spit.ecell.encrypto.R;
import spit.ecell.encrypto.ui.activities.Constants;

public class ProfileFragment extends Fragment {
    SharedPreferences userPrefs;
    private OnProfileFragmentInteractionListener mListener;

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
        TextView about = view.findViewById(R.id.about_button);
        TextView logout = view.findViewById(R.id.logout_button);

        String full_name = userPrefs.getString(Constants.USER_NAME, getString(R.string.username)).trim();
        name.setText(full_name);
        email.setText(userPrefs.getString(Constants.USER_EMAIL, getString(R.string.email)));
        balance.setText(getString(R.string.balance) + ": " + userPrefs.getLong(Constants.FIRESTORE_USER_BALANCE_KEY, 0));

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LibsBuilder()
                        .withAboutAppName(getString(R.string.app_name))
                        .withAboutDescription(getString(R.string.app_description))
                        .withAboutIconShown(true)
                        .withAboutVersionShown(true)
                        .withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR)
                        .start(view.getContext());
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onLogout();
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnProfileFragmentInteractionListener) {
            mListener = (OnProfileFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnProfileFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnProfileFragmentInteractionListener {
        void onLogout();
    }

}