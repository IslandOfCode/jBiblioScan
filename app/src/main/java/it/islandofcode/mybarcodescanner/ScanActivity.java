package it.islandofcode.mybarcodescanner;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
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

public class ScanActivity extends AppCompatActivity implements ProcessNetData{

    private final static String NEEDED_PERMISSION = Manifest.permission.CAMERA;

    private static final int REQUEST_ID = 444;
    private BarcodeDetector detector;
    private SurfaceView surfaceView;
    private CameraSource cameraSource;
    private TextView status;
    private MyHttpClient MHC;

    private String UUID;
    private String URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        this.URL = savedInstanceState.getString("URL");
        this.UUID = savedInstanceState.getString("UUID");

        surfaceView = findViewById(R.id.surfaceView);
        status = findViewById(R.id.TXT_scan_status);

        detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.EAN_13)
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

                if (items.size() != 0)
                    runOnUiThread(() -> {
                        String barcode=items.valueAt(0).displayValue;
                        status.setText(barcode);
                        status.setTextColor(Color.parseColor("#FF388E3C"));

                        MHC = new MyHttpClient(ScanActivity.this);
                        MHC.execute(URL+"/?isbn="+barcode+"&key="+UUID);
                    });
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
                        .setPositiveButton("ok", (dialog, which) -> ActivityCompat.requestPermissions(ScanActivity.this,
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

    public void resendCode(View view){
        //TODO reinvia codice
    }

    public void cleanCode(View view){
        status.setText(R.string.TXT_scan);
    }

    @Override
    public void process(String result) {
        if(result==null){
            new AlertDialog.Builder(this)
                    .setTitle("Attenzione!")
                    .setMessage("Il server non ha confermato la ricezione!")
                    .setPositiveButton("chiudi", null)
                    .show();
        } else if(result.equals("RECEIVED") || result.contains("RECEIVED")){
            status.setText("Ricevuto");
            status.setTextColor(Color.parseColor("#1976D2"));
        }
    }
}
