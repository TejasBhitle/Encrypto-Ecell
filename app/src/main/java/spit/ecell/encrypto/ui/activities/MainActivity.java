package spit.ecell.encrypto.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import spit.ecell.encrypto.R;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String USERS = "users";

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences(Constants.PREFS,MODE_PRIVATE);
        if(prefs.getBoolean(Constants.IS_FIRST_LAUNCH,true)){
            initializeUser();
            prefs.edit().putBoolean(Constants.IS_FIRST_LAUNCH,false).apply();
        }

    }

    public void initializeUser(){
        if(currentUser == null){
            Log.e(TAG,"current user is null");
            return;
        }
        String UID = currentUser.getUid();
        String name = prefs.getString(Constants.USER_NAME,"");

        Map<String,Object> data =  new HashMap<>();
        data.put("wallet-balance",1000);
        data.put("name",name);
        db.collection(USERS).document(UID).set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG,"Success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"Failure");
                    }
                });
    }
}
