package com.example.iotlab3app;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

public class MainActivity extends AppCompatActivity {


    private TextView txv_light;

    private TextView txv_temperature;

    private TextView txv_humidity;

    private TextView luxList;

    private TextView temperatureList;

    private TextView humidityList;
    private MqttAndroidClient client;
    private static final String SERVER_URI = "tcp://test.mosquitto.org:1883";
    private static final String TAG = "MainActivity";

    // Define your topic here
    private static final String TOPIC = "group03/sensorValue";

    private ArrayList<Double> luxValue = new ArrayList<>();

    private ArrayList<Double> temperatureValue = new ArrayList<>();

    private ArrayList<Double> humidityValue = new ArrayList<>();

    //ArrayList<Double> allLuxValue = new ArrayList<>();

    //ArrayList<Double> allTemperatureValue = new ArrayList();

    //innan de gamla värderna raderas, flyttas de över till dessa listor med alla tidigare värden
    //ingen funktionalitet för detta men kan vara framtida ambitioner

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.backlog), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txv_light = (TextView) findViewById(R.id.txv_lightValue);
        txv_temperature = (TextView) findViewById(R.id.txv_temperatureValue);
        txv_humidity = (TextView) findViewById((R.id.txv_humidityValue));

        luxList = (TextView) findViewById(R.id.luxValue);
        temperatureList = (TextView) findViewById(R.id.temperatureValue);
        humidityList = (TextView) findViewById(R.id.humidityValue);

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

                try {
                    JSONObject json = new JSONObject(newMessage);
                    double lux = json.getDouble("lux");
                    double temperature = json.getDouble("temperature");
                    double humidity = json.getDouble("humidity");

                    addValues(lux, temperature, humidity);

                    if(lux > 600d || lux < 0d){
                        String luxText = luxValue.get(0) + "OBS, lux out of safe range";
                        txv_light.setText(luxText);
                    }
                    if(temperature > 24d || temperature < 15d){
                        String temperatureText = temperatureValue.get(0) + "OBS, temperature out of safe range";
                        txv_temperature.setText(temperatureText);
                    }
                    if(humidity > 50 || humidity < 10){
                        String humidityText = humidityValue.get(0) + "OBS, humidity out of safe range";
                        txv_humidity.setText(humidityText);
                    }

                    else {
                        runOnUiThread(() -> {
                            txv_light.setText(String.valueOf(luxValue.get(0)));
                            txv_temperature.setText(String.valueOf(temperatureValue.get(0)));
                            txv_humidity.setText(String.valueOf(humidityValue.get(0)));

                            //för backloggen
                            luxList.setText(String.valueOf(luxValue));
                            temperatureList.setText(String.valueOf(temperatureValue));
                            humidityList.setText(String.valueOf(humidityValue));
                        });
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("Message delivered");
            }
        });
    }

    private void addValues(double lux, double temperature, double humidity){
        luxValue.add(0, lux);

        temperatureValue.add(0, temperature);

        humidityValue.add(0, humidity);

        if (luxValue.size() > 10){
            luxValue.remove(10);
        }
        if (temperatureValue.size() > 10){
            temperatureValue.remove(10);
        }
        if (humidityValue.size() > 10){
            humidityValue.remove(10);
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
