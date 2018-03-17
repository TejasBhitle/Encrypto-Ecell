package spit.ecell.encrypto.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;
import spit.ecell.encrypto.ui.fragments.MarketFragment;
import spit.ecell.encrypto.ui.fragments.ProfileFragment;
import spit.ecell.encrypto.ui.fragments.TransactionsFragment;

public class MainActivity extends AppCompatActivity implements ProfileFragment.OnProfileFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    BottomNavigationView bottomNavigationView;
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
            return;
        }
        db = FirebaseFirestore.getInstance();

        prefs = getSharedPreferences(Constants.PREFS,MODE_PRIVATE);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        /*
        if(prefs.getBoolean(Constants.IS_FIRST_LAUNCH,true)) {
            // Do something on first launch
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(Constants.IS_FIRST_LAUNCH, false).apply();
        }
        */
        final FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction()
                .replace(R.id.frameLayout, new MarketFragment())
                .commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.bottom_nav_home:
                        setTitle(R.string.app_name);
                        fm.beginTransaction()
                                .replace(R.id.frameLayout, new MarketFragment())
                                .commit();
                        break;
                    case R.id.bottom_nav_history:
                        setTitle(R.string.transaction_history);
                        fm.beginTransaction()
                                .replace(R.id.frameLayout, new TransactionsFragment())
                                .commit();
                        break;
                    case R.id.bottom_nav_profile:
                        setTitle(R.string.profile);
                        fm.beginTransaction()
                                .replace(R.id.frameLayout, new ProfileFragment())
                                .commit();
                        break;
                }
                return true;
            }
        });

    }

    @Override
    public void onLogout() {
        prefs.edit().clear().apply();
        mAuth.signOut();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

}
