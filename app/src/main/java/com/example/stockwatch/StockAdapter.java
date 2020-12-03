package com.example.stockwatch;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class StockAdapter extends RecyclerView.Adapter<StockViewHolder> {
    private ArrayList<Stock> stockList;
    private MainActivity mainActivity;

    public StockAdapter(ArrayList<Stock> stockList, MainActivity mainActivity) {
        this.stockList = stockList;
        this.mainActivity = mainActivity;
    }

    @NonNull
    @Override
    public StockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_view_holder, parent, false);
        itemView.setOnClickListener(mainActivity); //how to open notes with short click
        itemView.setOnLongClickListener(mainActivity); //how to prompt deletion of notes
        return new StockViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull StockViewHolder holder, int position) {
        Stock selection = stockList.get(position);
        int color =  Color.parseColor("#ff99cc00");
        String arrow = "\u25B2";
        if (selection.getChange() < 0){
            color = Color.parseColor("#ffcc0000");
            arrow = "\u25BC";
        }

        holder.sym.setText(selection.getSymbol());
        holder.sym.setTextColor(color);

        holder.name.setText(selection.getName());
        holder.name.setTextColor(color);

        holder.change.setText(String.format("%s %.2f (%.2f", arrow, (float) selection.getChange(), (float) selection.getChangePct()) + "%)");
        holder.change.setTextColor(color);

        holder.stockPrice.setText(String.format("%.2f", (float) selection.getPrice()));
        holder.stockPrice.setTextColor(color);

    }

    @Override
    public int getItemCount() {
        return stockList.size();
    }

}
