package com.example.kteam;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;


public class HistoryFragment extends Fragment {

    private User me;
    private ArrayList<MyTask> tasks;
    private ArrayList<TeamMember> teamMembers;
    private RelativeLayout viewTaskLayout, addTaskLayout;
    private LinearLayout header,header2,detail;
    private SearchView taskSearch;
    private RecyclerView taskRecyclerView;
    private EditText nameEnter, descriptionEnter;
    private Button allButton,buttonSave,deleteButton,finishButton;
    private ImageView backButton,backButton2;
    //FloatingActionButton addTask;
    private TextView detailTaskTitle, detailDesciption, memberAssigned,detailStart,detailEnd;
    private String name,description;
    private TaskAdapter2 adapter;
    private ListView listView;
    private FirebaseFirestore firestore;
    private ListenerRegistration snapshotListener;
    private ArrayList<String> items;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialize variables
        me =new User();
        tasks=new ArrayList<MyTask>();
        teamMembers=new ArrayList<TeamMember>();
        if (getArguments() != null) {
            me = getArguments().getParcelable("user");
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        //initialize widgets and views
        firestore = FirebaseFirestore.getInstance();
        viewTaskLayout =view.findViewById(R.id.viewTaskLayout);
        addTaskLayout =view.findViewById(R.id.addTaskLayout);
        taskSearch =view.findViewById(R.id.taskSearch);
        taskRecyclerView =view.findViewById(R.id.taskRecyclerView);
        nameEnter=view.findViewById(R.id.nameEnter);
        descriptionEnter =view.findViewById(R.id.descriptionEnter);
        allButton=view.findViewById(R.id.allButton);
        //addTask =view.findViewById(R.id.addTask);
        deleteButton=view.findViewById(R.id.deleteButton);
        backButton=view.findViewById(R.id.backButton);
        backButton2=view.findViewById(R.id.backButton2);
        header=view.findViewById(R.id.header);
        header2=view.findViewById(R.id.header2);
        detail=view.findViewById(R.id.detail);
        detailTaskTitle =view.findViewById(R.id.detailTaskTitle);
        detailDesciption =view.findViewById(R.id.detailDesciption);
        listView = view.findViewById(R.id.list_view);
        buttonSave=view.findViewById(R.id.buttonSave);
        memberAssigned=view.findViewById(R.id.memberAssigned);
        detailStart=view.findViewById(R.id.detailStart);
        detailEnd=view.findViewById(R.id.detailEnd);
        //adapter for scroll view
        adapter = new TaskAdapter2(getContext(), tasks);
        taskSearch.clearFocus();
        updateFromDB();
        adapter.setOnItemClickListener(new TaskAdapter2.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Handle click event and setting the view of the task details
                header.setVisibility(View.GONE);
                viewTaskLayout.setVisibility(View.GONE);
                detail.setVisibility(View.VISIBLE);
                detailTaskTitle.setText(adapter.dataList.get(position).getName());
                detailDesciption.setText(adapter.dataList.get(position).getDescription()+"");
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                detailStart.setText("Start: "+formatter.format(adapter.dataList.get(position).getStartDate()));
                detailEnd.setText("End: "+formatter.format(adapter.dataList.get(position).getEndDate()));
                String x="";
                for (String s:
                        adapter.dataList.get(position).getTeamIDList()) {
                    for (TeamMember tm:
                            teamMembers) {
                        if (tm.getMemberID().equals(s)){
                            x+=" - "+tm.getName()+"    "+tm.getPhoneNumber()+"\n";
                            break;
                        }
                    }

                }
                if(TextUtils.isEmpty(x))
                    x="None";
                memberAssigned.setText(x);
                buttonSave.setVisibility(View.INVISIBLE);
                //go back to the scroll view
                backButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        header.setVisibility(View.VISIBLE);
                        viewTaskLayout.setVisibility(View.VISIBLE);
                        detail.setVisibility(View.GONE);
                        buttonSave.setVisibility(View.INVISIBLE);
                        detailDesciption.setVisibility(View.VISIBLE);
                        detailTaskTitle.setVisibility(View.VISIBLE);
                        detailStart.setVisibility(View.VISIBLE);
                        detailEnd.setVisibility(View.VISIBLE);
                        memberAssigned.setVisibility(View.VISIBLE);
                    }
                });
                //delete the task and update the database and variables
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        adapter.dataList.get(position).removeTask (firestore,teamMembers);
                        tasks.remove(adapter.dataList.get(position));
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "Task Deleted!", Toast.LENGTH_SHORT).show();
                        taskSearch.setQuery("",false);
                        updateFromDB();
                        header.setVisibility(View.VISIBLE);
                        viewTaskLayout.setVisibility(View.VISIBLE);
                        detail.setVisibility(View.GONE);
                        view.invalidate();
                    }
                });
            }
        });
        //end
        updateFromDB();
        taskRecyclerView.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        taskRecyclerView.setLayoutManager(gridLayoutManager);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setting general view on back button
                viewTaskLayout.setVisibility(View.VISIBLE);
                addTaskLayout.setVisibility(View.GONE);
                backButton.setVisibility((View.GONE));
                descriptionEnter.setText("");
                descriptionEnter.setError(null);
                nameEnter.setError(null);
                nameEnter.setText("");
            }
        });
        //setting search operation
        taskSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                searchList(newText);
                return true;
            }
        });
        // Inflate the layout for this fragment
        return view;
    }
    public void searchList(String text){
        //performing search operation based on the task name
        ArrayList<MyTask> searchList = new ArrayList<>();
        for (MyTask dataClass: tasks){
            if (dataClass.getName().toLowerCase().contains(text.toLowerCase())){
                searchList.add(dataClass);
            }
        }
        adapter.searchDataList(searchList);
    }
    private void updateFromDB(){
        //getting data when updated from the database
        snapshotListener = firestore.collection("users").document(FirebaseAuth.getInstance().getUid()).collection("team").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                teamMembers.clear();
                for (DocumentSnapshot documentSnapshot : snapshot.getDocuments()) {
                    TeamMember tm=new TeamMember(documentSnapshot.getId(),name =  documentSnapshot.getString("name"),
                            documentSnapshot.getString("phone"),
                            (ArrayList<String>) documentSnapshot.get("taskID"),
                            documentSnapshot.getBoolean("available"));
                    teamMembers.add(tm);
                }
                adapter.notifyDataSetChanged();
            }
        });
        snapshotListener = firestore.collection("users").document(FirebaseAuth.getInstance().getUid()).collection("task").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    return;
                }
                tasks.clear();
                //getting finished tasks
                for (DocumentSnapshot documentSnapshot : snapshot.getDocuments()) {
                    if(documentSnapshot.getBoolean("isFinished")){
                        MyTask tk=new MyTask(documentSnapshot.getId(),
                                documentSnapshot.getString("name"),
                                documentSnapshot.getString("description"),
                                documentSnapshot.getDate("startDate"),
                                documentSnapshot.getDate("endDate"),
                                (ArrayList<String>) documentSnapshot.get("teamIDList"),
                                documentSnapshot.getBoolean("isFinished"));
                        tasks.add(tk);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}