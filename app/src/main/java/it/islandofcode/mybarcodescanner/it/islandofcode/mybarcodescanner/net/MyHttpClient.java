package it.islandofcode.mybarcodescanner.it.islandofcode.mybarcodescanner.net;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.Serializable;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyHttpClient  extends AsyncTask<String, Void, String> implements Serializable {
    private ProcessNetData dataProcessor;

    public static final String DISCONNECTION = "BYE";
    public static final String PONG = "PONG";
    public static final String ERROR = "ERROR";
    public static final String UNKNOW = "UNKNOW";

    public MyHttpClient(ProcessNetData PND){
        this.dataProcessor = PND;
    }

    @Override
    protected String doInBackground(String... strings) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .connectTimeout(1500, TimeUnit.MILLISECONDS)
                .readTimeout(1500, TimeUnit.MILLISECONDS)
                .writeTimeout(1500, TimeUnit.MILLISECONDS)
                .build();

        Request request = new Request.Builder()
                .url(strings[0])
                .build();
        Response response;
        try {
            response = client.newCall(request).execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    @Override
    protected void onPostExecute(String s) {
        if(s==null || s.isEmpty()){
            Log.e("JBIBLIO", "Stringa NULLA/VUOTA di ritorno dall'operazione di rete");
        }
        this.dataProcessor.process(s);
    }
}
