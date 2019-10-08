package it.islandofcode.mybarcodescanner;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import it.islandofcode.mybarcodescanner.it.islandofcode.mybarcodescanner.util.MyHttpClient;
import it.islandofcode.mybarcodescanner.it.islandofcode.mybarcodescanner.util.NetworkOp;

public class ConnectActivity extends AppCompatActivity implements ProcessNetData{

    private final static String NEEDED_PERMISSION = Manifest.permission.CAMERA;

    private static final int REQUEST_ID = 444;
    private BarcodeDetector detector;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextView status;
    private MyHttpClient MHC;
    private String qrcode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);


        surfaceView = findViewById(R.id.surfaceView);
        status = findViewById(R.id.TXT_scan_status);

        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build();

        if(!detector.isOperational()){
            exit("Errore irrecuperabile creando il detector");
            return;
        }

        cameraSource = new CameraSource
                .Builder(this, detector)
                .setAutoFocusEnabled(true)
                .build();

        surfaceView.getHolder().addCallback(
                new SurfaceHolder.Callback() {
                    @Override
                    public void surfaceCreated(SurfaceHolder holder) {
                        activateCamera();
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                        //do nothing
                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder holder) {
                        cameraSource.stop();
                    }
                }
        );

        detector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
                cameraSource.stop();
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> items = detections.getDetectedItems();

                if (items.size() != 0) {

                    /*Log.d("JBIBLIO", "ITEM SCANNED SIZE: " +items.size());

                    for(int i=0; i<items.size(); i++){
                        Log.d("JBIBLIO", "\tBARCODE ["+i+"] => " + items.valueAt(i).displayValue);
                    }*/

                    runOnUiThread(() -> {
                        if(qrcode!=null && !qrcode.isEmpty()){
                            Log.d("JBIBLIO","UN ALTRO! RILASCIO ED ANNULLO!");
                            release();
                            return;
                        }
                        qrcode=items.valueAt(0).displayValue;

                        Log.d("JBIBLIO","RICONOSCIUTO " + qrcode);

                        TextView status = findViewById(R.id.TXT_scan_status);
                        status.setText(qrcode);

                        /*netop = new NetworkOp(ConnectActivity.this);
                        netop.execute(qrcode+"/?connect=me");*/
                        MHC = new MyHttpClient(ConnectActivity.this);
                        MHC.execute(qrcode+"/?connect=123");

                    });
                }//fine if
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if(requestCode == REQUEST_ID){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                activateCamera();

            } else {

                exit("Permesso di accesso alla fotocamera negato, esco.");
            }
        }

    }

    private void activateCamera() {

        if (ActivityCompat.checkSelfPermission(this, NEEDED_PERMISSION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, NEEDED_PERMISSION)) {
                new AlertDialog.Builder(this)
                        .setMessage("Quest'app utilizza la fotocamera per rilevare codici a barre. Per poterla usare, è fondamentale condere i permessi relativi. ")
                        .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(ConnectActivity.this,
                                new String[]{NEEDED_PERMISSION}, REQUEST_ID))
                        .show();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{NEEDED_PERMISSION}, REQUEST_ID);

            }
        } else {

            try {
                cameraSource.start(surfaceView.getHolder());
            } catch (IOException e) {
                exit("Impossibile avviare la fotocamera!");
            }
        }

    }

    private void exit(String msg) {
        new AlertDialog.Builder(this)
                .setTitle("Impossibile proseguire")
                .setMessage(msg)
                .setPositiveButton("chiudi", (dialogInterface, i) -> finish())
                .show();
    }

    @Override
    public void process(String result) {
        //return data to main

        //TODO da verificare se result sia effettivamente un UUID!
        Intent intent = new Intent();
        intent.putExtra("UUID", result);
        intent.putExtra("URL", qrcode);

        if(result==null){
            status.setText("Errore connessione");
            status.setTextColor(Color.RED);
            Log.d("JBIBLIO","Errore connessione");
            setResult(RESULT_CANCELED,intent);
        } else {
            status.setText(result);
            status.setTextColor(Color.parseColor("#FF388E3C"));
            setResult(RESULT_OK,intent);
            Log.d("JBIBLIO", "Connessione stabilita, ritorno a MainActivity con UUID="+result);
        }

        MHC.cancel(true);
        finish();
    }

   /* @Override
    public void onBackPressed() {
        cameraSource.stop();
        finish();
    }*/
}
