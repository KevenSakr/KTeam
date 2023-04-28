package com.example.kteam;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.*;


import java.util.ArrayList;


public class TeamFragment extends Fragment {
    private User me;
    private ArrayList<MyTask> tasks;
    private ArrayList<TeamMember> teamMembers;
    private RelativeLayout viewTeamLayout,addTeamLayout;
    private LinearLayout header,header2,detail;
    private SearchView teamSearch;
    private RecyclerView teamRecyclerView;
    private EditText nameEnter,phoneEnter,codeEnter;
    private Button allButton,buttonSave;
    private Switch switchAvail;
    private ImageView backButton,backButton2;
    private FloatingActionButton addTeam;
    private Button editButton,deleteButton;
    private TextView detailAvail,detailName,detailPhone,taskk,taskks;
    private EditText detailName2,detailPhone2;
    private String name,phone;
    private TeamMemberAdapter adapter;
    private FirebaseFirestore firestore;
    private ListenerRegistration snapshotListener;

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
        View view = inflater.inflate(R.layout.fragment_team, container, false);
        //initialize widgets and views
        firestore = FirebaseFirestore.getInstance();
        viewTeamLayout=view.findViewById(R.id.viewTeamLayout);
        addTeamLayout=view.findViewById(R.id.addTeamLayout);
        teamSearch=view.findViewById(R.id.teamSearch);
        teamRecyclerView=view.findViewById(R.id.teamRecyclerView);
        nameEnter=view.findViewById(R.id.nameEnter);
        phoneEnter=view.findViewById(R.id.phoneEnter);
        codeEnter=view.findViewById(R.id.codeEnter);
        allButton=view.findViewById(R.id.allButton);
        addTeam=view.findViewById(R.id.addTeam);
        deleteButton=view.findViewById(R.id.deleteButton);
        editButton=view.findViewById(R.id.editButton);
        backButton=view.findViewById(R.id.backButton);
        backButton2=view.findViewById(R.id.backButton2);
        header=view.findViewById(R.id.header);
        header2=view.findViewById(R.id.header2);
        detail=view.findViewById(R.id.detail);
        detailName=view.findViewById(R.id.detailTeamName);
        detailPhone=view.findViewById(R.id.detailPhone);
        detailName2=view.findViewById(R.id.detailTeamName2);
        detailPhone2=view.findViewById(R.id.detailPhone2);
        detailAvail=view.findViewById(R.id.detailAvailable);
        buttonSave=view.findViewById(R.id.buttonSave);
        switchAvail=view.findViewById(R.id.switchAvail);
        taskk=view.findViewById(R.id.task);
        taskks=view.findViewById(R.id.tasks);
        //adapter for scroll view
        adapter = new TeamMemberAdapter(getContext(), teamMembers);
        //search view
        teamSearch.clearFocus();
        //when a item of the adapter is clicked
        adapter.setOnItemClickListener(new TeamMemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                // Handle click event and show team member info
                header.setVisibility(View.GONE);
                viewTeamLayout.setVisibility(View.GONE);
                detail.setVisibility(View.VISIBLE);
                detailName.setText(adapter.dataList.get(position).getName());
                detailPhone.setText(adapter.dataList.get(position).getPhoneNumber());
                detailAvail.setText(adapter.dataList.get(position).isAvailable()?"Available":"Not Available");
                buttonSave.setVisibility(View.INVISIBLE);
                switchAvail.setVisibility(View.INVISIBLE);
                detailName2.setVisibility(View.GONE);
                detailPhone2.setVisibility(View.GONE);
                //show on going task
                String x="";
                for (String s:
                        adapter.dataList.get(position).getTaskID()) {
                    for (MyTask t:
                         tasks) {
                        if (t.getTaskID().equals(s)) {
                            if(!t.isTaskFinished()){
                                x+="-"+t.getName()+"\n";
                            }
                            break;
                        }
                    }
                }
                if(!x.equals("")){
                    taskks.setText(x);
                }
                else {
                    taskks.setText("None");
                }

                //go back to the scrollview
                backButton2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //setting the view
                        header.setVisibility(View.VISIBLE);
                        viewTeamLayout.setVisibility(View.VISIBLE);
                        detail.setVisibility(View.GONE);
                        buttonSave.setVisibility(View.INVISIBLE);
                        switchAvail.setVisibility(View.INVISIBLE);
                        detailPhone.setVisibility(View.VISIBLE);
                        detailName.setVisibility(View.VISIBLE);
                        taskk.setVisibility(View.VISIBLE);
                        taskks.setVisibility(View.VISIBLE);
                        detailName2.setVisibility(View.GONE);
                        detailPhone2.setVisibility(View.GONE);
                    }
                });
                //to delete a team member
                deleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        for (String st :
                                adapter.dataList.get(position).getTaskID()) {
                            for (MyTask tsk :
                                    tasks) {
                                if(st.equals(tsk.getTaskID()) && !tsk.isTaskFinished()){
                                    //disable delete for a team member with on going tasks
                                    Toast.makeText(getContext(), "You can't remove a team member with on going task!", Toast.LENGTH_SHORT).show();
                                    return;
                                }
                            }
                        }
                        for (MyTask tsk :
                                tasks) {
                        }
                        //on success of the remove update the firestore and the adapter
                        adapter.dataList.get(position).removeTeamMember(FirebaseFirestore.getInstance(),tasks);
                        teamMembers.remove(adapter.dataList.get(position));
                        Toast.makeText(getContext(), "Removed!", Toast.LENGTH_SHORT).show();
                        teamSearch.setQuery("",false);
                        updateFromDB();
                        header.setVisibility(View.VISIBLE);
                        viewTeamLayout.setVisibility(View.VISIBLE);
                        detail.setVisibility(View.GONE);
                        view.invalidate();
                    }
                });
                //to edit a team member
                editButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //setting the view
                        buttonSave.setVisibility(View.VISIBLE);
                        switchAvail.setVisibility(View.VISIBLE);
                        detailName.setVisibility(View.GONE);
                        detailPhone.setVisibility(View.GONE);
                        detailName2.setVisibility(View.VISIBLE);
                        detailPhone2.setVisibility(View.VISIBLE);
                        taskk.setVisibility(View.GONE);
                        taskks.setVisibility(View.GONE);
                        TeamMember thisMember=adapter.dataList.get(position);
                        String nphone=thisMember
                                .getPhoneNumber().
                                replaceAll(" ","").
                                substring(4);
                        detailName2.setText(thisMember.getName());
                        if(nphone.length()==7)
                            nphone="0"+nphone;
                        detailPhone2.setText(nphone);
                        switchAvail.setChecked(thisMember.isAvailable());
                        switchAvail.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if(isChecked)
                                    detailAvail.setText("Available");
                                else
                                    detailAvail.setText("Not Available");
                            }
                        });
                        //on save button save the team member after edit
                        buttonSave.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //getting data and validating
                                name=detailName2.getText().toString().trim();
                                phone=detailPhone2.getText().toString().trim();
                                if(TextUtils.isEmpty(name)){
                                    detailName2.setError("Required");
                                    return;
                                }
                                if(TextUtils.isEmpty(phone)){
                                    detailPhone2.setError("Required");
                                    return;
                                }
                                //validate phone lebanese number
                                if(!phone.matches("^\\d{8}")){
                                    //Toast.makeText(getContext(), "Use 8-digits format!", Toast.LENGTH_SHORT).show();
                                    detailPhone2.setError("Use 8-digits format!");
                                    return;
                                }
                                //format phone number
                                if(phone.startsWith("0"))
                                    phone=phone.replace("0","");
                                String phoneNumber= PhoneNumberUtils.formatNumber("+961" + phone, "LB");
                                for (TeamMember tmm:
                                        teamMembers) {
                                    if(tmm.getMemberID()==thisMember.getMemberID())
                                        continue;
                                    if(tmm.getPhoneNumber().equals(phoneNumber)){
                                        Toast.makeText(getContext(), "Number already exists!", Toast.LENGTH_SHORT).show();
                                        phoneEnter.setError("Number already exists!");
                                        return;
                                    }
                                }
                                //updating the data
                                thisMember.setName(name);
                                thisMember.setPhoneNumber(phoneNumber);
                                thisMember.setAvailability(switchAvail.isChecked());
                                thisMember.setTeamMember(firestore);
                                Toast.makeText(getContext(), "Edit Done!", Toast.LENGTH_SHORT).show();
                                teamSearch.setQuery("",false);
                                backButton2.callOnClick();

                            }
                        });

                    }
                });

            }
        });

        updateFromDB();
        teamRecyclerView.setAdapter(adapter);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 1);
        teamRecyclerView.setLayoutManager(gridLayoutManager);
        //setting the view on creating a new tem member
        addTeam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewTeamLayout.setVisibility(View.GONE);
                addTeamLayout.setVisibility(View.VISIBLE);
                codeEnter.setVisibility(View.GONE);
                addTeam.setVisibility(View.GONE);
                backButton.setVisibility((View.VISIBLE));
            }
        });
        //when the back button is pressed return to the scroll view
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewTeamLayout.setVisibility(View.VISIBLE);
                addTeamLayout.setVisibility(View.GONE);
                codeEnter.setVisibility(View.VISIBLE);
                addTeam.setVisibility(View.VISIBLE);
                backButton.setVisibility((View.GONE));
                phoneEnter.setText("");
                phoneEnter.setError(null);
                nameEnter.setError(null);
                nameEnter.setText("");

            }
        });
        //allButton is used to create the new user
        allButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //getting user input and validating
                name=nameEnter.getText().toString().trim();
                phone=phoneEnter.getText().toString().trim();
                if(TextUtils.isEmpty(name)){
                    nameEnter.setError("Required");
                    return;
                }
                if(TextUtils.isEmpty(phone)){
                    phoneEnter.setError("Required");
                    return;
                }
                //validate phone lebanese number
                if(!phone.matches("^\\d{8}")){
                    //Toast.makeText(getContext(), "Use 8-digits format!", Toast.LENGTH_SHORT).show();
                    phoneEnter.setError("Use 8-digits format!");
                    return;
                }
                //format phone number
                if(phone.startsWith("0"))
                    phone=phone.replace("0","");
                String phoneNumber= PhoneNumberUtils.formatNumber("+961" + phone, "LB");
                for (TeamMember tmm:
                     teamMembers) {
                    if(tmm.getPhoneNumber().equals(phoneNumber)){
                        //unique phone number for the team members
                        Toast.makeText(getContext(), "Number already exists!", Toast.LENGTH_SHORT).show();
                        phoneEnter.setError("Number already exists!");
                        return;
                    }
                }
                TeamMember tm=new TeamMember(name,phoneNumber);
                teamMembers.add(tm);
                tm.setTeamMember(FirebaseFirestore.getInstance());
                Toast.makeText(getContext(), "Team Member Added!", Toast.LENGTH_SHORT).show();
                phoneEnter.setText("");
                nameEnter.setText("");
            }
        });
        //when performing search query
        teamSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
   private void searchList(String text){
        //get team members with name contains text searched for
        ArrayList<TeamMember> searchList = new ArrayList<>();
        for (TeamMember dataClass: teamMembers){
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
                for (DocumentSnapshot documentSnapshot : snapshot.getDocuments()) {
                    MyTask tk=new MyTask(documentSnapshot.getId(),
                            documentSnapshot.getString("name"),
                            documentSnapshot.getString("description"),
                            documentSnapshot.getDate("startDate"),
                            documentSnapshot.getDate("endDate"),
                            (ArrayList<String>) documentSnapshot.get("teamIDList"),
                            documentSnapshot.getBoolean("isFinished"));
                    tasks.add(tk);

                }
                adapter.notifyDataSetChanged();
            }
        });
    }
}