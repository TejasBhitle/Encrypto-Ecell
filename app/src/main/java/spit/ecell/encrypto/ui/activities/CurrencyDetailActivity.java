package spit.ecell.encrypto.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.ListenerRegistration;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.view.LineChartView;
import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;
import spit.ecell.encrypto.ui.fragments.BuySellBottomSheetFragment;
import spit.ecell.encrypto.util.FireStoreUtils;
import spit.ecell.encrypto.util.NetworkUtils;

public class CurrencyDetailActivity extends AppCompatActivity {
    private Currency currency;
    private ListenerRegistration currencyListener, ownedCurrencyQuantityListener, historyListener;
    private int ownedCurrencyQuantity = 0;
    private DecimalFormat formatter;

    private TextView descriptionView, symbol, variation, value, owned;
    private LineChartView lineChartView;

    private BuySellBottomSheetFragment buySellBottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_detail);

        formatter = new DecimalFormat(".00#");

        descriptionView = findViewById(R.id.description);
        symbol = findViewById(R.id.symbol);
        variation = findViewById(R.id.variation);
        value = findViewById(R.id.value);
        owned = findViewById(R.id.owned);
        lineChartView = findViewById(R.id.chart);
        lineChartView.setZoomEnabled(false);

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
                        updateUI((Currency) object, ownedCurrencyQuantity);
                    }

                    @Override
                    public void onFailure(Object object) {

                    }
                }
        );

        ownedCurrencyQuantityListener = FireStoreUtils.
                getOwnedCurrencyQuantityRealtime(currency.getId(),
                        new FireStoreUtils.FireStoreUtilCallbacks() {
            @Override
            public void onSuccess(Object object) {
                ownedCurrencyQuantity = Integer.parseInt(object.toString());
                updateUI(currency, ownedCurrencyQuantity);
            }

            @Override
            public void onFailure(Object object) {

            }
        });

        historyListener = FireStoreUtils.getCurrencyValueHistoryRealtime(
                currency.getId(), new FireStoreUtils.FireStoreUtilCallbacks() {
            @Override
            public void onSuccess(Object object) {
                ArrayList historyValues = (ArrayList) object;
                List<PointValue> values = new ArrayList<>();
                for (int i = 0; i < historyValues.size(); i++) {
                    values.add(new PointValue(i, Float.valueOf(historyValues.get(i).toString())));
                }

                Line line = new Line(values).setColor(getResources().getColor(R.color.colorPrimary))
                        .setFilled(true)
                        .setHasLabelsOnlyForSelected(true);
                List<Line> lines = new ArrayList<>();
                lines.add(line);

                LineChartData data = new LineChartData();
                data.setLines(lines);

                lineChartView.setLineChartData(data);
            }

            @Override
            public void onFailure(Object object) {

            }
        });
    }

    private void updateUI(Currency currency, int ownedCurrencyQuantity) {
        setTitle(currency.getName());
        descriptionView.setText(currency.getDesc());
        symbol.setText(currency.getSymbol());
        value.setText(formatter.format(currency.getCurrentValue()));
        owned.setText(String.valueOf(ownedCurrencyQuantity));
        if (currency.getVariation() >= 0) {
            variation.setText("+" + formatter.format(currency.getVariation()) + "%");
        } else {
            variation.setText(formatter.format(currency.getVariation()) + "%");
            variation.setBackgroundResource(R.drawable.border_rounded_red);
        }
        if (buySellBottomSheetFragment.isVisible()) {
            buySellBottomSheetFragment.updateUI(currency, ownedCurrencyQuantity);
        }
    }

    private void onBuyButtonPressed() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!buySellBottomSheetFragment.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("currency", currency);
            bundle.putInt("owned", ownedCurrencyQuantity);
            bundle.putBoolean("isBuySheet", true);
            buySellBottomSheetFragment.setArguments(bundle);
            buySellBottomSheetFragment.show(
                    getSupportFragmentManager(),
                    buySellBottomSheetFragment.getTag()
            );
        }
    }

    private void onSellButtonPressed() {
        if (!NetworkUtils.isNetworkConnected(this)) {
            Toast.makeText(this, "Please check your internet connection", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!buySellBottomSheetFragment.isAdded()) {
            Bundle bundle = new Bundle();
            bundle.putParcelable("currency", currency);
            bundle.putInt("owned", ownedCurrencyQuantity);
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
        if (ownedCurrencyQuantityListener != null)
            ownedCurrencyQuantityListener.remove();
        if (historyListener != null)
            historyListener.remove();
        super.onDestroy();
    }
}
