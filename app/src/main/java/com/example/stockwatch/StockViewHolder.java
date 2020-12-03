package com.example.stockwatch;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

public class StockViewHolder extends RecyclerView.ViewHolder {

    TextView sym;
    TextView name;
    TextView stockPrice;
    TextView change;


    public StockViewHolder(@NonNull View view) {
        super(view);
        sym = view.findViewById(R.id.stockSym);
        name = view.findViewById(R.id.company);
        stockPrice = view.findViewById(R.id.price);
        change = view.findViewById(R.id.priceChange);
    }
}
