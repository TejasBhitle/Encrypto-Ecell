package spit.ecell.encrypto.ui.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tejas on 18/3/18.
 */

public class BuySellBottomSheetFragment extends BottomSheetDialogFragment {

    private boolean isBuySheet;
    private Currency currency;
    private TextView header,valueText,costText,balanceText;
    private AppCompatSeekBar seekBar;
    private SharedPreferences preferences;

    public BuySellBottomSheetFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null){
            currency = bundle.getParcelable("currency");
            isBuySheet = bundle.getBoolean("isBuySheet");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.bottom_sheet_buy,container,false);

        header = view.findViewById(R.id.buy_header);
        valueText = view.findViewById(R.id.value);
        costText = view.findViewById(R.id.cost);
        balanceText = view.findViewById(R.id.balance);
        seekBar = view.findViewById(R.id.seekbar);

        preferences = getActivity().getSharedPreferences(Constants.PREFS, MODE_PRIVATE);
        updateUI(currency);

        view.findViewById(R.id.confirm_purchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (seekBar.getProgress() > 0) {
                    // TODO: Buy request
                    Toast.makeText(getActivity(),
                            "Purchase confirmed for " + seekBar.getProgress() + " " + currency.getSymbol() +
                                    " for " + costText.getText(), Toast.LENGTH_SHORT).show();
                }
                //TODO: ->view.dismiss();

            }
        });

        return view;
    }

    public void updateUI(final Currency currency){
        this.currency = currency;
        final double balance = preferences.getFloat(Constants.FIRESTORE_USER_BALANCE_KEY, 0);
        final double value = currency.getCurrentValue();

        header.setText("Buy " + currency.getSymbol());
        valueText.setText(getString(R.string.dollar_symbol) + value);
        costText.setText("0");
        balanceText.setText(getString(R.string.dollar_symbol) + balance);

        seekBar.setMax((int) (balance / value));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    double cost = value * progress;
                    double newBalance = (balance - cost);
                    costText.setText(getString(R.string.dollar_symbol) + cost);
                    balanceText.setText(getString(R.string.dollar_symbol) + newBalance);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


    }
}
