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

public class TaskAdapter extends RecyclerView.Adapter<MyViewHolder1> {

    private Context context;
    public ArrayList<MyTask> dataList;
    private OnItemClickListener mListener;
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public TaskAdapter(Context context, ArrayList<MyTask> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public MyViewHolder1 onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item1, parent, false);
        return new MyViewHolder1(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder1 holder, int position) {
        if (dataList.isEmpty()){
            return;
        }

        holder.recTitle.setText(dataList.get(position).getName());
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        holder.recStart.setText("Start: "+formatter.format(dataList.get(position).getStartDate()));
        holder.recMembers.setText(String.valueOf(dataList.get(position).getTeamIDList().size()));//(dataList.get(position).isAvailable())?"Available":"Not Available"
        holder.recCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Call onItemClick listener with the position of the clicked item
                if (mListener != null) {
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

class MyViewHolder1 extends RecyclerView.ViewHolder{
    ImageView recImage;
    TextView recTitle, recMembers, recStart;
    CardView recCard;
    public MyViewHolder1(@NonNull View itemView) {
        super(itemView);
        //recImage = itemView.findViewById(R.id.recImage);
        recCard = itemView.findViewById(R.id.recCard);
        recMembers = itemView.findViewById(R.id.recMembers);
        recStart = itemView.findViewById(R.id.recStart);
        recTitle = itemView.findViewById(R.id.recTitle);
    }
}