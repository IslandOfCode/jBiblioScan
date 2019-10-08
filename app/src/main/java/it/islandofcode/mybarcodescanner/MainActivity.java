package it.islandofcode.mybarcodescanner;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import it.islandofcode.mybarcodescanner.it.islandofcode.mybarcodescanner.util.NetworkOp;

public class MainActivity extends AppCompatActivity implements ProcessNetData{

    //private final static String NEEDED_PERMISSION = Manifest.permission.INTERNET;

    //private static final int REQUEST_ID = 444;

    private NetworkOp netop = null;
    private String UUID;
    private String URL;
    private boolean CONNECTION_ACTIVE = false;

    public void goToTheWebsite(View view){
        String url = "http://www.islandofcode.it";
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = null;
        if (connectivityManager != null) {
            networkInfo = connectivityManager.getActiveNetworkInfo();
        }

        if (networkInfo == null || !networkInfo.isConnected() ||
                (networkInfo.getType() != ConnectivityManager.TYPE_WIFI)) {

            showMessage("Nessuna connessione WiFi rilevata!");

            Button b_conn = findViewById(R.id.B_connect);
            b_conn.setEnabled(false);
            b_conn.setText(R.string.no_internet);
            //TODO usa un listener per cambiare stato al pulsante
        }

    }

    public void openScanView(View view){
        Intent iscan = new Intent(this, ScanActivity.class);
        iscan.putExtra("UUID", UUID);
        iscan.putExtra("URL", URL);
        startActivity(iscan);
    }

    public void connect(View view){
        if(CONNECTION_ACTIVE){ //sto chiedendo una disconnessione

            netop = new NetworkOp(this);
            netop.execute(URL+"/?disconnect="+UUID);

        } else {
            Intent cscan = new Intent(this, ConnectActivity.class);
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
        CONNECTION_ACTIVE = connected;
        if(connected){
            b_scan.setEnabled(true);
            b_conn.setText(R.string.B_disconnect);
            b_conn.setTextColor(Color.parseColor("#FF388E3C")); //green material
        } else {
            b_scan.setEnabled(false);
            b_conn.setText(R.string.B_connect);
            b_conn.setTextColor(Color.parseColor("#FF1976D2")); //blue material
        }
    }
/*
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //TODO

                } else {

                    //TODO
                }
                return;
            }
        }
    }*/

    @Override
    public void process(String result) {

        if(result == null){
            Log.d("JBIBLIO", "Nessun risultato per la disconnessione");
        } else if(result.equals(NetworkOp.DISCONNECTION)){
            Log.d("JBIBLIO", result);
            setConnectionTextStyle(false);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //connectactivity
        if(requestCode==991){

            if (resultCode==RESULT_OK) {
                //qui mi aspetto un UUID
                this.UUID = data.getStringExtra("UUID");
                this.URL = data.getStringExtra("URL");
                Log.d("JBIBLIO", "Returned " + UUID + " from " + URL + " by ConnectScan");
                setConnectionTextStyle(true);
                return;
            } else if(resultCode==RESULT_CANCELED){
                //ricevuto un errore
                showMessage("Connessione annullata");
                Log.d("JBIBLIO", "Result CANCELED by " + requestCode);
            }
        }

        Log.d("JBIBLIO","Intent code "+requestCode+" return nothing or failure");
    }

    private void showMessage(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Attenzione!")
                .setMessage(msg)
                .setPositiveButton("Ok", null)
                .show();
    }
}
