package it.islandofcode.jbiblioscan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import it.islandofcode.jbiblioscan.net.MyHttpClient;
import it.islandofcode.jbiblioscan.net.ProcessNetData;

public class MainActivity extends AppCompatActivity implements ProcessNetData {

    //private final static String NEEDED_PERMISSION = Manifest.permission.INTERNET;

    //private static final int REQUEST_ID = 444;

    private String UUID;
    private String URL;
    private boolean CONNECTION_ACTIVE = false;

    //private GregorianCalendar lastPing;
    private boolean PING_REQUESTED = false;
    //private static final int PING_MINIMUM_WAIT_TIME = 5000; //in millisecondi

    public void goToTheWebsite(View view){
        String url = "http://www.islandofcode.it";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!isNetworkConnected(this.getApplicationContext())){
            showMessage("Nessuna connessione WiFi rilevata!");

            Button b_conn = findViewById(R.id.B_connect);
            b_conn.setEnabled(false);
            b_conn.setText(R.string.no_internet);
            setNetworkCallback(getApplicationContext());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*if(CONNECTION_ACTIVE && URL != null && !URL.isEmpty()){

            if(lastPing==null)
                lastPing = new GregorianCalendar();

            long cmp = (new GregorianCalendar().getTimeInMillis())-lastPing.getTimeInMillis();
            if(cmp>=PING_MINIMUM_WAIT_TIME){
                PING_REQUESTED = true;
                Log.d("JBIBLIO","PING REQUESTED");
                netop = new MyHttpClient(this);
                netop.execute(URL+"/ping");
                lastPing = new GregorianCalendar();
            } else {
                Log.d("JBIBLIO","PING DENIEND, ultimo ping " +cmp+"ms fa.");
            }

        }*/
    }

    public void openScanView(View view){
        Intent iscan = new Intent(this, IsbnScanActivity.class);
        iscan.putExtra("UUID", UUID);
        iscan.putExtra("URL", URL);
        startActivity(iscan);
    }

    public void connect(View view){
        if(CONNECTION_ACTIVE){ //sto chiedendo una disconnessione
            MyHttpClient netop = new MyHttpClient(this);
            netop.execute(URL+"/disconnect/"+UUID);

        } else {
            Intent cscan = new Intent(this, PairActivity.class);
            startActivityForResult(cscan,991);
        }

    }

    /**
     * Modifica pulsanti e testo di stato connessione
     * @param connected Se TRUE, allora l'app è connessa al server, FALSE altrimenti.
     */
    private void setConnectionTextStyle(boolean connected){
        Button b_scan = findViewById(R.id.B_scan);
        Button b_conn = findViewById(R.id.B_connect);
        TextView label = findViewById(R.id.L_connection_status);
        CONNECTION_ACTIVE = connected;
        if(connected){
            b_scan.setEnabled(true);
            b_conn.setText(R.string.B_disconnect);
            b_conn.setTextColor(Color.parseColor("#FF388E3C")); //green material
            label.setText(R.string.MAIN_connected);
        } else {
            b_scan.setEnabled(false);
            b_conn.setText(R.string.B_connect);
            b_conn.setTextColor(Color.parseColor("#FF1976D2")); //blue material
            label.setText(R.string.MAIN_not_connected);
        }
    }

    @Override
    public void process(String result) {
        Log.d("JBIBLIO", "PROCESSED: " + result);
        if(result == null) {
            Log.d("JBIBLIO", "Nessun risultato per la richiesta");
            setConnectionTextStyle(false);
            showMessage("Server offline");
            CONNECTION_ACTIVE = false;
        } else if(PING_REQUESTED && result.contains(ProcessNetData.PONG)){
            setConnectionTextStyle(true);
        } else if(result.equals(ProcessNetData.DISCONNECTION) || result.equals(ProcessNetData.UNKNOW)){
            Log.d("JBIBLIO", "DISCONNECT RESPONSE: " + result);
            setConnectionTextStyle(false);
        /*/} else if(PING_REQUESTED && !result.contains(ProcessNetData.PONG)){
            Log.d("JBIBLIO","PING FAILED" );
            showMessage("Server offline");
            PING_REQUESTED = false;
            setConnectionTextStyle(false);*/
        } else {
            Log.d("JBIBLIO","Failed to process the result from network" );
        }
        PING_REQUESTED = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //TODO rimuovimi, servo solo per debug
        if(data!=null) {
            Log.d("JBIBLIO", "\n\tREQUEST CODE: "+requestCode
                    +"\n\tRESULT CODE: " +resultCode
                    +"\n\tHAS UUID: "+data.hasExtra("UUID")
                    +"\n\tHAS URL: "+data.hasExtra("URL")
            );
        }

        if(requestCode==991){

            if (resultCode==RESULT_OK && data.hasExtra("UUID") && data.hasExtra("URL")) {
                //qui mi aspetto un UUID
                this.UUID = data.getStringExtra("UUID");
                this.URL = data.getStringExtra("URL");
                Log.d("JBIBLIO", "Returned " + UUID + " from " + URL + " by PairScan");
                setConnectionTextStyle(true);
            } else if(resultCode==RESULT_CANCELED){
                //ricevuto un errore
                showMessage("Connessione annullata");
                Log.d("JBIBLIO", "Result CANCELED by " + requestCode);
            }
        } else {
            Log.d("JBIBLIO","Intent code "+requestCode+" return nothing or failure");
            showMessage("Errore nel tentativo di connessione al server remoto.");
        }

    }

    private void showMessage(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Attenzione!")
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .show();
    }

    public static boolean isNetworkConnected(Context context) {
        final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return  (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
        }

        return false;
    }

    private void setNetworkCallback(Context context){
        final ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if(cm != null){
            cm.registerDefaultNetworkCallback(new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    if(CONNECTION_ACTIVE){  //ripristina connessione
                        PING_REQUESTED = true;
                        MyHttpClient net = new MyHttpClient(MainActivity.this);
                        net.execute(URL+"/ping");
                    } else {
                        setConnectionTextStyle(false);
                    }
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    Button b_conn = ((Activity)context).findViewById(R.id.B_connect);
                    b_conn.setEnabled(false);
                    b_conn.setText(R.string.no_internet);
                    showMessage("Connessione alla rete persa!");
                }
            });
        }
    }
}
