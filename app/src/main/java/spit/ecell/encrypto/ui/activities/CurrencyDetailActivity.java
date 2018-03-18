package spit.ecell.encrypto.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;
import spit.ecell.encrypto.ui.fragments.BuySellBottomSheetFragment;
import spit.ecell.encrypto.util.FireStoreUtils;

public class CurrencyDetailActivity extends AppCompatActivity {
    private Currency currency;
    private SharedPreferences preferences;
    private ListenerRegistration currencyListener;

    private TextView descriptionView, symbol, variation, value;

    private BuySellBottomSheetFragment buySellBottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_detail);

        preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

        descriptionView = findViewById(R.id.description);
        symbol = findViewById(R.id.symbol);
        variation = findViewById(R.id.variation);
        value = findViewById(R.id.value);

        Bundle extras = getIntent().getExtras();

        if (extras == null || !extras.containsKey(Constants.FS_CURRENCIES_KEY)) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currency = extras.getParcelable(Constants.FS_CURRENCIES_KEY);
        buySellBottomSheetFragment = new BuySellBottomSheetFragment();


        findViewById(R.id.buyButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBuyButtonPressed();
            }
        });

        findViewById(R.id.sellButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSellButtonPressed();
            }
        });

        currencyListener = FireStoreUtils.getCurrencyRealTimeById(
                currency.getId(),
                new FireStoreUtils.FireStoreUtilCallbacks() {
                    @Override
                    public void onSuccess(Object object) {
                        updateUI((Currency) object);
                    }

                    @Override
                    public void onFailure(Object object) {

                    }
                }
        );
    }

    private void updateUI(Currency currency) {
        setTitle(currency.getName());
        descriptionView.setText(currency.getDesc());
        symbol.setText(currency.getSymbol());
        value.setText(getString(R.string.dollar_symbol) + currency.getCurrentValue());
        if (currency.getVariation() >= 0) {
            variation.setText("+" + currency.getVariation() + "%");
        } else {
            variation.setText(currency.getVariation() + "%");
            variation.setBackgroundResource(R.drawable.border_rounded_red);
        }
        if (buySellBottomSheetFragment.isVisible()) {
            buySellBottomSheetFragment.updateUI(currency);
        }
    }

    private void onBuyButtonPressed() {
        if (!buySellBottomSheetFragment.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("currency", currency);
            bundle.putBoolean("isBuySheet", true);
            buySellBottomSheetFragment.setArguments(bundle);
            buySellBottomSheetFragment.show(
                    getSupportFragmentManager(),
                    buySellBottomSheetFragment.getTag()
            );
        }
    }

    private void onSellButtonPressed() {
        if (!buySellBottomSheetFragment.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("currency", currency);
            bundle.putBoolean("isBuySheet", false);
            buySellBottomSheetFragment.setArguments(bundle);
            buySellBottomSheetFragment.show(
                    getSupportFragmentManager(),
                    buySellBottomSheetFragment.getTag()
            );
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        if (currencyListener != null)
            currencyListener.remove();
        super.onDestroy();
    }
}
