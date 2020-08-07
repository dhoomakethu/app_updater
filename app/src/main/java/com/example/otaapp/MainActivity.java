package com.example.otaapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static Context mContext;
    private UpdateChecker updateChecker;
    private static MainActivity instance;
    public static HashMap<String, String> updateInfo;


    //UI Elements
    Button      buttonUpdate;
    ProgressBar progressBarUpdate;
    ScrollView scrollView;
    LinearLayout linearLayout;
    TextView infoTextView;

    public static MainActivity getInstance(){
        return instance;
    }


    public void updateUI(){
        if (updateInfo == null){
            Log.d("otapkg.MainActivity", "Trying to update UI with out any update Info");
        }
        Intent updateIntent = new Intent(this, InstallApk.class);
        startActivity(updateIntent);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        instance = this;
        String externalFilesDirectory = getExternalFilesDir(null).getAbsolutePath();
        buttonUpdate = findViewById(R.id.updateButton);
        scrollView = findViewById(R.id.updateListScrollView);
        linearLayout = (LinearLayout) findViewById(R.id.updateListLinearLayout);
        infoTextView = findViewById(R.id.editTextTextMultiLine);
        infoTextView.setText(
                "This app observes: " + externalFilesDirectory + " for changes \n " +
                        "Do adb push <apk file> to observed directory to view the update info");
        infoTextView.setEnabled(false);

        updateChecker = new UpdateChecker(mContext, externalFilesDirectory);
        updateChecker.startWatching();


    }


}
