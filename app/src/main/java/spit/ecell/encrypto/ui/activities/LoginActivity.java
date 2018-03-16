package spit.ecell.encrypto.ui.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PatternMatcher;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.transition.Scene;
import android.transition.TransitionManager;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

import spit.ecell.encrypto.R;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private FirebaseAuth mAuth;
    Scene loginScene;
    Scene registerScene;
    TextInputEditText emailView, passwordView, nameView, confirmPasswordView;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        CardView card = findViewById(R.id.cardView);
        Button registerButton = findViewById(R.id.registerButton);

        loginScene = Scene.getSceneForLayout(card, R.layout.card_login, this);
        registerScene = Scene.getSceneForLayout(card, R.layout.card_register, this);

        prefs = getSharedPreferences(Constants.PREFS,MODE_PRIVATE);

        initLogin();
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
                TransitionManager.go(registerScene);
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
                TransitionManager.go(loginScene);
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
        } else {
            mAuth.signInWithEmailAndPassword(emailView.getText().toString().trim(), passwordView.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();
                                if (user != null) {
                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    public void attemptRegistration() {
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
        } else {
            mAuth.createUserWithEmailAndPassword(emailView.getText().toString().trim(), passwordView.getText().toString())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");

                                prefs.edit()
                                        .putString(Constants.USER_NAME,nameView.getText().toString())
                                        .apply();

                                Toast.makeText(LoginActivity.this,
                                        "Successfully registered. Please sign in",
                                        Toast.LENGTH_SHORT).show();

                                TransitionManager.go(loginScene);
                                initLogin();
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(LoginActivity.this,
                                        "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
        }
    }
}
