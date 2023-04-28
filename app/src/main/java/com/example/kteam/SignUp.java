package com.example.kteam;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.telephony.PhoneNumberUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignUp extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText firstName, lastName, email, phone, pass1, pass2;
    private Button signup;
    private ProgressBar pb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        // initializing firebase
        mAuth = FirebaseAuth.getInstance();
        // initializing widgets
        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        email = findViewById((R.id.email));
        phone = findViewById(R.id.phone);
        pass1 = findViewById(R.id.password);
        pass2 = findViewById(R.id.confirmpassword);
        signup = findViewById(R.id.signUpButton);
        pb = findViewById(R.id.progressBarSignUp);

        // get phone number from sim 1
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_SMS, android.Manifest.permission.READ_PHONE_NUMBERS, android.Manifest.permission.READ_PHONE_STATE}, 0);
        }
        String phoneNumber = telephonyManager.getLine1Number();
        phone.setText(phoneNumber);
        //process on sign-up button
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email1="",password1="",password2="",fname1="",lname1="",phone1="";
                String phoneInt;
                //read data from user input
                email1=email.getText().toString().trim();
                password1=pass1.getText().toString().trim();
                password2=pass2.getText().toString().trim();
                fname1=firstName.getText().toString().trim();
                lname1=lastName.getText().toString().trim();
                phone1=phone.getText().toString().trim();
                //validate input
                boolean b=true;
                if (TextUtils.isEmpty(fname1)) {
                    firstName.setError("Required");b=false;
                }
                if (TextUtils.isEmpty(lname1)) {
                    lastName.setError("Required");b=false;
                }
                if (TextUtils.isEmpty(email1)) {
                    email.setError("Required");b=false;
                }
                if (TextUtils.isEmpty(phone1)) {
                    phone.setError("Required");b=false;
                }
                if (TextUtils.isEmpty(password1)) {
                    pass1.setError("Required");
                    pass2.setError("Required");
                    b=false;
                }
                if (TextUtils.isEmpty(password2)) {
                    pass2.setError("Required");
                    b=false;
                }
                //don't continue sign up
                if(!b)
                    return;
                //validate email format
                if(!email1.matches( "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")){
                    Toast.makeText(SignUp.this, "Invalid email format!", Toast.LENGTH_SHORT).show();
                    email.setError("Invalid email format!");
                    return;
                }
                //validate phone lebanese number
                if(!phone1.matches("^\\d{8}")){
                    Toast.makeText(SignUp.this, "Use 8-digits format!", Toast.LENGTH_SHORT).show();
                    phone.setError("Use 8-digits format!");
                    return;
                }else{
                    phoneInt=phone1;
                    if(phone1.charAt(0)=='0') phoneInt=phone1.substring(1);
                    phoneInt = PhoneNumberUtils.formatNumber("+961" + phoneInt, "LB");
                    //phone.setText(phoneInt);
                    /*if(Patterns.PHONE.matcher(phoneInt).matches()){
                        return;
                    }*/
                    Log.d(TAG, phoneInt);
                }

                //check password match
                if(password1.compareTo(password2)!=0){
                    pass1.setError("Password doesn't match!");
                    pass2.setError("Password doesn't match!");
                    return;
                }
                //check password length
                if(password1.length()<6){
                    Toast.makeText(SignUp.this, "Minimum password length is 6!", Toast.LENGTH_SHORT).show();
                    pass1.setError("Minimum length is 6!");
                    pass2.setError("Minimum length is 6!");
                    return;
                }
                //set progressBar visible so the user don't get a frozen screen and start sign up process
                pb.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email1, password1)
                        .addOnCompleteListener(SignUp.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                pb.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    // Sign up success, update UI with the signed-in user's information then go to main
                                    pb.setVisibility(View.INVISIBLE);
                                    Log.d(TAG, "createUserWithEmail:success");
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    //set user name display
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(firstName.getText().toString().trim()+" "+lastName.getText().toString().trim())
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "User profile updated.");
                                                    }
                                                }
                                            });
                                    //request email verification
                                    user.sendEmailVerification();
                                    //save user info to Firestore
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> user1 = new HashMap<>();
                                    user1.put("fname", firstName.getText().toString().trim());
                                    user1.put("lname", lastName.getText().toString().trim());
                                    user1.put("email", email.getText().toString().trim().toLowerCase());
                                    user1.put("phone", phone.getText().toString().trim());
                                    user1.put("task",null);
                                    user1.put("team",null);
                                    db.collection("users").document(user.getUid())
                                            .set(user1)
                                            .addOnSuccessListener(documentReference -> {
                                                // User data saved successfully
                                                pb.setVisibility(View.GONE);
                                                startActivity(new Intent(SignUp.this,Login.class));
                                            })
                                            .addOnFailureListener(e -> {
                                                //delete user from firebase
                                                user.delete();
                                                return;
                                            });

                                } else {
                                    if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                            // Email already exists in Firebase Authentication system
                                            // Handle error or show message to user
                                            email.setError("Email already exists.");
                                            email.requestFocus();
                                        }
                                        else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                            Toast.makeText(SignUp.this, "Sign up failed.",
                                                    Toast.LENGTH_SHORT).show();
                                            //updateUI(null);
                                        }

                                }
                            }
                        });
            }
        });
    }
}