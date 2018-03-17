package spit.ecell.encrypto.ui.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import spit.ecell.encrypto.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MarketFragment extends Fragment {
    private OnMarketFragmentInteractionListener mListener;



    public MarketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_market, container, false);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMarketFragmentInteractionListener) {
            mListener = (OnMarketFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMarketFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnMarketFragmentInteractionListener {
        void onListItemClicked(String currencyId);

        void onBuyOrder(String currencyId);

        void onSellOrder(String currencyId);
    }


}
