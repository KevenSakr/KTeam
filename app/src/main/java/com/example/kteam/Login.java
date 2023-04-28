package com.example.kteam;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Login extends AppCompatActivity {
    private EditText email;
    private EditText password;
    private Button loginButton,forgetButton;
    private TextView signUp,title,forget;

    private CheckBox rememberMe;
    private boolean b;
    private ProgressBar progressBar;
    private User me;
    private FirebaseAuth mAuth;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //setting up firebase
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //setting up widgets
        email = findViewById(R.id.username);
        password = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        signUp= findViewById(R.id.signupText);
        rememberMe=findViewById(R.id.remember);
        progressBar = findViewById(R.id.progressBar);
        title=findViewById(R.id.loginText);
        forget=findViewById(R.id.forget);
        forgetButton=findViewById(R.id.forgetButton);
        b=false;
        //check if there is a saved credential
        if(getEmail()!=null && getPassword()!=null){
            email.setText(getEmail());
            password.setText(getPassword());
        }
        //when login button is clicked
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String email1="",password1="";
                boolean bs=true;//to disable sign in
                //read data
                email1= email.getText().toString().trim();
                password1=password.getText().toString().trim();
                //check data
                if (TextUtils.isEmpty(password1)) {
                    password.setError("Required");bs=false;
                }
                if (TextUtils.isEmpty(email1)) {
                    email.setError("Required");bs=false;
                }
                //don't continue sign in
                if(!bs)
                    return;
                //remember me check box
                if(rememberMe.isChecked()){
                    saveEmail(email1);
                    savePassword(password1);
                }else{
                    saveEmail(null);
                    savePassword(null);
                }
                //authenticate user
                mAuth.signInWithEmailAndPassword(email1,password1).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            //pb.setVisibility(View.INVISIBLE);
                            FirebaseUser user = mAuth.getCurrentUser();
                            //check email verification
                            if(!user.isEmailVerified()){
                                //if not verified don't continue with sign in
                                Toast.makeText(Login.this, "Verify email before login!",Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.GONE);
                                return;
                            }
                            db.collection("users")
                                    .document(user.getUid())
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    Log.d(TAG, document.getId() + " => " + document.getData());
                                                    //reading user object from firestore
                                                    me=new User(document.getString("fname"),
                                                            document.getString("lname"),
                                                            document.getString("phone"),
                                                            document.getString("email"));
                                                    me.setTeamList((ArrayList<String>) document.get("team"));
                                                    me.setTaskList((ArrayList<String>) document.get("task"));
                                                    progressBar.setVisibility(View.GONE);
                                                    //Toast.makeText(Login.this, "Welcome "+me.getFirstName()+" "+me.getLastName()+"!", Toast.LENGTH_SHORT).show();
                                                    //if verified, go to AdminMain
                                                    Intent intent=new Intent(Login.this, MainActivity.class);
                                                    //sending user object after login
                                                    intent.putExtra("user",me);
                                                    startActivity(intent);;
                                                } else {
                                                    Log.d(TAG, "No such document");
                                                }
                                            } else {
                                                Log.w(TAG, "Error getting document", task.getException());
                                            }
                                        }
                                    });
                        }
                            else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "LoginWithEmail:failure", task.getException());
                            progressBar.setVisibility(View.GONE);
                                Toast.makeText(Login.this, "Login failed.",Toast.LENGTH_SHORT).show();
                            }
                    }
                });
            }
        });
        //to create an account
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Login.this,SignUp.class);
                startActivity(intent);
            }
        });
        //to reset password
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                title.setText("Reset Password");
                password.setVisibility(View.GONE);
                loginButton.setVisibility(View.GONE);
                forgetButton.setVisibility(View.VISIBLE);
                forget.setVisibility(View.GONE);
                signUp.setVisibility(View.GONE);
                rememberMe.setVisibility(View.GONE);
                b=true;
                forgetButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        progressBar.setVisibility(View.VISIBLE);
                        String ml = email.getText().toString();

                        if (TextUtils.isEmpty(ml)) {
                            email.setError("Email is required.");
                            return;
                        }

                        mAuth.sendPasswordResetEmail(ml)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(Login.this, "Reset password link sent to email.", Toast.LENGTH_LONG).show();
                                            title.setText("Login");
                                            password.setVisibility(View.VISIBLE);

                                            loginButton.setVisibility(View.VISIBLE);
                                            forgetButton.setVisibility(View.GONE);
                                            forget.setVisibility(View.VISIBLE);
                                            signUp.setVisibility(View.VISIBLE);
                                            rememberMe.setVisibility(View.VISIBLE);

                                            progressBar.setVisibility(View.GONE);
                                            b=false;
                                        } else {
                                            progressBar.setVisibility(View.GONE);
                                            Toast.makeText(Login.this, "Failed to send reset password link.", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                });
                        }

                });
            }
        });

    }
    //exit app on back press
    public void onBackPressed() {
        if(b) {
            //if forget password
            title.setText("Login");
            password.setVisibility(View.VISIBLE);
            loginButton.setText("LOGIN");
            forget.setVisibility(View.VISIBLE);
            signUp.setVisibility(View.VISIBLE);
            rememberMe.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            loginButton.setVisibility(View.VISIBLE);
            forgetButton.setVisibility(View.GONE);
            b=false;
            return;
        }
        //in login main
        super.onBackPressed();
        finishAffinity();
        System.exit(0);
    }

    void saveEmail(String str){
        //saving email in cache
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(Login.this);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("email",str);
        editor.apply();
    }
    void savePassword(String str){
        //saving passwordin cash
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(Login.this);
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString("password",str);
        editor.apply();
    }
    String getEmail(){
        //retrieving email
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(Login.this);
        return sharedPreferences.getString("email",null);
    }
    String getPassword(){
        //retrieving password
        SharedPreferences sharedPreferences= PreferenceManager.getDefaultSharedPreferences(Login.this);
        return sharedPreferences.getString("password",null);
    }
}