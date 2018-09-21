package com.sherlock.androidapp;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class MainActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button submit;
    private TextView toggle;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        emailEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        submit = findViewById(R.id.submit);
        toggle = findViewById(R.id.toggle);

        progressBar = findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(FirebaseAuth.getInstance().getCurrentUser() != null) {
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public void toggle(View view) {

        if (toggle.getText().toString().equals(getString(R.string.login))) {

            toggle.setText(R.string.signup);
            submit.setText("Log In");
        } else {
            toggle.setText(R.string.login);
            submit.setText("Sign Up");
        }
    }

    public void submit(View view) {

        String email = emailEditText.getText().toString();
        String password = passwordEditText.getText().toString();

        if (email.length() == 0 || password.length() == 0)
            return;

        if (toggle.getText().toString().equals(getString(R.string.login)))
            signup(email, password);
        else
            login(email, password);

        progressBar.setVisibility(View.VISIBLE);
    }

    private void login(String email, String password) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.INVISIBLE);
                if (!task.isSuccessful()) {
                    if (task.getException() != null) {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthInvalidCredentialsException e) {

                            CommonUtils.showSnackbar(findViewById(R.id.root), "Wrong Password", 2000);
                        } catch (FirebaseAuthInvalidUserException e) {

                            CommonUtils.showSnackbar(findViewById(R.id.root), "User Doesn't Exist.", 2000);

                        } catch (FirebaseNetworkException e) {

                            CommonUtils.showSnackbar(findViewById(R.id.root), "No Internet Connectivity.", 2000);
                        } catch (Exception e) {

                            CommonUtils.showSnackbar(findViewById(R.id.root), "An Error Occured", 2000);
                            Log.e(MainActivity.class.toString(), e.toString());
                        }
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void signup(String email, String password) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                progressBar.setVisibility(View.INVISIBLE);
                if (!task.isSuccessful()) {

                    if (task.getException() != null) {
                        try {
                            throw task.getException();
                        } catch (FirebaseAuthUserCollisionException e) {

                            CommonUtils.showSnackbar(findViewById(R.id.root), "Email Already in Use.", 2000);
                        } catch (FirebaseNetworkException e) {

                            CommonUtils.showSnackbar(findViewById(R.id.root), "No Internet Connectivity.", 2000);
                        } catch (Exception e) {

                            CommonUtils.showSnackbar(findViewById(R.id.root), "An Error Occurred", 2000);
                            Log.e(MainActivity.class.toString(), e.toString());
                        }
                    }
                } else {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }
}
