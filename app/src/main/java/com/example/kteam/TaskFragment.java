package com.example.kteam;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.*;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;


public class TaskFragment extends Fragment {
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
    private FloatingActionButton addTask;
    private TextView  detailTaskTitle, detailDesciption, memberAssigned,detailStart;
    private String name,description;
    private TaskAdapter adapter;
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
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        //initialize widgets and views
        firestore = FirebaseFirestore.getInstance();
        viewTaskLayout =view.findViewById(R.id.viewTaskLayout);
        addTaskLayout =view.findViewById(R.id.addTaskLayout);
        taskSearch =view.findViewById(R.id.taskSearch);
        taskRecyclerView =view.findViewById(R.id.taskRecyclerView);
        nameEnter=view.findViewById(R.id.nameEnter);
        descriptionEnter =view.findViewById(R.id.descriptionEnter);
        allButton=view.findViewById(R.id.allButton);
        addTask =view.findViewById(R.id.addTask);
        deleteButton=view.findViewById(R.id.deleteButton);
        finishButton=view.findViewById(R.id.finishButton);
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
        //adapter for scroll view
        adapter = new TaskAdapter(getContext(), tasks);
        taskSearch.clearFocus();
        //update data
        updateFromDB();
        //adapter onclick listener to view tasks
        adapter.setOnItemClickListener(new TaskAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Handle click event
                //memberAssigned=view.findViewById(R.id.memberAssigned);
                //memberAssigned2=view.findViewById(R.id.memberAssigned2);
                //detailStart=

                header.setVisibility(View.GONE);
                viewTaskLayout.setVisibility(View.GONE);
                detail.setVisibility(View.VISIBLE);
                detailTaskTitle.setText(adapter.dataList.get(position).getName());
                detailDesciption.setText(adapter.dataList.get(position).getDescription()+"");
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                detailStart.setText("Start: "+formatter.format(adapter.dataList.get(position).getStartDate()));
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
                        memberAssigned.setVisibility(View.VISIBLE);
                    }
                });
                //delete task
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //setting views and updating firestore
                        adapter.dataList.get(position).removeTask (firestore,teamMembers);
                        tasks.remove(adapter.dataList.get(position));
                        Toast.makeText(getContext(), "Task Deleted!", Toast.LENGTH_SHORT).show();
                        taskSearch.setQuery("",false);
                        view.invalidate();
                        updateFromDB();
                        header.setVisibility(View.VISIBLE);
                        viewTaskLayout.setVisibility(View.VISIBLE);
                        detail.setVisibility(View.GONE);
                        view.invalidate();
                    }
                });
                //finishing the task
                finishButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //setting views and updating firestore
                        adapter.dataList.get(position).setFinished(true);
                        adapter.dataList.get(position).setEndDate(new Date());
                        adapter.dataList.get(position).setTask(firestore);
                        tasks.remove( adapter.dataList.get(position));
                        Toast.makeText(getContext(), "Task Finished!", Toast.LENGTH_SHORT).show();
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

        taskRecyclerView.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        taskRecyclerView.setLayoutManager(gridLayoutManager);

        //Creating a new task
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setting view for creating a task
                viewTaskLayout.setVisibility(View.GONE);
                addTaskLayout.setVisibility(View.VISIBLE);
                addTask.setVisibility(View.GONE);
                backButton.setVisibility((View.VISIBLE));
                //getting available team members
                //create an array of strings to populate the ListView
                items = new ArrayList<String>();
                String x;
                for (TeamMember tm:
                     teamMembers) {
                    if (tm.isAvailable()){
                        x=tm.getName()+"\n"+tm.getPhoneNumber();
                        items.add(x);
                    }
                }
                Collections.sort(items);
                // create an adapter that uses the android.R.layout.simple_list_item_multiple_choice layout
                ArrayAdapter<String> adapter1 = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_list_item_multiple_choice, items);
                // set the adapter on the ListView
                listView.setAdapter(adapter1);
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setting general view on back button
                viewTaskLayout.setVisibility(View.VISIBLE);
                addTaskLayout.setVisibility(View.GONE);
                addTask.setVisibility(View.VISIBLE);
                backButton.setVisibility((View.GONE));
                descriptionEnter.setText("");
                descriptionEnter.setError(null);
                nameEnter.setError(null);
                nameEnter.setText("");
            }
        });
        //on create task button press
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //get data
                name = nameEnter.getText().toString().trim();
                description = descriptionEnter.getText().toString().trim();
                if (TextUtils.isEmpty(name)) {
                    nameEnter.setError("Required");
                    return;
                }
                if (TextUtils.isEmpty(description)) {
                    descriptionEnter.setError("Required");
                    return;
                }

                //create the new task
                MyTask tk = new MyTask(name, description, new Date(), false);
                // get the checked item positions from the ListView
                SparseBooleanArray checkedItems = listView.getCheckedItemPositions();
                ArrayList<String> memberChecked = new ArrayList<String>();
                // loop through the checked items to add team members to the task
                int size=checkedItems.size();
                for (int i = 0; i < size; i++) {
                    int pos = checkedItems.keyAt(i);
                    boolean isChecked = checkedItems.valueAt(i);
                    if (isChecked) {
                        System.out.println();
                        memberChecked.add(items.get(pos).substring(items.get(pos).indexOf("\n") + 1));
                        listView.setItemChecked(pos, false);
                    }
                }
                for (String nb :
                        memberChecked) {
                    for (TeamMember tm :
                            teamMembers) {
                        if (nb.equals(tm.getPhoneNumber())) {
                            tk.addTeamMember(tm);
                            tm.addTask(tk);
                            tm.setTeamMember(firestore);
                            break;
                        }
                    }
                }
                if ( !memberChecked.isEmpty()) {
                    //send a message to the members assigned to the task that contains the task info
                    String message = "Task Launched By:\n" + me.getFirstName() + " " + me.getLastName() +
                            "\nTask Title:\n" + tk.getName()
                            + "\nTask Description:\n" + tk.getDescription();
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("smsto:" + Uri.encode(TextUtils.join(",", memberChecked))));
                    intent.putExtra("sms_body", message);
                    startActivity(intent);
                }
                //update data
                tasks.add(tk);
                tk.setTask(firestore);
                Toast.makeText(getContext(), "Task Created!", Toast.LENGTH_SHORT).show();
                descriptionEnter.setText("");
                nameEnter.setText("");
            }
        });
        //setting search view
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
        //doing the search operation
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
                //getting ongoing task only
                for (DocumentSnapshot documentSnapshot : snapshot.getDocuments()) {
                    if(!documentSnapshot.getBoolean("isFinished")){
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