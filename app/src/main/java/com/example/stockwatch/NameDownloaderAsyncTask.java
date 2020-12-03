package com.example.stockwatch;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;


public class NameDownloaderAsyncTask extends AsyncTask<ArrayList<Stock>, Integer, String> {
    private static HashMap<String, String> symName;
    private static final String symAPI = "https://api.iextrading.com/1.0/ref-data/symbols";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;
    private ArrayList<Stock> myStocks;

    public NameDownloaderAsyncTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        symName = new HashMap<>();
    }

    public ArrayList<String> addStock(String symbol) {
        ArrayList<String> result = new ArrayList<>();
        for (String sym : symName.keySet()){
            String tmp = symName.get(sym);
            if(sym.startsWith(symbol) || tmp.toUpperCase().startsWith(symbol)){
                result.add(sym + "-" + symName.get(sym));
            }
        }
        if (result.isEmpty()) result.add("StockNotFound");
        return result;
    }

    @Override
    protected String doInBackground(ArrayList<Stock>... symbol) {

        myStocks = symbol[0];

        Uri.Builder stockURL = Uri.parse(symAPI).buildUpon();
        String urlFinal = stockURL.build().toString();

        while(stockURL != null){
            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(urlFinal);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() != HttpURLConnection.HTTP_OK){
                    return null;
                }

                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null){
                    sb.append(line).append('\n');
                }

                return sb.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        parseJSON(s);
        for (int i =0; i< myStocks.size(); i++) {
              new StockDownloaderAsyncTask(mainActivity).execute(myStocks.get(i).getSymbol());
        }
        super.onPostExecute(s);
    }

    private void parseJSON(String s) {
        symName.clear();
        try {
            JSONArray jsonArray = new JSONArray(s);

            for (int i=0; i<jsonArray.length();i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String sym = jsonObject.getString("symbol");
                String company = jsonObject.getString("name");
                symName.put(sym, company);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}
