package com.example.kteam;

import static android.content.ContentValues.TAG;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class User implements Parcelable {
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private ArrayList<String> taskList;
    private ArrayList<String> teamList;
    public User(String firstName, String lastName, String phone, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.taskList = new ArrayList<String>();
        this.teamList = new ArrayList<String>();
    }
    public User() {
        this.taskList = new ArrayList<String>();
        this.teamList = new ArrayList<String>();
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public ArrayList<String> getTaskList() {
        return taskList;
    }
    public void setTaskList(ArrayList<String> taskList) {
        this.taskList = taskList;
    }
    public ArrayList<String> getTeamList() {
        return teamList;
    }
    public void setTeamList(ArrayList<String> teamList) {
        this.teamList = teamList;
    }
    public void AddTeamMember(TeamMember memb){
        if(memb ==null)
            return;
        int index = Collections.binarySearch(teamList, memb.getMemberID());
        if (index < 0) {
            index = -(index + 1); // compute insertion point
        }
        else if(index>=0){
            return;
        }
        teamList.add(index,  memb.getMemberID());
    }
    public void AddTask(MyTask tsk){
        int index = Collections.binarySearch(taskList, tsk.getTaskID());
        if (index < 0) {
            index = -(index + 1); // compute insertion point
        }
        else if(index>=0){
            return;
        }
        taskList.add(index, tsk.getTaskID());
    }
    public void getUser(FirebaseFirestore db,String userID){
        DocumentReference docRef = db.collection("users").document(userID);

        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Data retrieved successfully
                    setFirstName(documentSnapshot.getString("fname"));
                    lastName = documentSnapshot.getString("lname");
                    phone = documentSnapshot.getString("phone");
                    email = documentSnapshot.getString("email");
                    taskList=(ArrayList<String>) documentSnapshot.get("task");
                    teamList=(ArrayList<String>) documentSnapshot.get("team");
                } else {
                    // Document does not exist
                    System.exit(0);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error occurred while retrieving data
                System.exit(0);
            }
        });

    }
    public void setUser(FirebaseFirestore db) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();
        Map<String, Object> user = new HashMap<>();
        user.put("fname", firstName);
        user.put("lname", lastName);
        user.put("phone", phone);
        user.put("email", email);
        user.put("task", taskList);
        user.put("team", teamList);
        db.collection("users")
                .document(loggedUser.getUid())
                .set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "User successfully added to Firestore!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding user to Firestore", e);
                    }
                });
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(phone);
        dest.writeString(email);
        dest.writeStringList(taskList);
        dest.writeStringList(teamList);
    }
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
    private User(Parcel in) {
        firstName = in.readString();
        lastName = in.readString();
        phone = in.readString();
        email = in.readString();
        taskList = in.createStringArrayList();
        teamList = in.createStringArrayList();
    }
}