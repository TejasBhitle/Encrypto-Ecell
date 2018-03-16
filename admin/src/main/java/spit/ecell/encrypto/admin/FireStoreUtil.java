package spit.ecell.encrypto.admin;

import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tejas on 16/3/18.
 */

public class FireStoreUtil {

    private static final String TAG = "FireStoreUtil";

    /*Collections*/
    private static final String STATIC_FIELDS = "staticFields";
    private static final String ROUNDS = "rounds";
    private static final String USERS = "users";

    /*Documents*/
    private static final String CONSTANTS = "constants";


    private FirebaseFirestore db;

    public FireStoreUtil(){
        db = FirebaseFirestore.getInstance();
    }

    /**Server constants*/
    public void update(String key, String value){
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

    /**Rounds*/
    public void createRound(int index, String name){
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

    public void changeRoundStatus(String roundKey, boolean isActive){
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

    /**static docs*/
    public void fetchStaticDocuments(){
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
    public void initializeWalletBalance(String UID){
        Map<String,Integer> data =  new HashMap<>();
        data.put("wallet-balance",1000);
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

    public FirebaseFirestore getDb() {
        return db;
    }

    public void setDb(FirebaseFirestore db) {
        this.db = db;
    }


}
