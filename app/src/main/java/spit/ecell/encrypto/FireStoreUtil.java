package spit.ecell.encrypto;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
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

import java.util.ArrayList;
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

        db.collection(Constants.FIRESTORE_CURRENCIES_KEY)
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

        CollectionReference collectionRef = db.collection(Constants.FIRESTORE_CURRENCIES_KEY);

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
        DocumentReference currencyRef = db.collection(Constants.FIRESTORE_CURRENCIES_KEY).document(id);

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
}


