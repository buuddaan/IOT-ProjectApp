package com.example.iotlab3app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
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

        scanQrBtn.setOnClickListener(v -> testQrFromBitmap());

        errorQr = findViewById(R.id.errorMessage);

    }

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(), result -> {
        if (result.getContents() != null) {
            String qrText = result.getContents();
            handleQrResults(qrText);
        }
    });

    //Om man hade kunnat testa koden med en riktig kamera
    private void starQrScanner() {
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

    //Ritar upp en qr kod istället som scanner använder sig av
    private void testQrFromBitmap() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.levaxin);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        try {
            QRCodeReader reader = new QRCodeReader();
            Result result = reader.decode(binaryBitmap);
            handleQrResults(result.getText());
        } catch (Exception e) {
            errorQr.setText("Could not read QR from image");
            e.printStackTrace();
        }
    }

}