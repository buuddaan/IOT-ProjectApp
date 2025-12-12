package com.example.iotlab3app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


//EF TEST importer
import android.widget.TextView;
import java.util.ArrayList;


public class Backlog extends AppCompatActivity {
    //EF TEST x 3
    private TextView luxList;
    private TextView temperatureList;
    private TextView humidityList;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_backlog);
        //EF TEST hämta id från backlog2
        luxList = findViewById(R.id.luxList);
        temperatureList = findViewById(R.id.temperatureList);
        humidityList = findViewById(R.id.humidityList);

        luxList.setText(buildBacklogText(MainActivity.luxBacklogEntries));
        temperatureList.setText(buildBacklogText(MainActivity.temperatureBacklogEntries));
        humidityList.setText(buildBacklogText(MainActivity.humidityBacklogEntries));
        //EF TEST slut för stycket


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.backlog), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        button = findViewById(R.id.btnMainPage);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Backlog.this, MainActivity.class);
                startActivity(intent);
            }
        });

    }
    //EF TEST
    private String buildBacklogText(ArrayList<Entry> entries) {
        StringBuilder sb = new StringBuilder();

        for (Entry e : entries) {
            sb.append(e.timeStamp)
                    .append("  ")
                    .append(e.value);

            if (e.outOfRange) {
                sb.append(" !");
            }

            sb.append("\n");
        }

        return sb.toString();
    }



}