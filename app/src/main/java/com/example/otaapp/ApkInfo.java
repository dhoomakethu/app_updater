package com.example.otaapp;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import java.util.HashMap;


public class ApkInfo {

    public static HashMap<String, String> updateInfo;

    public static HashMap<String, String> readApk(MainActivity mainActivity, String path){
        Context context = mainActivity.getApplicationContext();
        updateInfo = new HashMap<String, String> ();
        PackageManager pManager = context.getPackageManager();
        PackageInfo pInfo = pManager.getPackageArchiveInfo(path, 0);
        if (pInfo == null){
            Log.e("otapkg.ApkInfo", "Error reading apk file: "+ path);
            return updateInfo;
        }
        String packageVersion = pInfo.versionName;
        String packageName = pInfo.packageName;
        updateInfo.put("packagePath", path);
        updateInfo.put("packageName", packageName);
        updateInfo.put("packageVersion", packageVersion);
        Log.d("otapkg.ApkInfo", "Package Name: " + packageName + " Package Version: "+ packageVersion);
        try{
            PackageInfo currentInfo = pManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            String currentPackageName = currentInfo.packageName;
            String currentPackageVersion = currentInfo.versionName;
            Log.d("otapkg.ApkInfo", "Current Package Name: " + currentPackageName + " Current Package Version: "+ currentPackageVersion);
            updateInfo.put("currentPackageName", currentPackageName);
            updateInfo.put("currentPackageVersion", currentPackageVersion);

        }catch (PackageManager.NameNotFoundException e){
            Log.d("otapkg.ApkInfo", "Package: " + packageName + " Not yet installed!!");
        }
        return updateInfo;


    }

}

