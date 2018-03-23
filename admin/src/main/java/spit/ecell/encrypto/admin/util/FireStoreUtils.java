package spit.ecell.encrypto.admin.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.Transaction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import spit.ecell.encrypto.admin.Constants;

/**
 * Created by tejas on 16/3/18.
 */

public class FireStoreUtils {

    private static final String TAG = "FireStoreUtils";

    /*Collections*/
    private static final String STATIC_FIELDS = "staticFields";
    private static final String ROUNDS = "rounds";
    private static final String USERS = "users";

    /*Documents*/
    private static final String CONSTANTS = "constants";


    /**Server constants*/
    public static void updateConstants(String key, String value) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Pair<String,String> pair = new Pair<>(key,value);
        db.collection(STATIC_FIELDS).document(CONSTANTS).set(pair)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
        });
    }

    /*
    public static void createRound(int index, String name){
        Map<String, Object> data = new HashMap<>();
        data.put("isActive", true);

        db.collection(ROUNDS).document("round"+index).set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void documentReference) {
                        Log.d(TAG, "Round created ");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error", e);
                    }
                });
    }

    public static void changeRoundStatus(String roundKey, boolean isActive){
        db.collection(ROUNDS).document(roundKey)
                .update("isActive",isActive)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void avoid) {
                        Log.d(TAG, "Round updated");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document ", e);
                    }
                });
    }
    */

    /**static docs*/
    public static void fetchStaticDocuments() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(STATIC_FIELDS).document(CONSTANTS)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document != null && document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                            } else {
                                Log.d(TAG, "No such document");
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                }
        });

    }

    /**User*/
    public static void initializeWalletBalance(String UID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String,Integer> data =  new HashMap<>();
        data.put("wallet-balance", 20000);
        db.collection(USERS).document(UID).set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }

    public static void update() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        // To be used when updating user portfolio
        final HashMap<String, Double> newPricesMap = new HashMap<>();

        /*calculate and change prices*/
        final CollectionReference currenciesRef = db.collection("currencies");
        currenciesRef.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (final DocumentSnapshot snapshot : task.getResult()) {
                            db.runTransaction(new Transaction.Function<Void>() {
                                @Nullable
                                @Override
                                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot documentSnapshot = transaction.get(currenciesRef.document(snapshot.getId()));
                                    Long changed = documentSnapshot.getLong("change-this-round");
                                    Double value = documentSnapshot.getDouble("value-now");
                                    Double vf = documentSnapshot.getDouble("variation-factor");
                                    Long circulation = documentSnapshot.getLong("circulation");

                                    ArrayList<Double> history = (ArrayList<Double>) documentSnapshot.get("history");

                                    Double newValue = value + value * changed * vf / circulation;
                                    Double variation = (newValue - value) / value * 100;

                                    newPricesMap.put(snapshot.getId(), newValue);
                                    history.add(newValue);

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("change-this-round", 0);
                                    map.put("value-now", newValue);
                                    map.put("variation", variation);
                                    map.put("history", history);

                                    transaction.set(currenciesRef.document(snapshot.getId()), map, SetOptions.merge());
                                    return null;
                                }
                            });
                        }
                        Log.d("valuation", "Updating total valuation");
                        updateTotalValuation(newPricesMap);
                    }
                });
    }

    public static void updateTotalValuation(final HashMap<String, Double> updatedCurrencyValueMap) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        final CollectionReference ref = db.collection(Constants.FS_USERS_KEY);
        ref.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (final DocumentSnapshot doc : task.getResult()) {
                            db.runTransaction(new Transaction.Function<Void>() {
                                @Nullable
                                @Override
                                public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                                    DocumentSnapshot snapshot = transaction.get(ref.document(doc.getId()));
                                    Double wallet_balance;
                                    try {
                                        wallet_balance = Double.parseDouble(snapshot.get(Constants.FS_USER_BALANCE_KEY).toString());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        wallet_balance = ((Long) snapshot.get(Constants.FS_USER_BALANCE_KEY)).doubleValue();
                                    }
                                    HashMap<String, Object> purchased_curr =
                                            (HashMap<String, Object>) snapshot.get(Constants.FS_PURCHASED_CURRENCIES_KEY);

                                    if (purchased_curr == null) {
                                        Log.d("valuation", "Purchased currencies is null when updating valuation");
                                        return null;
                                    }

                                    Double totalValuation = wallet_balance;
                                    for (String currencyId : purchased_curr.keySet()) {
                                        totalValuation +=
                                                (updatedCurrencyValueMap.get(currencyId) * (Long) purchased_curr.get(currencyId));
                                    }
                                    Log.d(TAG, "Total valuation: " + totalValuation);
                                    transaction.update(ref.document(doc.getId()), Constants.FS_TOTAL_VALUATION, totalValuation);
                                    return null;
                                }
                            });
                        }
                    }
                });

    }


}
