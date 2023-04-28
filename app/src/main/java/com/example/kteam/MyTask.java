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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MyTask implements Comparable<MyTask>, Parcelable {
    private String taskID;
    private String name;
    private String description;
    private Date startDate;
    private Date endDate;
    private Boolean isFinished;
    private ArrayList<String> teamIDList;

    public MyTask() {
        this.teamIDList=new ArrayList<String>();
    }

    public MyTask(String taskID, String name, String description, Date startDate, Date endDate,ArrayList<String> l,Boolean isF) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        CollectionReference collectionRef = db.collection("users")
                .document(loggedUser.getUid())
                .collection("task");
        this.taskID = taskID;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        isFinished = isF;
        this.teamIDList=l;
    }

    public MyTask(String name, String description, Date startDate, Boolean isFinished) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference collectionRef = db.collection("users")
                .document(loggedUser.getUid())
                .collection("task");
        this.taskID = collectionRef.document().getId();
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate=null;
        this.isFinished = isFinished;
        this.teamIDList=new ArrayList<String>();
    }

    public String getTaskID() {
        return taskID;
    }
    public void setTaskID(String taskID) {
        this.taskID = taskID;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public Date getStartDate() {
        return startDate;
    }
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    public Date getEndDate() {
        return endDate;
    }
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    public ArrayList<String> getTeamIDList() {
        return teamIDList;
    }
    public void setTeamIDList(ArrayList<String> teamIDList) {
        this.teamIDList = teamIDList;
    }
    public void setFinished(Boolean finish){
        this.isFinished=finish;
    }
    public Boolean isTaskFinished(){
        return isFinished;
    }
    public void addTeamMember(TeamMember memb){
        int index = Collections.binarySearch(teamIDList, memb.getMemberID());
        if (index < 0) {
            index = -(index + 1); // compute insertion point
        }
        else if(index>=0){
            return;
        }
        teamIDList.add(index,  memb.getMemberID());
    }
    public void removeTask(FirebaseFirestore db,ArrayList<TeamMember> tl) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();
        db.collection("users")
                .document(loggedUser.getUid())
                .collection("task")
                .document(getTaskID()).delete();
        for (String str:
             teamIDList) {
            for (TeamMember tm:
                    tl) {
                if (str.equals(tm.getMemberID())) {
                    tm.getTaskID().remove(getTaskID());
                    tm.setTeamMember(db);
                    break;
                }
            }
        }
    }
    public void removeTask(FirebaseFirestore db) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();
        db.collection("users")
                .document(loggedUser.getUid())
                .collection("task")
                .document(getTaskID()).delete();
        /*for (String str:
             teamIDList) {
            for (TeamMember tm:
                    tl) {
                if (str.equals(tm.getMemberID())) {
                    tm.getTaskID().remove(getTaskID());
                    tm.setTeamMember(db);
                    break;
                }
            }
        }*/
    }
    @Override
    public int compareTo(MyTask o) {
        return taskID.compareTo(((MyTask)o).getTaskID());
    }
    public void getTask(){
        FirebaseFirestore db=FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser= mAuth.getCurrentUser();
        DocumentReference docRef = db.collection("users")
                .document(loggedUser.getUid())
                .collection("task")
                .document(getTaskID());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Data retrieved successfully
                    name =  documentSnapshot.getString("name");
                    description =  documentSnapshot.getString("description");
                    startDate=documentSnapshot.getDate("startDate");
                    endDate=documentSnapshot.getDate("endDate");
                    isFinished= documentSnapshot.getBoolean("isFinished");
                    teamIDList = (ArrayList<String>) documentSnapshot.get("teamIDList");
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
    public void setTask(FirebaseFirestore db) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser loggedUser = mAuth.getCurrentUser();
        Map<String, Object> member = new HashMap<>();
        member.put("name", name);
        member.put("description", description);
        member.put("startDate", startDate);
        member.put("endDate", endDate);
        member.put("isFinished", isFinished);
        member.put("teamIDList", teamIDList);
        db.collection("users")
                .document(loggedUser.getUid())
                .collection("task")
                .document(getTaskID())
                .set(member)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "Task successfully added to Firestore!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding Task to Firestore", e);
                    }
                });
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(taskID);
        dest.writeString(name);
        dest.writeString(startDate.toString());
        dest.writeString(endDate.toString());
        dest.writeString(isFinished.toString());
        dest.writeStringList(teamIDList);
    }
    public static final Parcelable.Creator<MyTask> CREATOR = new Parcelable.Creator<MyTask>() {
        @Override
        public MyTask createFromParcel(Parcel source) {
            return new MyTask(source);
        }

        @Override
        public MyTask[] newArray(int size) {
            return new MyTask[size];
        }
    };
    private MyTask(Parcel in) {
        taskID = in.readString();
        name = in.readString();
        try{
            startDate = DateFormat.getDateInstance().parse(in.readString());
            endDate = DateFormat.getDateInstance().parse(in.readString());
        }catch (ParseException e){
            e.printStackTrace();
        }
        isFinished =Boolean.valueOf(in.readString()) ;
        teamIDList = in.createStringArrayList();
    }
}
