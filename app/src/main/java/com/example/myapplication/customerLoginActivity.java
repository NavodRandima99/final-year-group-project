package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class customerLoginActivity extends AppCompatActivity {

    private Button mLogin , mRegistertion , mGoBack;
    private EditText mEmail , mPassword, mReenterPassword, mNICnumber ,mEmailLogin, mPasswordLogin;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebasequthlistener;
    private ProgressBar mPrograssBar;
    private TextView mVeifiedEmalText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        mLogin = findViewById(R.id.loginButton);
        mRegistertion = findViewById(R.id.registerButton);

        mEmail = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        mGoBack = (Button) findViewById(R.id.goback);
        mReenterPassword = (EditText) findViewById(R.id.reenterpassword);
        mEmailLogin = (EditText) findViewById(R.id.emailLogin);
        mPasswordLogin = (EditText) findViewById(R.id.passwordLogin);
        mNICnumber = (EditText) findViewById(R.id.nicnumber);
        mVeifiedEmalText = (TextView) findViewById(R.id.verifiedText);
        mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(customerLoginActivity.this, splashScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }

        LoadingDialog loadingDialog =new LoadingDialog(customerLoginActivity.this);

        mGoBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(customerLoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        firebasequthlistener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        mRegistertion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmail.getText().toString();
                final String password = mPassword.getText().toString();
                final String reenterpassword = mReenterPassword.getText().toString();
                final String  nicNumber = mNICnumber.getText().toString();


                if(email.isEmpty()){
                     mEmail.setError("Please enter Email");
                     mEmail.requestFocus();
                     return;
                }
                if (password.isEmpty()){
                    mPassword.setError("Enter Strong password");
                    mPassword.requestFocus();
                    return;
                }
                if(reenterpassword.isEmpty()){
                    mReenterPassword.setError("Reenter your password");
                    mReenterPassword.requestFocus();
                    return;
                }
                if (password.length() <=5 ){
                    mPassword.setError("Passwod is too short");
                    mPassword.requestFocus();
                    return;
                }
                if (password.length() != reenterpassword.length()){
                    mReenterPassword.setError("Password are not matching");
                    mReenterPassword.requestFocus();
                    return;
                }

                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    mEmail.setError("Invalid Email address");
                    mEmail.requestFocus();
                    return;
                }
                if(password.length() == reenterpassword.length() && Patterns.EMAIL_ADDRESS.matcher(email).matches()){

                    loadingDialog.stratAlertAnimation();

                }
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(customerLoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                               if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                   loadingDialog.stopAlert();
                                   Toast.makeText(customerLoginActivity.this , "You are already registered" , Toast.LENGTH_LONG).show();
                                   return;

                               }else{
                                   Toast.makeText(customerLoginActivity.this , task.getException().getMessage() , Toast.LENGTH_LONG).show();

                               }
                            Toast.makeText(customerLoginActivity.this , "Check your internet connect" , Toast.LENGTH_LONG).show();
                        }else{

                            String user_id = mAuth.getCurrentUser().getUid();
                            DatabaseReference currnt_user_db = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(user_id);
                            DatabaseReference curretUserNIC = FirebaseDatabase.getInstance().getReference().child("Users").child("Customer").child(user_id).child("UserNIC").child(nicNumber);

                            final FirebaseUser user = mAuth.getCurrentUser();
                            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                       if(task.isSuccessful()){
                                           loadingDialog.stopAlert();
                                           Toast.makeText(customerLoginActivity.this , "Email has sent" , Toast.LENGTH_LONG).show();


                                       }else{
                                           Toast.makeText(customerLoginActivity.this , "Email Not sent" , Toast.LENGTH_LONG).show();

                                       }
                                }
                            });
                            currnt_user_db.setValue(true);
                            curretUserNIC.setValue(true);

                        }
                    }
                });
            }
        });

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String email = mEmailLogin.getText().toString();
                final String password = mPasswordLogin.getText().toString();
                final FirebaseAuth newauth = FirebaseAuth.getInstance(); ;

                if (email.isEmpty()){
                    mEmailLogin.setError("Please enter your Email");
                    mEmailLogin.requestFocus();
                    return;
                }
                if(password.isEmpty()){
                    mPasswordLogin.setError("Password is empty");
                    mPasswordLogin.requestFocus();
                    return;
                }
                if(!email.isEmpty() && !password.isEmpty()){
                    loadingDialog.stratAlertAnimation();
                }

                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            loadingDialog.stopAlert();
                         Toast.makeText(customerLoginActivity.this , task.getException().getMessage() , Toast.LENGTH_LONG).show();

                        }
                        else{
                            if(newauth.getCurrentUser().isEmailVerified()){
                                loadingDialog.stopAlert();
                                Intent intent = new Intent(getApplicationContext() , customerMapActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                                Toast.makeText(customerLoginActivity.this ,  "Verified" , Toast.LENGTH_LONG).show();


                            }else{
                                loadingDialog.stopAlert();
                                mVeifiedEmalText.setVisibility(View.VISIBLE);
                                mVeifiedEmalText.setText("Please verify your Email");


                            }



                        }
                    }
                });
            }
        });
    }



    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(firebasequthlistener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mAuth.removeAuthStateListener(firebasequthlistener);
    }
}