package spit.ecell.encrypto.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.transition.ChangeBounds;
import android.transition.Scene;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.HashMap;
import java.util.Map;

import spit.ecell.encrypto.Constants;
import spit.ecell.encrypto.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final String USERS = "users";

    Scene loginScene;
    Scene registerScene;
    Transition transition = new ChangeBounds().setDuration(750);
    TextInputEditText emailView, passwordView, nameView, confirmPasswordView;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        ViewGroup root = findViewById(R.id.rootView);
        Button registerButton = findViewById(R.id.registerButton);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.please_wait));

        loginScene = Scene.getSceneForLayout(root, R.layout.card_login, this);
        registerScene = Scene.getSceneForLayout(root, R.layout.card_register, this);

        prefs = getSharedPreferences(Constants.PREFS, MODE_PRIVATE);

    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        // Check if no view has focus:
        View view = LoginActivity.this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (currentUser != null) {
                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    TransitionManager.go(loginScene, transition);
                    initLogin();
                }
            }
        }, 1000);
    }


    public void initLogin() {
        emailView = findViewById(R.id.input_email);
        passwordView = findViewById(R.id.input_password);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if no view has focus:
                View view = LoginActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                TransitionManager.go(registerScene, transition);
                initRegister();
            }
        });
    }

    public void initRegister() {
        nameView = findViewById(R.id.input_name);
        emailView = findViewById(R.id.input_email);
        passwordView = findViewById(R.id.input_password);
        confirmPasswordView = findViewById(R.id.confirm_password);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if no view has focus:
                View view = LoginActivity.this.getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (imm != null) {
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
                TransitionManager.go(loginScene, transition);
                initLogin();
            }
        });

        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptRegistration();
            }
        });
    }

    public void attemptLogin() {
        progressDialog.show();
        TextInputEditText errorView = null;

        if (passwordView.getText().toString().length() < 8) {
            errorView = passwordView;
            passwordView.setError(getString(R.string.password_length_prompt));
        }
        if (!emailView.getText().toString().trim().matches(Patterns.EMAIL_ADDRESS.pattern())) {
            errorView = emailView;
            emailView.setError(getString(R.string.invalid_email_prompt));
        }

        if (errorView != null) {
            errorView.requestFocus();
            progressDialog.dismiss();
        } else {
            mAuth.signInWithEmailAndPassword(emailView.getText().toString().trim(), passwordView.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                currentUser = mAuth.getCurrentUser();
                                if (currentUser != null) {
                                    db.collection(USERS).document(currentUser.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                        @Override
                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                            String username = (String) documentSnapshot.get(Constants.FS_USER_NAME_KEY);
                                            String email = currentUser.getEmail();
                                            Float balance = Float.parseFloat(documentSnapshot.get(Constants.FS_USER_BALANCE_KEY).toString());
                                            SharedPreferences.Editor editor = prefs.edit();
                                            editor.putString(Constants.USER_NAME, username);
                                            editor.putString(Constants.USER_EMAIL, email);
                                            editor.apply();
                                            Log.d(TAG, "Name: " + username + "\nEmail: " + email + "\nBalance: " + balance);
                                            progressDialog.dismiss();
                                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            finish();
                                        }
                                    });
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    public void attemptRegistration() {
        progressDialog.show();
        TextInputEditText errorView = null;

        if (!passwordView.getText().toString().equals(confirmPasswordView.getText().toString())) {
            errorView = confirmPasswordView;
            confirmPasswordView.setError(getString(R.string.password_mismatch_prompt));
        }

        if (passwordView.getText().toString().length() < 8) {
            errorView = passwordView;
            passwordView.setError(getString(R.string.password_length_prompt));
        }
        if (!emailView.getText().toString().trim().matches(Patterns.EMAIL_ADDRESS.pattern())) {
            errorView = emailView;
            emailView.setError(getString(R.string.invalid_email_prompt));
        }

        if (nameView.getText().toString().trim().length() == 0) {
            errorView = nameView;
            nameView.setError(getString(R.string.input_name_prompt));
        }

        if (errorView != null) {
            errorView.requestFocus();
            progressDialog.dismiss();
        } else {
            mAuth.createUserWithEmailAndPassword(emailView.getText().toString().trim(), passwordView.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");

                                String name = nameView.getText().toString().trim();
                                initializeUser(name);

                                Toast.makeText(LoginActivity.this,
                                        "Successfully registered. Please sign in",
                                        Toast.LENGTH_SHORT).show();

                                progressDialog.dismiss();
                                TransitionManager.go(loginScene, transition);
                                initLogin();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                progressDialog.dismiss();
                                Toast.makeText(LoginActivity.this,
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }

    public void initializeUser(String name) {
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            Log.e(TAG, "current user is null");
            return;
        }
        String UID = currentUser.getUid();

        // Since we are redirecting to login
        mAuth.signOut();

        Map<String, Object> data = new HashMap<>();
        data.put(Constants.FS_USER_BALANCE_KEY, 20000.00);
        data.put(Constants.FS_TOTAL_VALUATION, 20000.00);
        data.put(Constants.FS_USER_NAME_KEY, name);
        data.put(Constants.FS_PURCHASED_CURRENCIES_KEY, new HashMap<String, Object>());
        db.collection(USERS).document(UID).set(data)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.d(TAG, "Successfully initialized user");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "Failure in user initialization");
                    }
                });
    }
}
