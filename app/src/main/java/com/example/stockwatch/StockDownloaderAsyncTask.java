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


public class StockDownloaderAsyncTask extends AsyncTask<String, Void, String> {
    private static final String TAG = "StockDownloaderAsyncTask";
    private static final String nameAPI = "https://cloud.iexapis.com/stable/stock";
    private JSONArray stockResults = new JSONArray();
    private boolean status;
    private static final String TOKEN = "quote?token=<YOUR_API_TOKEN>";
    @SuppressLint("StaticFieldLeak")
    private MainActivity mainActivity;

    public StockDownloaderAsyncTask(MainActivity m){
        this.mainActivity = m;
    }

    @Override
    protected void onPostExecute(String s) {
        ArrayList<Stock> dataList = new ArrayList<>();
        try{
            JSONArray jsonArray = new JSONArray(s);
            for (int i =0; i<jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String sym = jsonObject.getString("symbol");
                String name = jsonObject.getString("companyName");
                double price;
                double changePoint;
                double changePct;
                if (!jsonObject.isNull("latestPrice"))
                    price = jsonObject.getDouble("latestPrice");
                else price = 0.0;
                if (!jsonObject.isNull("change"))
                    changePoint =  jsonObject.getDouble("change");
                else changePoint = 0.0;
                if (!jsonObject.isNull("changePercent"))
                    changePct =  jsonObject.getDouble("changePercent");
                else changePct = 0.0;
                Stock stk = new Stock(sym, name, price, changePoint, changePct);
                dataList.add(stk);
            }
        } catch (JSONException e){
            e.printStackTrace();
        }
        if (status == true)
            mainActivity.stockDataResult(dataList.get(0));
        else
            mainActivity.addStockData(dataList.get(0));

    }

    @Override
    protected String doInBackground(String... symbol){

        String stockSym = symbol[0];
        if (symbol.length > 1){
            status = false;
        }
        else
            status = true;
        Uri.Builder stockURL = Uri.parse(nameAPI).buildUpon();
        stockURL.appendEncodedPath(stockSym).appendEncodedPath(TOKEN);
        String urlFinal = stockURL.build().toString();

            StringBuilder sb = new StringBuilder();
            try {
                URL url = new URL(urlFinal);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                if(conn.getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND){
                    return null;
                }

                conn.setRequestMethod("GET");

                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null){
                    sb.append(line).append('\n');
                }

                parseJSONResults(sb.toString());

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        return stockResults.toString();
    }

    private void parseJSONResults(String s){
        try{
            JSONObject stockInfo = new JSONObject(s);
                stockResults.put(stockInfo);
            } catch (JSONException ex) {
            ex.printStackTrace();
        }

    }


}
