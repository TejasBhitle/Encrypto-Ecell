package spit.ecell.encrypto.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Transaction;

/**
 * Created by tejas on 17/3/18.
 */

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {
    private ArrayList<Transaction> transactions;
    private Context context;

    public TransactionAdapter(ArrayList<Transaction> transactions, Context context) {
        this.transactions = transactions;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).
                inflate(R.layout.list_item_transaction,parent,false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction data = transactions.get(position);
        String text = (data.isBought() ? "Bought " : "Sold ")
                +(int)data.getQuantity()+" "
                + data.getName() + " for $" + data.getValue() * data.getQuantity();
        holder.detailsTextView.setText(text);

        SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm:ss a");
        String time = localDateFormat.format(data.getTimeStamp());
        holder.timestampTextView.setText(time);
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView detailsTextView, timestampTextView;

        ViewHolder(View view) {
            super(view);
            timestampTextView = view.findViewById(R.id.timestampTextView);
            detailsTextView = view.findViewById(R.id.detailsTextView);
        }
    }
}
