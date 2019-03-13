package com.bornstunner.firebaseauth;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;

public class SignUp extends AppCompatActivity{
    ProgressBar pb;

    EditText etEmail, etPassword;
    private FirebaseAuth mAuth;
    Button btnSignUp;
    TextView login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mAuth = FirebaseAuth.getInstance();



        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        login = findViewById(R.id.login);
        pb = findViewById(R.id.pb);


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUp.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

    }


    private void registerUser(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()){
            etEmail.setError("Email field cannot be empty");
            etEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Please enter a valid E-mail.");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()){
            etPassword.setError("Password field cannot be empty");
            etPassword.requestFocus();
            return;
        }

        if (password.length()< 6){
            etPassword.setError("Password cannot be less than 6 character");
            etPassword.requestFocus();
            return;
        }


        pb.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                pb.setVisibility(View.GONE);
                if (task.isSuccessful()){
                    //Toast.makeText(getApplicationContext(),"Registered Successfully.",Toast.LENGTH_LONG).show();


                    /*Intent intent = new Intent(SignUp.this, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);*/

                    finish();
                    startActivity(new Intent(SignUp.this, ProfileActivity.class));
                }
                else {
                    //Toast.makeText(getApplicationContext(), "Something Went wrong while trying to register", Toast.LENGTH_LONG).show();
                    if (task.getException() instanceof FirebaseAuthUserCollisionException){
                        Toast.makeText(SignUp.this, "You are already Registered.", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });

    }


}
