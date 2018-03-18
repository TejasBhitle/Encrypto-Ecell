package spit.ecell.encrypto.ui.fragments;

import android.content.SharedPreferences;
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

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.FireStoreUtil;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by tejas on 18/3/18.
 */

public class BuySellBottomSheetFragment extends BottomSheetDialogFragment {

    private boolean isBuySheet;
    private Double balance = null;
    private ListenerRegistration balanceListener;
    private Currency currency;
    private TextView header,valueText,costText,balanceText,quantityText;
    private Button buySellButton;
    private AppCompatSeekBar seekBar;
    private SharedPreferences preferences;
    private FireStoreUtil fireStoreUtil;
    private ProgressBar progressBar;
    private LinearLayout sheetLayout;

    public BuySellBottomSheetFragment(){}

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null){
            currency = bundle.getParcelable("currency");
            isBuySheet = bundle.getBoolean("isBuySheet");
        }

        fireStoreUtil = new FireStoreUtil(getActivity());
        balanceListener = fireStoreUtil.getBalance(new FireStoreUtil.FireStoreUtilCallbacks() {
            @Override
            public void onSuccess(Object object) {
                balance = (Double)object;
                updateUI(currency);
            }

            @Override
            public void onFailure(Object object) {}
        });

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.bottom_sheet_buy,container,false);

        header = view.findViewById(R.id.buy_header);
        valueText = view.findViewById(R.id.value);
        costText = view.findViewById(R.id.cost);
        balanceText = view.findViewById(R.id.balance);
        quantityText =view.findViewById(R.id.quantity);
        seekBar = view.findViewById(R.id.seekbar);
        progressBar = view.findViewById(R.id.progressBar);
        sheetLayout = view.findViewById(R.id.sheetLayout);
        buySellButton = view.findViewById(R.id.confirm_purchase);

        preferences = getActivity().getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

        progressBar.setVisibility(View.VISIBLE);
        sheetLayout.setVisibility(View.GONE);

        return view;
    }

    public void updateUI(final Currency currency){
        this.currency = currency;
        final double value = currency.getCurrentValue();

        if(!isVisible()) return;
        if(balance == null)return;
        progressBar.setVisibility(View.GONE);
        sheetLayout.setVisibility(View.VISIBLE);

        header.setText("Buy " + currency.getSymbol());
        valueText.setText(getString(R.string.dollar_symbol) + value);
        costText.setText("0");
        quantityText.setText("x 0");
        balanceText.setText(getString(R.string.dollar_symbol) + balance);

        seekBar.setMax((int) (balance / value));
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    double cost = value * progress;
                    double newBalance = (balance - cost);
                    quantityText.setText("x "+progress);
                    costText.setText(getString(R.string.dollar_symbol) + cost);
                    balanceText.setText(getString(R.string.dollar_symbol) + newBalance);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        buySellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(balance != null) {//safe check to avoid failure
                    if (seekBar.getProgress() > 0) {
                        Toast.makeText(getActivity(),
                        "Purchase confirmed for " +seekBar.getProgress()+ " "+currency.getSymbol()+
                        " for " + costText.getText(), Toast.LENGTH_SHORT).show();
                        fireStoreUtil.buyCurrency(currency, seekBar.getProgress());
                        dismiss();
                    }
                }
            }
        });


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(balanceListener != null)
            balanceListener.remove();
    }
}
