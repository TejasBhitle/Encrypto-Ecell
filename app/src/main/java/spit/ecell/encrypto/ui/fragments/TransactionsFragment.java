package spit.ecell.encrypto.ui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
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

import spit.ecell.encrypto.R;
import spit.ecell.encrypto.models.Transaction;
import spit.ecell.encrypto.ui.activities.Constants;
import spit.ecell.encrypto.ui.adapters.TransactionAdapter;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionsFragment extends Fragment {

    private static final String TAG = "MarketFragment";
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    public TransactionsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_history, container, false);

        recyclerView = view.findViewById(R.id.transactions_recycler_view);
        progressBar = view.findViewById(R.id.progressBar);
        getTransactions();

        view.findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTransaction("BTC",20.0,100.0,true);
            }
        });

        return view;
    }


    public void updateUI(ArrayList<Transaction> transactions){
        progressBar.setVisibility(View.GONE);
        Log.d(TAG,"updating UI ....");
        if(transactions.size() != 0) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(new TransactionAdapter(transactions, getContext()));
        }
        else{
            Toast.makeText(getContext(),"No transactions",Toast.LENGTH_SHORT).show();
        }
    }

    public void getTransactions(){
        final ArrayList<Transaction> transactions = new ArrayList<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user==null){
            Log.d(TAG,"user is null");
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        db.collection(Constants.FIRESTORE_USERS_KEY)
                .document(user.getUid())
                .collection(Constants.FIRESTORE_TRANSACTIONS_KEY)
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

                    }
                });
    }

    public void createTransaction(String name, Double quantity, Double value, boolean isBought){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return;

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        details.put("currency-name",name);
        details.put("purchased-value",value);
        details.put("purchased-quantity",quantity);
        details.put("isBought",isBought);
        data.put("details",details);
        data.put("timestamp", Calendar.getInstance().getTime());


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.FIRESTORE_USERS_KEY)
                .document(user.getUid())
                .collection(Constants.FIRESTORE_TRANSACTIONS_KEY)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.e(TAG,"transaction added");
                        Toast.makeText(getActivity(), "Transaction created", Toast.LENGTH_SHORT).show();
                        getTransactions();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"transaction adding failed");
                        Toast.makeText(getActivity(), "Failed to create transaction", Toast.LENGTH_SHORT).show();
                    }
                });
    }


}
