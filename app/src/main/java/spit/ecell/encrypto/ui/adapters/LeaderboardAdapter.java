package spit.ecell.encrypto.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;

import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Score;

/**
 * Created by Samriddha on 22-03-2018.
 */

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.ViewHolder> {
    private ArrayList<Score> scores;
    private Context context;

    public LeaderboardAdapter(ArrayList<Score> scores, Context context) {
        this.scores = scores;
        this.context = context;
    }

    @NonNull
    @Override
    public LeaderboardAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new LeaderboardAdapter.ViewHolder(LayoutInflater.from(context).
                inflate(R.layout.list_item_leaderboard, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Score data = scores.get(position);
        DecimalFormat formatter = new DecimalFormat("0.00");

        holder.positionView.setText((position + 1) + ".");

        String name = data.getUsername();
        holder.nameTextView.setText(name);

        String value = formatter.format(data.getScore());
        holder.valueView.setText(value);
    }

    @Override
    public int getItemCount() {
        return scores.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView positionView, nameTextView, valueView;

        ViewHolder(View view) {
            super(view);
            positionView = view.findViewById(R.id.position);
            nameTextView = view.findViewById(R.id.name);
            valueView = view.findViewById(R.id.value);
        }
    }
}
