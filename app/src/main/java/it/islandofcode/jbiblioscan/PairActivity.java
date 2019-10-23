package it.islandofcode.jbiblioscan;

import android.Manifest;
import android.content.Intent;
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

public class PairActivity extends AppCompatActivity implements ProcessNetData, ZXingScannerView.ResultHandler{

    private ZXingScannerView mScannerView;
    private static final int REQUEST_ID = 555;
    private final static String NEEDED_PERMISSION = Manifest.permission.CAMERA;

    private MyHttpClient MHC;
    private String qrcode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pair);

        if (ContextCompat.checkSelfPermission(this, NEEDED_PERMISSION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{NEEDED_PERMISSION}, REQUEST_ID);
        }

        mScannerView = new ZXingScannerView(this);
        mScannerView.setAutoFocus(true);
        //Per HUAWEI
        mScannerView.setAspectTolerance(0.5f);

        List<BarcodeFormat> formats = new ArrayList<>();
        formats.add(BarcodeFormat.QR_CODE);
        //formats.add(BarcodeFormat.EAN_13);

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
        //TODO verificare se devo reimpostare anche autofocus, AspectTolerance e formats
    }

    @Override
    public void process(String result) {

        Intent intent = new Intent();
        intent.putExtra("UUID", result);
        intent.putExtra("URL", qrcode);
        if(result==null || result.equals(ProcessNetData.ERROR)){
            Log.d("JBIBLIO","Errore connessione");
            setResult(RESULT_CANCELED,intent);
        } else {
            setResult(RESULT_OK,intent);
            Log.d("JBIBLIO", "Connessione stabilita, ritorno a MainActivity con UUID="+result);
        }

        MHC.cancel(true);
        finish();

        //(new Handler()).postDelayed(this::finish, 1000);
    }

    @Override
    public void handleResult(Result rawResult) {
        qrcode = rawResult.getText();
        Log.v("JBIBLIO", qrcode); // Prints scan results
        Log.v("JBIBLIO", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        beepSound();

        MHC = new MyHttpClient(PairActivity.this);
        MHC.execute(qrcode+"/connect");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if(requestCode == REQUEST_ID){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.checkSelfPermission(this, NEEDED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {

                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, NEEDED_PERMISSION)) {
                        new AlertDialog.Builder(this)
                                .setMessage("Quest'app utilizza la fotocamera per rilevare QrCode. Per poterla usare, è fondamentale condere i permessi relativi. ")
                                .setPositiveButton("Ok", (dialog, which) -> ActivityCompat.requestPermissions(PairActivity.this,
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
