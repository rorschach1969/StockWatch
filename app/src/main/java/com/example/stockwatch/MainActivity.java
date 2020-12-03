package com.example.stockwatch;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener{

    private static final String marketWatch = "http://www.marketwatch.com/investing/stock/";
    private ArrayList<Stock> stkList = new ArrayList<>();
    private RecyclerView recyclerView;
    private StockAdapter stockAdapter;
    private NameDownloaderAsyncTask hash;
    private int position;
    private String added;
    private SwipeRefreshLayout swipelayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Stock Watch");
        setContentView(R.layout.activity_main);
        swipelayout = findViewById(R.id.swipeRefresh);
        swipelayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (networkStatus()) {
                    stkList.clear();
                    ArrayList<Stock> tmp = readSaveStocks();
                    if (tmp.size() > 0) {
                        new NameDownloaderAsyncTask(MainActivity.this).execute(tmp);
                    }
                }
                else{
                    networkError("Updated");
                }
                swipelayout.setRefreshing(false);
            }
        });
        if (!networkStatus()){
            stkList = readSaveStocks();
            setDefaultStocks();
            networkError("Updated");
        }
        else {
            hash = new NameDownloaderAsyncTask(this);
            new NameDownloaderAsyncTask(this).execute(readSaveStocks());
        }
        recyclerView = findViewById(R.id.recyclerView);
        stockAdapter = new StockAdapter(stkList, this);
        recyclerView.setAdapter(stockAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected  void onPause() {
        try{
            saveNotes();
        }catch (IOException | JSONException e){
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.addStock:
                addStock();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }


    private void setDefaultStocks(){
        for (int i = 0; i<stkList.size();i++){
            String sym = stkList.get(i).getSymbol();
            String name = stkList.get(i).getName();
            Stock deft = new Stock(sym, name, 0, 0, 0);
            stkList.remove(i);
            stkList.add(0, deft);
        }
        Collections.sort(stkList);
        try {
            saveNotes();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean networkStatus(){
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null){
            return false;
        }
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected()){
            return true;
        }
        else
            return false;
    }

    public void addStock() {
        if (networkStatus()) {
            new NameDownloaderAsyncTask(this).execute(readSaveStocks());
            hash = new NameDownloaderAsyncTask(this);
            LayoutInflater inflater = LayoutInflater.from(this);
            final View add = inflater.inflate(R.layout.add_dialogue, null);
            EditText edt = add.findViewById(R.id.symText);
            edt.setFilters(new InputFilter[]{new InputFilter.AllCaps()});
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Stock Selection");
            builder.setMessage("Please enter a Stock Symbol");
            builder.setView(add);
            builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    EditText sym = add.findViewById(R.id.symText);
                    ArrayList<String> match = hash.addStock(sym.getText().toString());
                    if (match.get(0).equals("StockNotFound"))
                        stockNotFound(sym.getText().toString());
                    else if (match.size() > 1) selectFromStockList(match);
                    else {
                        String[] stock = match.get(0).split("-");
                        new StockDownloaderAsyncTask(MainActivity.this).execute(stock[0], "true");
                    }
                }
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    return;
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        else{
            networkError("Added");
        }
    }

    private void selectFromStockList(final ArrayList<String> matched){
        final CharSequence[] sArray = new CharSequence[matched.size()];
        for (int i = 0; i<matched.size(); i++){
            sArray[i] = matched.get(i);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Make a selection");

        builder.setItems(sArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                added = sArray[which].toString();
                String[] stock = added.split("-");
                new StockDownloaderAsyncTask(MainActivity.this).execute(stock[0], "true");
            }
        });

        builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void duplicateStock(String symbol){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_warning_black_48);
        builder.setTitle("Duplicate Stock");
        builder.setMessage("Stocks Symbol " + symbol + " is already displayed");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void stockNotFound(String symbol){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Symbol Not Found: " + symbol);
        builder.setMessage("Data for stock symbol");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void networkError(String verb){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No Network Connection");
        builder.setMessage("Stocks Cannot Be " + verb + " Without A Network Connection");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onClick(View v) {
        position = recyclerView.getChildLayoutPosition(v);
        if (stkList.get(position).getSymbol().isEmpty()){
            return;
        }
        Intent i = new Intent(Intent.ACTION_VIEW);
        String url = marketWatch + stkList.get(position).getSymbol();
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    @Override
    public boolean onLongClick(View v) {
        position = recyclerView.getChildLayoutPosition(v);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setIcon(R.drawable.baseline_delete_black_48);
        builder.setTitle("Delete Stock");
        builder.setMessage("Are you sure you want to delete '" + stkList.get(position).getSymbol() + "'?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                stkList.remove(position);
                try {
                    saveNotes();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stockAdapter.notifyDataSetChanged();
//                checkSize();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                return;
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
        return true;
    }

    public void addStockData(Stock newStock){
        if (newStock == null){
            Toast.makeText(this, "Error accessing Data", Toast.LENGTH_SHORT).show();
            return;
        }
        boolean x = false;
        for (int i =0; i<stkList.size(); i++){
            if (stkList.get(i).getSymbol().equals(newStock.getSymbol())){
                x = true;
                duplicateStock(newStock.getSymbol());
            }
        }
        if (x == false) stkList.add(newStock);
        Collections.sort(stkList);
        try {
            saveNotes();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        stockAdapter.notifyDataSetChanged();
        swipelayout.setRefreshing(false);
    }



    public void stockDataResult(Stock stockData) {
        if (stockData == null){
            Toast.makeText(this, "Error accessing Data", Toast.LENGTH_SHORT).show();
            return;
        }
        stkList.add(stockData);
        try {
            saveNotes();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Collections.sort(stkList);
        stockAdapter.notifyDataSetChanged();
        swipelayout.setRefreshing(false);
    }


    private void saveNotes() throws IOException, JSONException {
        FileOutputStream stockFile = openFileOutput("stockList.txt", Context.MODE_PRIVATE);
        JSONArray jsonArray = new JSONArray();

        for (Stock s : stkList){
            try {
                JSONObject stockJSON = new JSONObject();
                stockJSON.put("symbol", s.getSymbol());
                stockJSON.put("name", s.getName());
                stockJSON.put("latestPrice", s.getPrice());
                stockJSON.put("change", s.getChange());
                stockJSON.put("changePct", s.getChangePct());
                jsonArray.put(stockJSON);
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
        String jsonText = jsonArray.toString();
        stockFile.write(jsonText.getBytes());
        stockFile.close();
    }

    private ArrayList<Stock> readSaveStocks() {
        stkList.clear();
        ArrayList<Stock> returnStocks = new ArrayList<>();
        try {
            InputStream inputStream = openFileInput("stockList.txt");
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                String recieveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while((recieveString = bufferedReader.readLine())!= null){
                    stringBuilder.append(recieveString);
                }
                inputStream.close();
                String jsonText = stringBuilder.toString();

                try {
                    JSONArray jsonArray = new JSONArray(jsonText);
                    for (int i = 0; i<jsonArray.length(); i++){
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String sym = jsonObject.getString("symbol");
                        String name = jsonObject.getString("name");
                        double price = jsonObject.getDouble("latestPrice");
                        double changePoint = jsonObject.getDouble("change");
                        double changePct = jsonObject.getDouble("changePct");
                        Stock s = new Stock(sym, name, price, changePoint, changePct);
                        returnStocks.add(s);
                    }
                    return returnStocks;
                } catch (JSONException e){
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
        return null;
    }
}
