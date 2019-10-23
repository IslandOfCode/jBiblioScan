package it.islandofcode.jbiblioscan;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import it.islandofcode.jbiblioscan.net.MyHttpClient;
import it.islandofcode.jbiblioscan.net.ProcessNetData;
import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class IsbnScanActivity extends AppCompatActivity implements ProcessNetData, ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    private static final int REQUEST_ID = 444;
    private final static String NEEDED_PERMISSION = Manifest.permission.CAMERA;

    private MyHttpClient MHC;

    private String UUID;
    private String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_isbn_scan);

        if (ContextCompat.checkSelfPermission(this, NEEDED_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{NEEDED_PERMISSION}, REQUEST_ID);
        }


        this.URL = getIntent().getStringExtra("URL");
        this.UUID = getIntent().getStringExtra("UUID");

        mScannerView = new ZXingScannerView(this);
        mScannerView.setAutoFocus(true);
        //Per HUAWEI
        mScannerView.setAspectTolerance(0.5f);

        List<BarcodeFormat> formats = new ArrayList<>();
        //formats.add(BarcodeFormat.QR_CODE);
        formats.add(BarcodeFormat.EAN_13);

        mScannerView.setFormats(formats);
        setContentView(mScannerView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
        //TODO verificare se devi reimpostare anche autofocus, AspectTolerance e formats
    }

    @Override
    public void process(String result) {
        if (result == null || !result.trim().equals(ProcessNetData.RECEIVED)) {
            Log.d("JBIBLIO", "ISBN NON RICEVUTO, risposta [" + result + "]");
            new AlertDialog.Builder(this)
                    .setTitle("ERRORE!")
                    .setMessage("Il server non ha confermato la ricezione!")
                    .setPositiveButton("chiudi", (dialogInterface, i) -> finish())
                    .show();
        } else if(result != null && result.trim().equals(ProcessNetData.UNKNOW)) {
            Log.d("JBIBLIO", "SERVER NON HA RICONOSCIUTO QUESTO DEVICE!");
            new AlertDialog.Builder(this)
                    .setTitle("ERRORE!")
                    .setMessage("Il server non ha riconosciuto questo dispositivo.")
                    .setPositiveButton("OK", (dialogInterface, i) -> finish())
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("ISBN ricevuto!")
                    .setMessage("Vuoi scansionare un altro codice a barre?")
                    .setPositiveButton("Si", (dialogInterface, i) -> mScannerView.resumeCameraPreview(this))
                    .setNegativeButton("No", (dialogInterface, i) -> finish())
                    .show();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        String barcode = rawResult.getText();
        Log.v("JBIBLIO", barcode); // Prints scan results
        Log.v("JBIBLIO", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        beepSound();

        //TODO dovrebbe essere inutile, poichè già concludo la connessione prima di aprirne un'altra
        if(MHC!=null && !MHC.isCancelled()){
            MHC.cancel(true);
        }

        MHC = new MyHttpClient(IsbnScanActivity.this);
        MHC.execute(URL+"/isbn/"+ barcode +"/"+UUID);

        //(new Handler()).postDelayed(this::finish, 1000);

        /* //Per java precedente a 8, utile pure per lanciare più cose contemporaneamente
        Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    // yourMethod();
                }
            }, 5000);   //5 seconds
         */


        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if(requestCode == REQUEST_ID){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, NEEDED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, NEEDED_PERMISSION)) {
                        new AlertDialog.Builder(this)
                                .setMessage("Quest'app utilizza la fotocamera per rilevare codici a barre. Per poterla usare, è fondamentale condere i permessi relativi. ")
                                .setPositiveButton("Ok", (dialog, which) -> ActivityCompat.requestPermissions(IsbnScanActivity.this,
                                        new String[]{NEEDED_PERMISSION}, REQUEST_ID))
                                .show();
                    } else {
                        ActivityCompat.requestPermissions(this, new String[]{NEEDED_PERMISSION}, REQUEST_ID);

                    }
                } else {
                    //Camera attiva, ma non so come verificarlo!
                    Log.d("JBIBLIO", "PERMESSI CONCESSI, CAMERA ATTIVA");
                }

            } else {

                new AlertDialog.Builder(this)
                        .setTitle("Impossibile proseguire")
                        .setMessage("Permesso di accesso alla fotocamera negato, esco.")
                        .setPositiveButton("chiudi", (dialogInterface, i) -> finish())
                        .show();
                //exit("Permesso di accesso alla fotocamera negato, esco.");
            }
        }

    }

    protected void beepSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
