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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TeamMember implements Comparable<TeamMember>, Parcelable {
    private String memberID;
    private String name;
    private String phoneNumber;
    private ArrayList<String> TaskID;
    private Boolean available;

    public TeamMember() {
        this.TaskID = new ArrayList<String>();
    }
    public TeamMember( String name, String phoneNumber) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("users")
                .document(loggedUser.getUid())
                .collection("team");
        this.memberID = collectionRef.document().getId();
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.available=true;
        this.TaskID = new ArrayList<String>();
    }

    public TeamMember(String memberID, String name, String phoneNumber, ArrayList<String> taskID, Boolean available) {
        this.memberID = memberID;
        this.name = name;
        this.phoneNumber = phoneNumber;
        TaskID = taskID;
        this.available = available;
    }

    public String getMemberID() {
        return memberID;
    }
    public void setMemberID(String memberID) {
        this.memberID = memberID;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhoneNumber() {
        return phoneNumber;
    }
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
    public ArrayList<String> getTaskID() {
        return TaskID;
    }
    public Boolean isAvailable(){
        return available;
    }
    public void setAvailability(Boolean availability){
        this.available=availability;
    }
    public void setMemberTask(ArrayList<String> TasksID) {
        this.TaskID = TasksID;
    }
    public void addTask(MyTask tsk){
        int index = Collections.binarySearch(TaskID, tsk.getTaskID());
        if (index < 0) {
            index = -(index + 1); // compute insertion point
        }
        else if(index>=0){
            return;
        }
        TaskID.add(index, tsk.getTaskID());
    }
    @Override
    public int compareTo(TeamMember o) {
        return memberID.compareTo(((TeamMember)o).getMemberID());
    }


    public void getTeamMember(){
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser= mAuth.getCurrentUser();
        DocumentReference docRef = db.collection("users")
                .document(loggedUser.getUid())
                .collection("team")
                .document(getMemberID());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Data retrieved successfully
                    name =  documentSnapshot.getString("name");
                    phoneNumber =  documentSnapshot.getString("phone");
                    available= documentSnapshot.getBoolean("available");
                    TaskID = (ArrayList<String>) documentSnapshot.get("taskID");
                } else {
                    // Document does not exist
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Error occurred while retrieving data
            }
        });

    }
    public void setTeamMember(FirebaseFirestore db) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();
        Map<String, Object> member = new HashMap<>();
        member.put("name", name);
        member.put("phone", phoneNumber);
        member.put("available", available);
        member.put("taskID", TaskID);
        db.collection("users")
                .document(loggedUser.getUid())
                .collection("team")
                .document(getMemberID())
                .set(member)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Team member successfully added to Firestore!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding Team member to Firestore", e);
                    }
                });
    }
    @Override
    public boolean equals(Object o) {
        if(o instanceof TeamMember && memberID==((TeamMember)o).getMemberID())
            return true;
        return false;
    }
    public void removeTeamMember(FirebaseFirestore db,ArrayList<MyTask> l) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();
        db.collection("users")
                .document(loggedUser.getUid())
                .collection("team")
                .document(getMemberID()).delete();
        for (String str:
                TaskID) {
            for (MyTask tk:
                    l) {
                if (str.equals(tk.getTaskID())) {
                    tk.getTeamIDList().remove(getMemberID());
                    tk.setTask(db);
                    break;
                }
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(memberID);
        dest.writeString(name);
        dest.writeString(phoneNumber);
        dest.writeString(available.toString());
        dest.writeStringList(TaskID);
    }
    public static final Parcelable.Creator<TeamMember> CREATOR = new Parcelable.Creator<TeamMember>() {
        @Override
        public TeamMember createFromParcel(Parcel source) {
            return new TeamMember(source);
        }

        @Override
        public TeamMember[] newArray(int size) {
            return new TeamMember[size];
        }
    };

    private TeamMember(Parcel in) {
        memberID = in.readString();
        name = in.readString();
        phoneNumber = in.readString();
        available =Boolean.valueOf(in.readString()) ;
        TaskID = in.createStringArrayList();
    }
}
