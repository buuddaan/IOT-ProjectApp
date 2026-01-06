package com.example.iotlab3app;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button; // ??? Denna är borttagen, vilken knapp?
import android.widget.TextView;
import android.util.Log;

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

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

//EF TEST för att skapa timestamp varje gång ett nytt meddelande från MQTT kommer
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Button buttonBacklog;

    private Button buttonScanQr;
    private TextView txv_light;
    private TextView txv_temperature;
    private TextView txv_humidity;
    private TextView luxList;
    private TextView temperatureList;
    private TextView humidityList;
    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String TAG = "MainActivity";
    public String outOfReferenceMessange = " !*";

    // Define your topic here
    private static final String TOPIC = "appValues"; //Vår topic heter bara appValues, inte group03/appValues

    //EF TEST, listor med alla backlog entries
    public static final ArrayList<Entry> luxBacklogEntries = new ArrayList<>();
    public static final ArrayList<Entry> temperatureBacklogEntries = new ArrayList<>();
    public static final ArrayList<Entry> humidityBacklogEntries = new ArrayList<>();
    private static final int BACKLOG_MAX = 50; //EF TEST om vi vill begränsa backlog (?)


    //private Button scanQrBtn;

    //private TextView errorQr;



    @SuppressLint("MissingInflatedId")
    @Override
    // Körs endast en gång, varje ny start av appen
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        buttonBacklog = findViewById(R.id.btnBacklog);

        buttonBacklog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Backlog.class);
                startActivity(intent);
            }
        });

        buttonScanQr = findViewById(R.id.btnScanQR);

        buttonScanQr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, QrCode.class);
                startActivity(intent);
            }
        });


        //OBS, VÅRA VÄRDEN LIVE UPPDATERAS. DÄRFÖR GÖR INTE UPDATE-KNAPPEN NÅT
        txv_light = (TextView) findViewById(R.id.txv_lightValue);
        txv_temperature = (TextView) findViewById(R.id.txv_temperatureValue);
        txv_humidity = (TextView) findViewById((R.id.txv_humidityValue));

        luxList = (TextView) findViewById(R.id.luxList);
        temperatureList = (TextView) findViewById(R.id.temperatureList);
        humidityList = (TextView) findViewById(R.id.humidityList);

        connect();

        client.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean reconnect, String serverURI) {
                if (reconnect) {
                    System.out.println("Reconnected to : " + serverURI);
                    // Re-subscribe as we lost it due to new session
                    subscribe(TOPIC);
                } else {
                    System.out.println("Connected to: " + serverURI);
                    subscribe(TOPIC);
                }
            }
            @Override
            public void connectionLost(Throwable cause) {
                System.out.println("The Connection was lost.");
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws
                    Exception {
                String newMessage = new String(message.getPayload());
                System.out.println("Incoming message: " + newMessage);
                // (har bekräftat i mqtt.py att det stämmer med json format)
                JSONObject json = new JSONObject(newMessage);
                double lux = json.getDouble("lux");
                double temperature = json.getDouble("temperature");
                double humidity = json.getDouble("humidity");

                //EF TEST skapa timestamp BORDE FLYTTAS TILL MQTT-SCRIPTET I RASPBERRY PI FÖR ATT DEET SKA VA FULLT ASYNKRONT men behåller såhär nu
                String ts = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());
                //EF TEST skapa Entry objekt (outOfRange sätts i entrykonstruktorn i den klassen)
                Entry luxEntry = new Entry(SensorType.LUX, ts, String.valueOf(lux));
                Entry tempEntry = new Entry(SensorType.TEMPERATURE, ts, String.valueOf(temperature));
                Entry humEntry = new Entry(SensorType.HUMIDITY, ts, String.valueOf(humidity));

                addValues(luxEntry, tempEntry, humEntry);

                runOnUiThread(() -> {
                    String luxText=String.valueOf(lux);
                    String temperatureText=String.valueOf(temperature);
                    String humidityText=String.valueOf(humidity);
                    if (luxEntry.outOfRange) {
                        luxText = luxText + outOfReferenceMessange;
                    }
                    if (tempEntry.outOfRange) {
                        temperatureText = temperatureText + outOfReferenceMessange;
                    }
                    if (humEntry.outOfRange) {
                        humidityText = humidityText + outOfReferenceMessange;
                    }
                    txv_light.setText(luxText);
                    txv_temperature.setText(temperatureText);
                    txv_humidity.setText(humidityText);

                    luxList.setText(String.valueOf(luxBacklogEntries));
                    temperatureList.setText(String.valueOf(temperatureBacklogEntries));
                    humidityList.setText(String.valueOf(humidityBacklogEntries));
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Message delivered");
            }
        });


        //scanQrBtn = findViewById(R.id.btnScanQR);

        //scanQrBtn.setOnClickListener(v -> testQrFromBitmap());

        //errorQr = findViewById(R.id.errorMessageMain);

    }
   /*
    private void handleQrResults(String qrText) {
        if (qrText.equals("levaxin")) {
            startActivity(new Intent(MainActivity.this, QrCode.class));
        }

        else {
            errorQr.setText("Wrong QR code, try again!");
        }
    }

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

    */

    private void addValues(Entry lux, Entry temperature, Entry humidity) {
        luxBacklogEntries.add(0, lux);
        temperatureBacklogEntries.add(0, temperature);
        humidityBacklogEntries.add(0, humidity);
        //EF TEST Begränsa backlog-storlek (nu till 50 objekt/lista)
        if (luxBacklogEntries.size() > BACKLOG_MAX) {
            luxBacklogEntries.remove(luxBacklogEntries.size() - 1);
        }
        if (temperatureBacklogEntries.size() > BACKLOG_MAX) {
            temperatureBacklogEntries.remove(temperatureBacklogEntries.size() - 1);
        }
        if (humidityBacklogEntries.size() > BACKLOG_MAX) {
            humidityBacklogEntries.remove(humidityBacklogEntries.size() - 1);
        }
    }

    private void connect(){
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), SERVER_URI, clientId);
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                    System.out.println(TAG + " Success. Connected to " + SERVER_URI);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");
                    System.out.println(TAG + " Oh no! Failed to connect to " + SERVER_URI);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void subscribe(String topicToSubscribe) {
        final String topic = topicToSubscribe;
        int qos = 1;
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    System.out.println("Subscription successful to topic: " + topic);
                }
                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    System.out.println("Failed to subscribe to topic: " + topic);
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}