package spit.ecell.encrypto.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSeekBar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;

public class CurrencyDetailActivity extends AppCompatActivity {
    Currency currency;
    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_detail);

        preferences = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

        TextView descriptionView = findViewById(R.id.description);
        TextView symbol = findViewById(R.id.symbol);
        TextView variation = findViewById(R.id.variation);
        TextView value = findViewById(R.id.value);
        Button buyButton = findViewById(R.id.buyButton);
        Button sellButton = findViewById(R.id.sellButton);

        Bundle extras = getIntent().getExtras();

        if (extras == null || !extras.containsKey(Constants.FIRESTORE_CURRENCIES_KEY)) {
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currency = extras.getParcelable(Constants.FIRESTORE_CURRENCIES_KEY);
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

        buyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBuyButtonPressed();
            }
        });

        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSellButtonPressed();
            }
        });

    }

    private void onBuyButtonPressed(){
        final BottomSheetDialog bottomSheetDialog =
                new BottomSheetDialog(CurrencyDetailActivity.this, R.style.BottomSheet_Light);
        bottomSheetDialog.setContentView(R.layout.bottom_sheet_buy);

        TextView header = bottomSheetDialog.findViewById(R.id.buy_header);
        TextView valueText = bottomSheetDialog.findViewById(R.id.value);
        final TextView costText = bottomSheetDialog.findViewById(R.id.cost);
        final TextView balanceText = bottomSheetDialog.findViewById(R.id.balance);
        final AppCompatSeekBar seekBar = bottomSheetDialog.findViewById(R.id.seekbar);

        final double balance = preferences.getFloat(Constants.FIRESTORE_USER_BALANCE_KEY, 0);
        final double value = currency.getCurrentValue();

        // TODO: Should probably fetch values from server
        header.append(" " + currency.getSymbol());
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

        bottomSheetDialog.findViewById(R.id.confirm_purchase).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (seekBar.getProgress() > 0) {
                    // TODO: Buy request
                    Toast.makeText(CurrencyDetailActivity.this,
                            "Purchase confirmed for " + seekBar.getProgress() + " " + currency.getSymbol() +
                                    " for " + costText.getText(), Toast.LENGTH_SHORT).show();
                }
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    private void onSellButtonPressed(){}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
