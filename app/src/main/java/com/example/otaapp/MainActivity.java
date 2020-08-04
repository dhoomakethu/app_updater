package com.example.otaapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.ScriptGroup;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.provider.CalendarContract.CalendarCache.URI;

// Checks a given folder for updates
// version.txt = contains single liner for version information e.g. 4.0.0.1
// packages
//

public class MainActivity extends AppCompatActivity {

    //constants
    static final int updateCheckInterval = 10000; //in ms
    static final String otapkgFolder    = "/mnt/sdcard/otapkg/";
    static final String versionFile     = "version.txt";
    static final String packages[]      = { "HMI-Core", "HMI-App"};
    Boolean updateAvailable             = false;
    String currentVersion               = "4.0.0.0";
    String newVersion                   = "NA";

    //UI Elements
    TextView    textViewCurrentVersion;
    TextView    textViewNewVersion;
    Button      buttonUpdate;
    ProgressBar progressBarUpdate;

    // setup the timer thread to monitor the update folder
    final Handler updateHandler = new Handler();

    Runnable updateHandlerRunnable = new Runnable() {
        @Override
        public void run() {

            Log.i("Update Check:", "Checking folder for update");


            try {
                File fileInput = new File(otapkgFolder + versionFile);
                if (fileInput != null) {
                    BufferedReader bufferedReader = new BufferedReader(new FileReader(fileInput));
                    String line;
                    try {
                        line = bufferedReader.readLine();
                        //check if there's a new version
                        if ((!line.isEmpty()) && (line.compareTo(currentVersion) != 0)) {
                            setUpdateAvailable(line);
                        }

                    } catch (Exception e) {
                        Log.e("Update Check:", e.getMessage());
                    }
                    bufferedReader.close();
                }
            } catch (Exception e) {
                Log.e("Update Check:", e.getMessage());
            }

            //post this only if we didn't find any update
            if (!isUpdateAvailable ())
                updateHandler.postDelayed(this, updateCheckInterval);
        }
    };

    protected Boolean isUpdateAvailable() {
        return updateAvailable;
    }
    protected void setUpdateAvailable(String otaVersion) {
        updateAvailable = true;
        newVersion = otaVersion;
        textViewNewVersion.setText(newVersion);
        buttonUpdate.setClickable(true);
    }

    protected void resetUpdateAvailable() {
        updateAvailable = false;
    }

    public void onUpdateButtonSWUpdate (View view){
        Log.i("Update:", "Update invoked");

        //check through all the files
        File otapkgDirectory = new File(otapkgFolder);
        File[] files = otapkgDirectory.listFiles();

        for (int i = 0; i < files.length; i++) {
            //check for APKs
            if (files[i].getName().contains("apk")) {
                updateAPK(otapkgFolder, files[i].getName());
            }
        }
        //as cleanup post update
    }

    public void updateAPK (String directory, String apkName) {
        String filePath = "file:///" + directory + apkName;

        //Intent intent = new Intent(Intent.ACTION_VIEW)
        //        .setDataAndType(URI.parse(filePath), "application/vnd.android.package-archive");
        //startActivity(intent);

        OTAInstaller otaInstaller = new OTAInstaller(newVersion);
        otaInstaller.installViaPackageManager(getApplicationContext(), directory, apkName);
        //otaInstaller.installViaOSCmd(directory, apkName);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewCurrentVersion = findViewById(R.id.currentVersion);
        textViewNewVersion = findViewById(R.id.newVersion);
        buttonUpdate = findViewById(R.id.updateButton);
        progressBarUpdate = findViewById(R.id.progressBar);

        buttonUpdate.setClickable(false);
        progressBarUpdate.setEnabled(false);
        textViewCurrentVersion.setText(currentVersion);
        textViewNewVersion.setText(newVersion);

        updateHandlerRunnable.run();
        Intent receivedIntent = getIntent();
    }


}
