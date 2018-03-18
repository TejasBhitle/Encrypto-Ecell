package spit.ecell.encrypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spit.ecell.encrypto.models.Currency;

/**
 * Created by tejas on 18/3/18.
 */

public class FireStoreUtil {

    private static final String TAG = "FireStoreUtil";
    private Context context;

    public FireStoreUtil(Context context) {
        this.context = context;
    }

    public interface FireStoreUtilCallbacks {
        void onSuccess(Object object);

        void onFailure(Object object);
    }

    public void getCurrencies(final FireStoreUtilCallbacks callbacks) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "user is null");
            return;
        }

        db.collection(Constants.FS_CURRENCIES_KEY)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "Success");
                            final ArrayList<Currency> currencies = new ArrayList<>();
                            final SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
                            for (DocumentSnapshot document : task.getResult()) {
                                Map<String, Object> object = document.getData();
                                Log.e(TAG, object.toString());
                                String id = document.getId();
                                String symbol = (String) object.get("symbol");
                                String desc = (String) object.get("desc");
                                String name = (String) object.get("name");
                                Double value = Double.parseDouble(object.get("value-now").toString());
                                Double variation = Double.parseDouble(object.get("variation").toString());
                                Double circulation = Double.parseDouble(object.get("circulation").toString());
                                Double factor = Double.parseDouble(object.get("variation-factor").toString());
                                Integer owned = prefs.getInt("OWNED_" + symbol, 0);
                                currencies.add(new Currency(id, symbol, name, desc, value, variation, owned, factor, circulation));
                            }
                            if (callbacks != null)
                                callbacks.onSuccess(currencies);
                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callbacks != null)
                            callbacks.onFailure(null);
                    }
                });
    }

    public ListenerRegistration getCurrenciesRealTime(final FireStoreUtilCallbacks callbacks) {

        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "user is null");
            return null;
        }

        CollectionReference collectionRef = db.collection(Constants.FS_CURRENCIES_KEY);

        return collectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot snapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w(TAG, "Listen failed.", e);
                    callbacks.onFailure(null);
                    return;
                }

                final ArrayList<Currency> currencies = new ArrayList<>();
                final SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);

                for (DocumentSnapshot document : snapshot) {
                    Map<String, Object> object = document.getData();
                    Log.e(TAG, object.toString());
                    String id = document.getId();
                    String symbol = (String) object.get("symbol");
                    String desc = (String) object.get("desc");
                    String name = (String) object.get("name");
                    Double value = Double.parseDouble(object.get("value-now").toString());
                    Double variation = Double.parseDouble(object.get("variation").toString());
                    Double circulation = Double.parseDouble(object.get("circulation").toString());
                    Double factor = Double.parseDouble(object.get("variation-factor").toString());
                    Integer owned = prefs.getInt("OWNED_" + symbol, 0);
                    currencies.add(new Currency(id, symbol, name, desc, value, variation, owned, factor, circulation));
                }
                if (callbacks != null)
                    callbacks.onSuccess(currencies);
            }
        });
    }

    public ListenerRegistration getCurrencyRealTimeById(String id,
                                                        final FireStoreUtilCallbacks callbacks){

        final SharedPreferences prefs = context.getSharedPreferences(Constants.PREFS, Context.MODE_PRIVATE);
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currencyRef = db.collection(Constants.FS_CURRENCIES_KEY).document(id);

        return currencyRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot document, FirebaseFirestoreException e) {
                String id = document.getId();
                Map<String, Object> object = document.getData();
                String symbol = (String) object.get("symbol");
                String desc = (String) object.get("desc");
                String name = (String) object.get("name");
                Double value = Double.parseDouble(object.get("value-now").toString());
                Double variation = Double.parseDouble(object.get("variation").toString());
                Double circulation = Double.parseDouble(object.get("circulation").toString());
                Double factor = Double.parseDouble(object.get("variation-factor").toString());
                Integer owned = prefs.getInt("OWNED_" + symbol, 0);
                Currency currency = new Currency(id, symbol, name, desc, value, variation, owned, factor, circulation);
                if(callbacks != null){
                    callbacks.onSuccess(currency);
                }
            }
        });
    }

    public void buyCurrency(final Currency currency, final double quantity){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return;
        String userId = user.getUid();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        /*change balance*/
        final DocumentReference userRef = db.collection(Constants.FS_USERS_KEY)
                .document(userId);
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(userRef);
                double balance = snapshot.getDouble(Constants.FS_USER_BALANCE_KEY);
                balance -= quantity*currency.getCurrentValue();
                transaction.update(userRef, Constants.FS_USER_BALANCE_KEY, balance);

                return null;
            }
        });

        /*purchase currency*/
        final DocumentReference purchased_currencies =
                userRef.collection(Constants.FS_PURCHASED_CURRENCIES_KEY)
                        .document(currency.getId());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot snapshot = transaction.get(purchased_currencies);
                if(snapshot.exists()){
                    double newQuantity = snapshot.getDouble("quantity");
                    newQuantity += quantity;
                    transaction.update(purchased_currencies,"quantity",newQuantity);
                }else{
                    Map<String,Object> map =  new HashMap<>();
                    map.put("quantity",0);
                    /*quantity = 0 because on creating this the upper transaction runs again*/
                    purchased_currencies.set(map);
                }
                return null;
            }
        });

        /*update transaction node*/
        createTransaction(currency.getName(),quantity,currency.getCurrentValue(),true);

    }

    private void createTransaction(String currency_name,Double quantity, Double value, boolean isBought){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null) return;

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        details.put("currency-name",currency_name);
        details.put("purchased-value",value);
        details.put("purchased-quantity",quantity);
        details.put("isBought",isBought);
        data.put("details",details);
        data.put("timestamp", Calendar.getInstance().getTime());


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.FS_USERS_KEY)
                .document(user.getUid())
                .collection(Constants.FS_TRANSACTIONS_KEY)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.e(TAG,"transaction added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG,"transaction adding failed");
                    }
                });
    }

    public void getTransactions(final FireStoreUtilCallbacks callbacks){
        final ArrayList<spit.ecell.encrypto.models.Transaction> transactions = new ArrayList<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user==null){
            Log.d(TAG,"user is null");
            return;
        }
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
                                transactions.add(new spit.ecell.encrypto.models.Transaction(name,value,quantity,isBought,timestamp));

                            }
                            callbacks.onSuccess(transactions);

                        } else {
                            Log.e(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        callbacks.onFailure(null);
                    }
                });
    }
}


