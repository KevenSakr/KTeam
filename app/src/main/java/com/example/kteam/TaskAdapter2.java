package com.example.kteam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class TaskAdapter2 extends RecyclerView.Adapter<MyViewHolder2> {

    private Context context;
    public ArrayList<MyTask> dataList;
    private OnItemClickListener mListener;
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public TaskAdapter2(Context context, ArrayList<MyTask> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public MyViewHolder2 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item2, parent, false);
        return new MyViewHolder2(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder2 holder, int position) {
        if (dataList.isEmpty()){
            return;
        }
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        holder.recTitle.setText(dataList.get(position).getName());
        holder.recStart.setText("Start: "+formatter.format(dataList.get(position).getStartDate()));
        holder.recEnd.setText("End: "+formatter.format(dataList.get(position).getEndDate()));
        holder.recMembers.setText(String.valueOf(dataList.get(position).getTeamIDList().size()));//(dataList.get(position).isAvailable())?"Available":"Not Available"
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call onItemClick listener with the position of the clicked item
                if (mListener != null) {
                    //mListener.onItemClick(dataList.indexOf(dataList.get(position)));
                    mListener.onItemClick(holder.getAdapterPosition());
                }
            }
        });


    }
    @Override
    public int getItemCount() {
        return dataList.size();
    }
    public void searchDataList(ArrayList<MyTask> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
}

class MyViewHolder2 extends RecyclerView.ViewHolder{
    ImageView recImage;
    TextView recTitle, recMembers, recStart,recEnd;
    CardView recCard;
    public MyViewHolder2(@NonNull View itemView) {
        super(itemView);
        //recImage = itemView.findViewById(R.id.recImage);
        recCard = itemView.findViewById(R.id.recCard);
        recMembers = itemView.findViewById(R.id.recMembers);
        recStart = itemView.findViewById(R.id.recStart);
        recEnd = itemView.findViewById(R.id.recEnd);
        recTitle = itemView.findViewById(R.id.recTitle);
    }
}