package com.example.otaapp;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public class OTAInstaller {
    private static final String PACKAGE_INSTALLED_ACTION =
            "com.example.otaapp.SESSION_API_PACKAGE_INSTALLED";
    String version;

    public OTAInstaller(String version) {
        this.version = version;
        Log.i("OTA Installer", "Installer initialized, version to be installed: " + version);
    }

    public void installViaPackageManager(Context context, String directory, String apkName) {
        PackageInstaller pkgInstaller = context.getPackageManager().getPackageInstaller();
        PackageInstaller.SessionParams sessionParam = new PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL);

        try {
            int sessionId;

            sessionParam.setAppPackageName(apkName);
            sessionId = pkgInstaller.createSession(sessionParam);
            PackageInstaller.Session session = pkgInstaller.openSession(sessionId);

            //package manager session, open a handle, we can write to.
            OutputStream outputStream = session.openWrite(apkName, 0, -1);

            //Read apk file content from the directory
            File file = new File(directory + apkName);
            FileInputStream fileInputStream = new FileInputStream(file);
            //for the lack of better code examples :)
            byte apkRawContent[] = new byte[(int) file.length()];
            int numReadBytes = fileInputStream.read(apkRawContent);

            Log.i("OTAInstaller", "File Length:" + file.length() + " Read: " + numReadBytes);

            //apk content read, now copy to the outputstream
            outputStream.write(apkRawContent, 0, numReadBytes);

            //write to disk
            outputStream.flush();
            session.fsync(outputStream);

            //cleanup
            outputStream.close();
            fileInputStream.close();
            String[] apkNames = session.getNames();
            for (int i = 0; i < apkNames.length; i++) {
                Log.i("OTAInstaller Session", apkNames[i]);
            }

            session.commit(PendingIntent.getBroadcast(context, sessionId,
                    new Intent("android.intent.action.MAIN"), 0).getIntentSender());
            session.close();
            Log.i("OTAInstaller Session", "Session committed and closed");

        } catch (Exception e) {
            Log.e("OTA Installer", e.getMessage());
        }
    }

    public void installViaOSCmd(String directory, String apkName) {
        try {
            final String command = "pm install -r " + directory + apkName;
            Process apkInstallProc = Runtime.getRuntime().exec(new String[]{
                    "su",
                    "-c",
                    command
            });
            apkInstallProc.waitFor();
            Log.i("OTAInstaller", "Installed package:" + apkName);
        } catch (Exception e) {
            Log.i("OTAInstaller", "Failed to install package:" + apkName);
            e.printStackTrace();
        }
    }
}