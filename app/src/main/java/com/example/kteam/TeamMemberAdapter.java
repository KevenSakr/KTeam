package com.example.kteam;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class TeamMemberAdapter extends RecyclerView.Adapter<MyViewHolder> {
//adapter for the recycler view
    private Context context;
    public ArrayList<TeamMember> dataList;
    private OnItemClickListener mListener;
    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }
    public TeamMemberAdapter(Context context, ArrayList<TeamMember> dataList) {
        this.context = context;
        this.dataList = dataList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new MyViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.recName.setText(dataList.get(position).getName());
        holder.recPhone.setText(dataList.get(position).getPhoneNumber());
        holder.recAv.setText((dataList.get(position).isAvailable())?"Available":"Not Available");//(dataList.get(position).isAvailable())?"Available":"Not Available"
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
    public void searchDataList(ArrayList<TeamMember> searchList){
        dataList = searchList;
        notifyDataSetChanged();
    }
}

class MyViewHolder extends RecyclerView.ViewHolder{
    ImageView recImage;
    TextView recName, recPhone, recAv;
    CardView recCard;
    public MyViewHolder(@NonNull View itemView) {
        super(itemView);
        //recImage = itemView.findViewById(R.id.recImage);
        recCard = itemView.findViewById(R.id.recCard);
        recPhone = itemView.findViewById(R.id.recAv);
        recAv = itemView.findViewById(R.id.recPhone1);
        recName = itemView.findViewById(R.id.recTitle);
    }
}