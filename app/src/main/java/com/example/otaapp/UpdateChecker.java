package com.example.otaapp;

import android.os.FileObserver;
import android.content.Context;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.HashMap;


public class UpdateChecker extends FileObserver {

    String updateDirectory;
    Context currentContext;
    MainActivity mainActivity;
    public UpdateChecker(Context context, String path){
        super(path, FileObserver.CREATE|FileObserver.DELETE|FileObserver.MODIFY);
        updateDirectory = path;
        currentContext = context;
        mainActivity = MainActivity.getInstance();
    }

    @Override
    public void onEvent(int event, @Nullable String path) {
        Boolean updateAvailable = false;
        HashMap <String, String> updateInfo = null;
        String apkPath = updateDirectory + "/" + path;
        switch(event){
            case FileObserver.CREATE:
            case FileObserver.MODIFY:
                Log.d("otapkg.UPDATE_DIRECTORY", "File Created/Modified " + updateDirectory + path);
                updateInfo = ApkInfo.readApk(mainActivity, apkPath);
                mainActivity.updateInfo = updateInfo;
                updateAvailable = true;
                break;
            case FileObserver.DELETE:
                Log.d("otapkg.UPDATE_DIRECTORY", "File Deleted " + updateDirectory + path);
                break;
        }
        if (updateAvailable){
            mainActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mainActivity.updateUI();

                }
            });

        }

    }

}
