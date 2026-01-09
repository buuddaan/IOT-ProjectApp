package com.example.iotlab3app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QrCode extends AppCompatActivity {

    private Button scanQrBtn;
    private TextView errorQr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_qrcode);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        scanQrBtn = findViewById(R.id.scanQrBtn);

        scanQrBtn.setOnClickListener(v -> startQrScanner());

        errorQr = findViewById(R.id.errorMessageQr);

    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String qrText = result.getContents();
            handleQrResults(qrText);
        }
    });

    private void startQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Scanna nästa paket");
        options.setBeepEnabled(true);
        options.setOrientationLocked(false);
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);

        barcodeLauncher.launch(options);
    }

    private void handleQrResults(String qrText) {
        if (qrText.equals("levaxin")) {
            startActivity(new Intent(QrCode.this, MainActivity.class));
        }

        else {
            errorQr.setText("Wrong QR code, try again!");
        }
    }
}