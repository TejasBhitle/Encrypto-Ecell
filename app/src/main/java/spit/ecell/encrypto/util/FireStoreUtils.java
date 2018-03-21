package spit.ecell.encrypto.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.models.Currency;
import spit.ecell.encrypto.models.Score;

/**
 * Created by tejas on 18/3/18.
 */

public class FireStoreUtils {

    private static final String TAG = "FireStoreUtils";
    public static HashMap<String, String> currencyIdNameMap = new HashMap<>();

    // hack to quickly map currency names to IDs
    static {
        currencyIdNameMap.put("Bitcoin", "1DmDHDXE1M1fs2Kgqrqp");
        currencyIdNameMap.put("Ethereum", "2D5eV6BoISuPFLTccqMQ");
        currencyIdNameMap.put("Monero", "FlDaa4zETagXkKypq9qL");
        currencyIdNameMap.put("Dash", "J8PG9Bb78Hp5p1JV5L6h");
        currencyIdNameMap.put("e-Sikka", "TxOzglqZwR0vWNtfGknt");
        currencyIdNameMap.put("Litecoin", "ziWcZm6xPTV0xDMz7dFH");
    }

    public static ListenerRegistration getBalance(final FireStoreUtilCallbacks callbacks) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callbacks.onFailure("User is null");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection(Constants.FS_USERS_KEY).document(user.getUid());
        return userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getData();

                /*
                 If the decimal places are .00 then firebase stores them as long
                 otherwise its double
                */
                try {
                    Double balance = (Double) map.get(Constants.FS_USER_BALANCE_KEY);
                    callbacks.onSuccess(balance);
                } catch (ClassCastException exp) {
                    Double balance = ((Long) map.get(Constants.FS_USER_BALANCE_KEY)).doubleValue();
                    callbacks.onSuccess(balance);
                }
            }
        });
    }

    public static ListenerRegistration getTotalValuation(final FireStoreUtilCallbacks callbacks) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callbacks.onFailure("User is null");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection(Constants.FS_USERS_KEY).document(user.getUid());
        return userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getData();

                /*
                 If the decimal places are .00 then firebase stores them as long
                 otherwise its double
                */
                try {
                    Double valuation = (Double) map.get(Constants.FS_TOTAL_VALUATION);
                    callbacks.onSuccess(valuation);
                } catch (ClassCastException exp) {
                    Double valuation = ((Long) map.get(Constants.FS_TOTAL_VALUATION)).doubleValue();
                    callbacks.onSuccess(valuation);
                }
            }
        });
    }

    public static void getCurrencies(final FireStoreUtilCallbacks callbacks) {

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
                                currencies.add(new Currency(id, symbol, name, desc, value, variation, factor, circulation));
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

    public static ListenerRegistration getCurrenciesRealTime(final FireStoreUtilCallbacks callbacks) {

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

                for (DocumentSnapshot document : snapshot) {
                    try {
                        Map<String, Object> object = document.getData();
                        Log.d(TAG, object.toString());
                        String id = document.getId();
                        String symbol = (String) object.get("symbol");
                        String desc = (String) object.get("desc");
                        String name = (String) object.get("name");
                        Double value = Double.parseDouble(object.get("value-now").toString());
                        Double variation = Double.parseDouble(object.get("variation").toString());
                        Double circulation = Double.parseDouble(object.get("circulation").toString());
                        Double factor = Double.parseDouble(object.get("variation-factor").toString());
                        currencies.add(new Currency(id, symbol, name, desc, value, variation, factor, circulation));
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }
                }
                if (callbacks != null)
                    callbacks.onSuccess(currencies);

            }
        });
    }

    public static ListenerRegistration getCurrencyRealTimeById(String id,
                                                               final FireStoreUtilCallbacks callbacks) {
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
                Currency currency = new Currency(id, symbol, name, desc, value, variation, factor, circulation);
                if (callbacks != null) {
                    callbacks.onSuccess(currency);
                }
            }
        });
    }

    public static void buySellCurrency(final Currency currency, long qty, boolean isBuy) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        final long quantity = (isBuy) ? qty : -qty;

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
                balance -= quantity * currency.getCurrentValue();
                transaction.update(userRef, Constants.FS_USER_BALANCE_KEY, balance);
                return null;
            }
        });


        /*purchase currency*/
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(userRef);
                HashMap<String, Object> purchased_currencies =
                        (HashMap<String, Object>) snapshot.get(Constants.FS_PURCHASED_CURRENCIES_KEY);

                if (purchased_currencies == null) {
                    purchased_currencies = new HashMap<>();
                    purchased_currencies.put(currency.getId(), quantity);
                } else {
                    Long newQuantity = (Long) (purchased_currencies.get(currency.getId()));
                    if (newQuantity == null) {
                        purchased_currencies.put(currency.getId(), quantity);
                    } else {
                        newQuantity += quantity;
                        purchased_currencies.put(currency.getId(), newQuantity);
                    }
                }
                transaction.update(userRef, Constants.FS_PURCHASED_CURRENCIES_KEY, purchased_currencies);
                return null;
            }
        });

        /*update currency change-this-round values*/
        final DocumentReference currencyRef =
                db.collection(Constants.FS_CURRENCIES_KEY)
                        .document(currency.getId());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot snapshot = transaction.get(currencyRef);
                long changed = snapshot.getLong("change-this-round");
                changed += quantity;
                transaction.update(currencyRef, "change-this-round", changed);
                return null;
            }
        });

        /*update transaction node*/
        createTransaction(currency.getName(), quantity, currency.getCurrentValue(), isBuy);

    }

    private static void createTransaction(String currency_name, Long quantity, Double value, boolean isBought) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        Map<String, Object> details = new HashMap<>();
        details.put("currency-name", currency_name);
        details.put("purchased-value", value);
        details.put("purchased-quantity", quantity);
        details.put("isBought", isBought);
        data.put("details", details);
        data.put("timestamp", Calendar.getInstance().getTime());


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(Constants.FS_USERS_KEY)
                .document(user.getUid())
                .collection(Constants.FS_TRANSACTIONS_KEY)
                .add(data)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.e(TAG, "transaction added");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "transaction adding failed");
                    }
                });
    }

    public static void getTransactions(final FireStoreUtilCallbacks callbacks) {
        final ArrayList<spit.ecell.encrypto.models.Transaction> transactions = new ArrayList<>();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "user is null");
            return;
        }
        db.collection(Constants.FS_USERS_KEY)
                .document(user.getUid())
                .collection(Constants.FS_TRANSACTIONS_KEY).orderBy(Constants.FS_TIMESTAMP)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            Log.e(TAG, "Success");
                            for (DocumentSnapshot document : task.getResult()) {
                                Map<String, Object> object = document.getData();
                                Log.e(TAG, object.toString());
                                Map<String, Object> details = (HashMap<String, Object>) object.get("details");
                                String name = details.get("currency-name").toString();
                                Double quantity = Double.parseDouble(details.get("purchased-quantity").toString());
                                Double value = Double.parseDouble(details.get("purchased-value").toString());
                                boolean isBought = Boolean.parseBoolean(details.get("isBought").toString());
                                Date timestamp = (Date) object.get("timestamp");
                                transactions.add(new spit.ecell.encrypto.models.Transaction(name, value, quantity, isBought, timestamp));

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

    public static void getOwnedCurrencies(final FireStoreUtilCallbacks callbacks) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "user is null");
            return;
        }

        db.collection(Constants.FS_USERS_KEY).document(user.getUid())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        HashMap<String, Object> map = (HashMap<String, Object>) documentSnapshot.getData();
                        HashMap<String, Object> purchased_currencies =
                                (HashMap<String, Object>) map.get(Constants.FS_PURCHASED_CURRENCIES_KEY);
                        if (callbacks != null && purchased_currencies != null) {
                            callbacks.onSuccess(purchased_currencies);
                        }
                    }
                });
    }

    public static ListenerRegistration getOwnedCurrencyQuantityRealtime(final String currencyId, final FireStoreUtilCallbacks callbacks) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callbacks.onFailure("User is null");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference ref = db.collection(Constants.FS_USERS_KEY).document(user.getUid());
        return ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                int owned = 0;
                if (snapshot.exists()) {
                    HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getData();
                    HashMap<String, Object> purchased_currencies =
                            (HashMap<String, Object>) map.get(Constants.FS_PURCHASED_CURRENCIES_KEY);
                    if (purchased_currencies.containsKey(currencyId)) {
                        try {
                            owned = (int) Double.parseDouble(purchased_currencies.get(currencyId).toString());

                            callbacks.onSuccess(owned);
                        } catch (ClassCastException exp) {
                            owned = ((Long) purchased_currencies.get(currencyId)).intValue();
                            callbacks.onSuccess(owned);
                        }
                    } else {
                        callbacks.onSuccess(0);
                    }
                }
                Log.e(TAG, "Owned ->" + owned);
            }
        });

    }

    public static ListenerRegistration getCurrencyValueHistoryRealtime(String id, @NonNull final FireStoreUtilCallbacks callbacks) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference ref = db.collection(Constants.FS_CURRENCIES_KEY).document(id);
        return ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                ArrayList<Float> historyValues = (ArrayList<Float>) snapshot.get("history");
                callbacks.onSuccess(historyValues);
            }
        });
    }

    public static void getLeaderboard(final FireStoreUtilCallbacks callbacks) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a reference to the users collection
        CollectionReference citiesRef = db.collection(Constants.FS_USERS_KEY);

        // Create a query against the collection.
        Query query = citiesRef.orderBy(Constants.FS_TOTAL_VALUATION, Query.Direction.DESCENDING).limit(11);
        query.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                ArrayList<Score> scores = new ArrayList<>();
                for (DocumentSnapshot doc : documentSnapshots.getDocuments()) {
                    // Blacklist admin from leaderboard and show max 10 entries
                    if (!doc.getId().equals("tNHDmIm2DBOS0k2B38HzyED7tRD2") && scores.size() != 10)
                        scores.add(new Score(doc.getString(Constants.FS_USER_NAME_KEY),
                                Double.parseDouble(doc.get(Constants.FS_TOTAL_VALUATION).toString())));
                }
                if (callbacks != null) {
                    callbacks.onSuccess(scores);
                }
            }
        });
    }

    public interface FireStoreUtilCallbacks {
        void onSuccess(Object object);

        void onFailure(Object object);
    }

}


