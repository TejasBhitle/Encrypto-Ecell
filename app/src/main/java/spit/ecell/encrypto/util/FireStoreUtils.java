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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import spit.ecell.encrypto.models.Currency;

import static spit.ecell.encrypto.Constants.FS_CURRENCIES_KEY;
import static spit.ecell.encrypto.Constants.FS_PURCHASED_CURRENCIES_KEY;
import static spit.ecell.encrypto.Constants.FS_QUANTITY_KEY;
import static spit.ecell.encrypto.Constants.FS_TIMESTAMP;
import static spit.ecell.encrypto.Constants.FS_TRANSACTIONS_KEY;
import static spit.ecell.encrypto.Constants.FS_USERS_KEY;
import static spit.ecell.encrypto.Constants.FS_USER_BALANCE_KEY;

/**
 * Created by tejas on 18/3/18.
 */

public class FireStoreUtils {

    private static final String TAG = "FireStoreUtils";

    public static ListenerRegistration getBalance(final FireStoreUtilCallbacks callbacks) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callbacks.onFailure("User is null");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference userRef = db.collection(FS_USERS_KEY).document(user.getUid());
        return userRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getData();

                /*
                 If the decimal places are .00 then firebase stores them as long
                 otherwise its double
                */
                try {
                    Double balance = (Double) map.get(FS_USER_BALANCE_KEY);
                    callbacks.onSuccess(balance);
                } catch (ClassCastException exp) {
                    Double balance = ((Long) map.get(FS_USER_BALANCE_KEY)).doubleValue();
                    callbacks.onSuccess(balance);
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

        db.collection(FS_CURRENCIES_KEY)
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

        CollectionReference collectionRef = db.collection(FS_CURRENCIES_KEY);

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
        DocumentReference currencyRef = db.collection(FS_CURRENCIES_KEY).document(id);

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

    public static void buySellCurrency(final Currency currency, double qty, boolean isBuy) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String userId = user.getUid();

        final double quantity = (isBuy) ? qty : -qty;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        /*change balance*/
        final DocumentReference userRef = db.collection(FS_USERS_KEY)
                .document(userId);
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(userRef);
                double balance = snapshot.getDouble(FS_USER_BALANCE_KEY);
                balance -= quantity * currency.getCurrentValue();
                transaction.update(userRef, FS_USER_BALANCE_KEY, balance);
                return null;
            }
        });

        /*purchase currency*/
        final DocumentReference purchased_currencies =
                userRef.collection(FS_PURCHASED_CURRENCIES_KEY)
                        .document(currency.getId());
        db.runTransaction(new Transaction.Function<Void>() {
            @Nullable
            @Override
            public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {

                DocumentSnapshot snapshot = transaction.get(purchased_currencies);
                if (snapshot.exists()) {
                    double newQuantity = snapshot.getDouble("quantity");
                    newQuantity += quantity;
                    transaction.update(purchased_currencies, "quantity", newQuantity);
                } else {
                    Map<String, Object> map = new HashMap<>();
                    map.put("quantity", 0);
                    /*quantity = 0 because on creating this the upper transaction runs again*/
                    purchased_currencies.set(map);
                }
                return null;
            }
        });

        /*update currency change-this-round values*/
        final DocumentReference currencyRef =
                db.collection(FS_CURRENCIES_KEY)
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

    private static void createTransaction(String currency_name, Double quantity, Double value, boolean isBought) {
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
        db.collection(FS_USERS_KEY)
                .document(user.getUid())
                .collection(FS_TRANSACTIONS_KEY)
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
        db.collection(FS_USERS_KEY)
                .document(user.getUid())
                .collection(FS_TRANSACTIONS_KEY).orderBy(FS_TIMESTAMP)
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

    public static void getOwnedCurrencyQuantity(String currencyId, final FireStoreUtilCallbacks callbacks) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            Log.d(TAG, "user is null");
            return;
        }

        db.collection(FS_USERS_KEY).document(user.getUid())
                .collection(FS_PURCHASED_CURRENCIES_KEY).document(currencyId)
                .get()
                .addOnCompleteListener(
                        new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                double quantity = 0;
                                if (task.isSuccessful()) {
                                    Log.e(TAG, "Success");
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Object object = document.get(FS_QUANTITY_KEY);
                                        if (object != null) {
                                            quantity = Double.parseDouble(object.toString());
                                        }
                                    } else {
                                        quantity = 0;
                                    }
                                } else {
                                    Log.e(TAG, "Error getting documents: ", task.getException());
                                }
                                if (callbacks != null)
                                    callbacks.onSuccess(quantity);
                            }
                        }
                )
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (callbacks != null)
                            callbacks.onFailure(null);
                    }
                });
    }

    public static ListenerRegistration getOwnedCurrencyQuantityRealtime(String currencyId, final FireStoreUtilCallbacks callbacks) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            callbacks.onFailure("User is null");
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference ref = db.collection(FS_USERS_KEY).document(user.getUid())
                .collection(FS_PURCHASED_CURRENCIES_KEY).document(currencyId);
        return ref.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot snapshot, FirebaseFirestoreException e) {
                if (snapshot.exists()) {
                    HashMap<String, Object> map = (HashMap<String, Object>) snapshot.getData();
                /*
                 If the decimal places are .00 then firebase stores them as long
                 otherwise its double
                */
                    try {
                        int owned = (int) Double.parseDouble(map.get(FS_QUANTITY_KEY).toString());
                        callbacks.onSuccess(owned);
                    } catch (ClassCastException exp) {
                        int owned = ((Long) map.get(FS_QUANTITY_KEY)).intValue();
                        callbacks.onSuccess(owned);
                    }
                } else {
                    callbacks.onSuccess(0);
                }
            }
        });
    }

    public interface FireStoreUtilCallbacks {
        void onSuccess(Object object);

        void onFailure(Object object);
    }

}


