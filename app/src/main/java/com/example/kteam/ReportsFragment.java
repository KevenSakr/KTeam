package com.example.kteam;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ReportsFragment extends Fragment {
    private User me;
    private ArrayList<MyTask> tasks;
    private ArrayList<TeamMember> teamMembers;
    private BarChart chart;
    private PieChart pieChart,pieChart1;
    private GradientDrawable drawable;
    private FirebaseFirestore firestore;
    private View view;
    private TextView tasks_created,tasks_finished,tasks_pending;
    private TableLayout tableLayout;
    private TextView total_members,available_members,unavailable_members;
    private ListenerRegistration snapshotListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        view = inflater.inflate(R.layout.fragment_reports, container, false);
        //today task completion
        pieChart=view.findViewById(R.id.piechart);
        //team members availability
        pieChart1=view.findViewById(R.id.piechart1);
        //task per months chart
        chart = view.findViewById(R.id.barchart);
        //tasks stat
        tasks_pending=view.findViewById(R.id.tasks_pending);
        tasks_created=view.findViewById(R.id.tasks_created);
        tasks_finished=view.findViewById(R.id.tasks_finished);
        total_members=view.findViewById(R.id.total_members);
        //members stat
        available_members=view.findViewById(R.id.available_members);
        unavailable_members=view.findViewById(R.id.unavailable_members);
        //team members table
        tableLayout = view.findViewById(R.id.table);
        drawable = new GradientDrawable();
        drawable.setStroke(2, Color.BLACK); // set border width and color
        drawable.setShape(GradientDrawable.RECTANGLE); // set shape
        view.findViewById(R.id.t1).setBackground(drawable);
        view.findViewById(R.id.t2).setBackground(drawable);
        view.findViewById(R.id.t3).setBackground(drawable);
        firestore = FirebaseFirestore.getInstance();
        //getting the data from firestore
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
                //setting data for the barChart
                BarData data = new BarData(getXAxisValues(), getDataSet());
                //setting the data bar values to integer (default float), it appear on top of each bar
                data.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
                        int intValue = (int) value;
                        return String.valueOf(intValue);
                    }
                });
                //adding data, description and animation to the chart
                chart.setData(data);
                chart.setDescription("Task Report");
                chart.animateXY(1000, 1000);
                chart.setDrawValueAboveBar(true);//data appear above the bar
                chart.invalidate();
            }
        });
        snapshotListener = firestore.collection("users").document(FirebaseAuth.getInstance().getUid()).collection("team").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshot, @Nullable FirebaseFirestoreException error) {
                if (error != null || getContext()==null) {
                    return;
                }
                teamMembers.clear();
                ArrayList<TmData> dt=new ArrayList<TmData>();
                for (DocumentSnapshot documentSnapshot : snapshot.getDocuments()) {
                    TeamMember tm = new TeamMember(documentSnapshot.getId(), documentSnapshot.getString("name"),
                            documentSnapshot.getString("phone"),
                            (ArrayList<String>) documentSnapshot.get("taskID"),
                            documentSnapshot.getBoolean("available"));
                    teamMembers.add(tm);
                    //stat for team members
                    int compl=0,tot=tm.getTaskID().size();
                    for (String s:
                            tm.getTaskID() ) {
                        for (MyTask tsk :
                                tasks) {
                            if(tsk.getTaskID().equals(s)){
                                if( tsk.isTaskFinished()){
                                    compl++;
                                }
                                break;
                            }

                        }
                    }
                    //data for the table
                    dt.add(new TmData(tm.getName(), tot,compl));
                }
                //sort alphabetic
                dt.sort(null);
                for (TmData d:
                     dt) {
                    addRow(d.name,d.tot,d.compl,getContext());
                }
                //draw team member chart
                setPieChart1();
            }
        });

        //to enable scrolling inside the barchart
        chart.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                        // Disable scroll view's parent to intercept touch events
                        v.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                    case MotionEvent.ACTION_UP:
                        // Enable scroll view's parent to intercept touch events
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                // Handle BarChart touch events
                chart.onTouchEvent(event);
                return true;
            }
        });
        return view;
    }
    private ArrayList getDataSet() {
        //function to get the value of tasks for each months of the last 12 months
        ArrayList dataSets = null;
        int []start=new int[12],
                finished=new int[12],
                pending=new int[12];
        Arrays.fill(start,0);
        Arrays.fill(finished,0);
        Arrays.fill(pending,0);
        //used for the piechart of the today tasks
        int td_cr=0,td_fn=0;
        //format date to compare with today
        SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
        Date today=new Date();
        for (MyTask tk :
                tasks) {
            if(tk.getStartDate().getTime()<(new Date(today.getYear()-1,today.getMonth(),1)).getTime())
                continue;
            if(formatDate.format(tk.getStartDate()).equals(formatDate.format(new Date()))){
                td_cr+=1;
                if(tk.isTaskFinished()){
                    td_fn+=1 ;
                }
            }
            start[tk.getStartDate().getMonth()]+=1 ;
            if(tk.isTaskFinished()){
                finished[tk.getStartDate().getMonth()]+=1 ;
            }else{
                pending[tk.getStartDate().getMonth()]+=1 ;
            }
        }
        //if there is today task draw the pie chart
        if(td_cr!=0){
            pieChart.setVisibility(View.VISIBLE);
            setPieChart(td_cr,td_fn,td_cr-td_fn);
            tasks_created.setText("Created : "+Integer.toString(td_cr) );
            tasks_finished.setText("Finished : "+Integer.toString(td_fn) +"/"+Integer.toString(td_cr)+"  "+Integer.toString(100*td_fn/td_cr)+"%" );
            tasks_pending.setText("Pending : "+Integer.toString(td_cr-td_fn) +"/"+Integer.toString(td_cr)+"  "+Integer.toString(100*(td_cr-td_fn)/td_cr)+"%" );
        }
        else {
            pieChart.setVisibility(View.INVISIBLE);
        }
        //setting the bar data values
        ArrayList valueSet1 = new ArrayList();
        ArrayList valueSet2 = new ArrayList();
        ArrayList valueSet3 = new ArrayList();
        for(int i=0;i<12;i++){
            BarEntry v1e1 = new BarEntry(start[i], i);
            valueSet1.add(v1e1);
            BarEntry v2e1 = new BarEntry(finished[i], i);
            valueSet2.add(v2e1);
            BarEntry v3e1 = new BarEntry(pending[i], i);
            valueSet3.add(v3e1);
        }
        //bar data legend and colors
        BarDataSet barDataSet1 = new BarDataSet(valueSet1, "Task Created");
        barDataSet1.setColor(Color.rgb(0, 155, 0));
        BarDataSet barDataSet2 = new BarDataSet(valueSet2, "Task Finished");
        barDataSet2.setColor(Color.rgb(0, 0,155));
        BarDataSet barDataSet3 = new BarDataSet(valueSet3, "Task Pending");
        barDataSet3.setColor(Color.rgb(155, 0,0));
        //setting and returning the data to the chart
        dataSets = new ArrayList();
        dataSets.add(barDataSet1);
        dataSets.add(barDataSet2);
        dataSets.add(barDataSet3);
        return dataSets;
    }

    private ArrayList getXAxisValues() {
        //used to display the months on the chart
        ArrayList xAxis = new ArrayList();
        xAxis.add("JAN");
        xAxis.add("FEB");
        xAxis.add("MAR");
        xAxis.add("APR");
        xAxis.add("MAY");
        xAxis.add("JUN");
        xAxis.add("JUL");
        xAxis.add("AUG");
        xAxis.add("SEP");
        xAxis.add("OCT");
        xAxis.add("NOV");
        xAxis.add("DEC");
        return xAxis;
    }
    private void setPieChart(int td_cr,int td_fn,int td_pd){
        //chart for the tasks of today
        ArrayList tsk_td = new ArrayList();

        tsk_td.add(new Entry(td_fn, 0));
        tsk_td.add(new Entry(td_pd, 1));

        PieDataSet dataSet = new PieDataSet(tsk_td, "");

        int []cl=new int[2];
        cl[0]=Color.rgb(0,0,200);
        cl[1]=Color.rgb(200,0,0);
        dataSet.setColors(cl);
        ArrayList desc = new ArrayList();

        desc.add("Tasks Finished");
        desc.add("Tasks Pending");

        PieData data = new PieData(desc, dataSet);

        pieChart.setData(data);
        pieChart.setDescription("Today Tasks");
        pieChart.animateXY(1000, 1000);

    }
    private void setPieChart1(){
        //chart for team members
        int total=0 ,av=0 ,unav=0;
        for (TeamMember tm :
                teamMembers) {
            total+=1;
            if(tm.isAvailable()){
                av+=1;
            }
            else {
                unav+=1;
            }
        }
        ArrayList nb_members = new ArrayList();
        nb_members.add(new Entry(av, 0));
        nb_members.add(new Entry(unav, 1));
        PieDataSet dataSet = new PieDataSet(nb_members, "");
        int []cl=new int[2];
        cl[0]=Color.rgb(0,0,200);
        cl[1]=Color.rgb(200,0,0);
        dataSet.setColors(cl);
        ArrayList desc = new ArrayList();
        desc.add("Available");
        desc.add("Unavailable");
        PieData data = new PieData(desc, dataSet);
        if(total!=0){
            pieChart1.setVisibility(View.VISIBLE);
            total_members.setText("Total : "+Integer.toString(total) );
            available_members.setText("Available : "+Integer.toString(av) +"/"+Integer.toString(total)+"  "+Integer.toString(100*av/total)+"%" );
            unavailable_members.setText("Unavailable : "+Integer.toString(unav) +"/"+Integer.toString(total)+"  "+Integer.toString(100*(unav)/total)+"%" );
        }
        else {
            pieChart1.setVisibility(View.INVISIBLE);
        }
        pieChart1.setData(data);
        pieChart1.setDescription("Team Members");
        pieChart1.animateXY(1000, 1000);
    }
    //used to build team member tables and sort them alphabetic
    private class TmData implements Comparable<TmData>{
        public String name;
        public int tot;
        public int compl;
        public TmData(String name, int tot, int compl) {
            this.name = name;
            this.tot = tot;
            this.compl = compl;
        }
        @Override
        public int compareTo(TmData o) {
            return name.compareTo(o.name);
        }
    }


    private void addRow(String name, int tot, int compl, Context c){

        // Create a new TableRow
        TableRow newR = new TableRow(c);
        // Create TextViews for each cell in the new row having name, task (completed/created) and percentage
        TextView nameTV = new TextView(c);
        nameTV.setText(name);
        nameTV.setBackground(drawable);
        nameTV.setGravity(1);
        nameTV.setPadding(4, 4, 4, 4);
        TextView valueTV = new TextView(c);
        valueTV.setText(compl+"/"+tot);
        valueTV.setBackground(drawable);
        valueTV.setGravity(1);
        valueTV.setPadding(4, 4, 4, 4);
        TextView percentTV = new TextView(c);
        if(tot!=0){
            percentTV.setText(Integer.toString(compl*100/tot)+"%");

        }else{
            percentTV.setText("-");

        }
        percentTV.setBackground(drawable);
        percentTV.setGravity(1);
        percentTV.setPadding(4, 4, 4, 4);
        // Add the TextViews to the new TableRow
        newR.addView(nameTV);
        newR.addView(valueTV);
        newR.addView(percentTV);
        // Add the new TableRow to the TableLayout after the existing TableRow
        tableLayout.addView(newR, tableLayout.getChildCount());
    }
}
