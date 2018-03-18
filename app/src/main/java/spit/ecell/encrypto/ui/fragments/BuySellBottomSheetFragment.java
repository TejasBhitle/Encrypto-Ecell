package spit.ecell.encrypto.ui.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;
import spit.ecell.encrypto.util.FireStoreUtils;
import spit.ecell.encrypto.util.NetworkUtils;

/**
 * Created by tejas on 18/3/18.
 */

public class BuySellBottomSheetFragment extends BottomSheetDialogFragment {

    private boolean isBuySheet;
    private Double balance = null;
    private ListenerRegistration balanceListener;
    private Currency currency;
    private int ownedCurrencyQuantity = 0;
    private TextView header, valueText, costText, balanceText, quantityText, plus_minus, ownedText;
    private Button buySellButton;
    private AppCompatSeekBar seekBar;
    private ProgressBar progressBar;
    private LinearLayout sheetLayout;

    public BuySellBottomSheetFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if (bundle != null) {
            currency = bundle.getParcelable("currency");
            ownedCurrencyQuantity = bundle.getInt("owned", 0);
            isBuySheet = bundle.getBoolean("isBuySheet");
        }

        balanceListener = FireStoreUtils.getBalance(new FireStoreUtils.FireStoreUtilCallbacks() {
            @Override
            public void onSuccess(Object object) {
                balance = (Double) object;
                updateUI(currency, ownedCurrencyQuantity);
            }

            @Override
            public void onFailure(Object object) {
            }
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.bottom_sheet_buy_sell, container, false);

        header = view.findViewById(R.id.header);
        valueText = view.findViewById(R.id.value);
        costText = view.findViewById(R.id.cost);
        balanceText = view.findViewById(R.id.balance);
        quantityText = view.findViewById(R.id.quantity);
        seekBar = view.findViewById(R.id.seekbar);
        progressBar = view.findViewById(R.id.progressBar);
        sheetLayout = view.findViewById(R.id.sheetLayout);
        buySellButton = view.findViewById(R.id.confirm_button);
        plus_minus = view.findViewById(R.id.plus_minus);
        ownedText = view.findViewById(R.id.owned);

        progressBar.setVisibility(View.VISIBLE);
        sheetLayout.setVisibility(View.INVISIBLE);

        return view;
    }

    public void updateUI(final Currency currency, int ownedCurrencyQuantity) {
        this.currency = currency;
        this.ownedCurrencyQuantity = ownedCurrencyQuantity;
        final double value = currency.getCurrentValue();

        if (!isVisible()) return;
        if (balance == null) return;
        seekBar.setProgress(0);
        progressBar.setVisibility(View.GONE);
        sheetLayout.setVisibility(View.VISIBLE);

        if (isBuySheet) {
            header.setText(getString(R.string.buy_currency_placeholder, currency.getSymbol()));
            plus_minus.setText("-");
            seekBar.setMax((int) (balance / value));
            buySellButton.setText(R.string.confirm_purchase);
        } else {
            header.setText(getString(R.string.sell_currency_placeholder, currency.getSymbol()));
            plus_minus.setText("+");
            seekBar.setMax(ownedCurrencyQuantity);
            buySellButton.setText(R.string.confirm_sale);
        }

        valueText.setText(String.valueOf(value));
        costText.setText("0");
        quantityText.setText("0");
        balanceText.setText(String.valueOf(balance));
        ownedText.setText(String.valueOf(ownedCurrencyQuantity));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    double cost = value * progress;
                    double newBalance = (isBuySheet) ? (balance - cost) : (balance + cost);
                    quantityText.setText(String.valueOf(progress));
                    costText.setText(String.valueOf(cost));
                    balanceText.setText(String.valueOf(newBalance));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        buySellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!NetworkUtils.isNetworkConnected(getActivity())) {
                    Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (balance != null) {//safe check to avoid failure
                    if (seekBar.getProgress() > 0) {
                        if (isBuySheet) {
                            Toast.makeText(getActivity(),
                                    "Purchase confirmed for " + seekBar.getProgress() + " " + currency.getSymbol() +
                                            " for " + costText.getText(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(),
                                    "Sale confirmed for " + seekBar.getProgress() + " " + currency.getSymbol() +
                                            " for " + costText.getText(), Toast.LENGTH_SHORT).show();
                        }
                        sheetLayout.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.VISIBLE);
                        FireStoreUtils.buySellCurrency(currency, seekBar.getProgress(), isBuySheet);
                    }
                }
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (balanceListener != null)
            balanceListener.remove();
    }
}
