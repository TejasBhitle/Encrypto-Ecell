package spit.ecell.encrypto.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Currency;

public class CurrencyDetailActivity extends AppCompatActivity {
    Currency currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_currency_detail);

        TextView descriptionView = findViewById(R.id.description);
        TextView symbol = findViewById(R.id.symbol);
        TextView variation = findViewById(R.id.variation);
        TextView value = findViewById(R.id.value);

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
    }
}
