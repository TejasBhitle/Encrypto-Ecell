package spit.ecell.encrypto.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Transaction;
import spit.ecell.encrypto.ui.adapters.TransactionAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionsFragment extends Fragment {
    private static final String TAG = "TransactionFragment";
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView blankText;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.transactions_recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        blankText = view.findViewById(R.id.blankText);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getTransactions();
            }
        });

        getTransactions();

        return view;
    }


    public void updateUI(ArrayList<Transaction> transactions){
        Log.d(TAG,"updating UI ....");
        if(transactions.size() != 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new TransactionAdapter(transactions, getContext()));
            blankText.setVisibility(View.GONE);
        } else {
            blankText.setVisibility(View.VISIBLE);
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    public void getTransactions(){
        final ArrayList<Transaction> transactions = new ArrayList<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user==null){
            Log.d(TAG,"user is null");
            return;
        }
        swipeRefreshLayout.setRefreshing(true);
        db.collection(Constants.FS_USERS_KEY)
                .document(user.getUid())
                .collection(Constants.FS_TRANSACTIONS_KEY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG,"Success");
                            for (DocumentSnapshot document : task.getResult()) {
                                Map<String,Object> object = document.getData();
                                Log.e(TAG,object.toString());
                                Map<String, Object> details = (HashMap<String,Object>)object.get("details");
                                String name = details.get("currency-name").toString();
                                Double quantity = Double.parseDouble(details.get("purchased-quantity").toString());
                                Double value = Double.parseDouble(details.get("purchased-value").toString());
                                boolean isBought = Boolean.parseBoolean( details.get("isBought").toString());
                                Date timestamp = (Date)object.get("timestamp");
                                transactions.add(new Transaction(name,value,quantity,isBought,timestamp));

                            }
                            updateUI(transactions);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getActivity(), "Failed to get transaction history", Toast.LENGTH_SHORT).show();
                    }
                });
    }




}
