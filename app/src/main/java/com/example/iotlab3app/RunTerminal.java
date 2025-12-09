package com.example.iotlab3app;

import android.os.Bundle;
import android.os.AsyncTask;
import com.google.android.material.snackbar.Snackbar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import android.view.View;
import android.widget.Switch;
import android.widget.CompoundButton;
import android.widget.Button;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import android.os.StrictMode;
import android.view.View;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.iotlabapp.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private volatile String lastOutput = "";
    TextView txv_temp_indoor = null;
    Switch lightToggle = null;
    Button btnUpdateTemp = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txv_temp_indoor = (TextView) findViewById(R.id.indoorTempShow);
        txv_temp_indoor.setText("The fetched temp indoor");
        lightToggle = (Switch) findViewById(R.id.btnToggle);
        lightToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
// below you write code to change switch status and action to take
                if (isChecked) {
//do something if checked
                    new AsyncTask<Integer, Void, Void>() {
                        @Override
                        protected Void doInBackground(Integer... params) {
                            run("python turnondevices.py");
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void r) {

                            lightToggle.setText("ON");

                        }
                    }.execute(1);
                } else {
// to do something if not checked
                    new AsyncTask<Integer, Void, Void>() {
                        @Override
                        protected Void doInBackground(Integer... params) {
                            run("python turnoffdevices.py");
                            return null;
                        }
                        @Override
                        protected void onPostExecute(Void r) {
                            lightToggle.setText("OFF");
                        }
                    }.execute(1);
                }}
        });
        btnUpdateTemp = (Button) findViewById(R.id.btnUpdateTemp);
        btnUpdateTemp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Add code to execute on click
                new AsyncTask<Integer, Void, Void>() {
                    @Override
                    protected Void doInBackground(Integer... params) {
                        run("python listsensors.py");
                        return null;
                    }
                    @Override
                    protected void onPostExecute(Void r) {

                        txv_temp_indoor.setText(lastOutput.trim());


                    }
                }.execute(1);
            }
        });

    }

    public void run (String command) {
        String hostname = "RPI_IP_ADDRESS";
        String username = "RPI_USERNAME";
        String password = "RPI_PASSWORD";
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder() .permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try
        {
            Connection conn = new Connection(hostname); //init connection
            conn.connect(); //start connection to the hostname
            boolean isAuthenticated = conn.authenticateWithPassword(username,password);
            if (isAuthenticated == false)
                throw new IOException("Authentication failed.");
            Session sess = conn.openSession();
            sess.execCommand(command + " 2>&1");
            InputStream stdout = new StreamGobbler(sess.getStdout());
            BufferedReader br = new BufferedReader(new InputStreamReader(stdout));
            lastOutput = "";
//reads text
            while (true){
                String line = br.readLine(); // read line
                if (line == null)
                    break;
                System.out.println(line);
                lastOutput += line + "\n";
            }
            /* Show exit status, if available (otherwise "null") */
            System.out.println("ExitCode: " + sess.getExitStatus());
            sess.close(); // Close this session
            conn.close();
        }
        catch (IOException e)
        { e.printStackTrace(System.err);
            System.exit(2);

            //return;
        }
    }

}