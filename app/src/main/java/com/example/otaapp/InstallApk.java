/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.otaapp;

// Need the following import to get access to the app resources, since this
// class is in a sub-package.

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInstaller;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;


/**
 * Demonstration of package installation and uninstallation using the package installer Session
 * API.
 *
 *
 */
public class InstallApk extends Activity {
    private static final String PACKAGE_INSTALLED_ACTION =
            "com.example.otaapp.SESSION_API_PACKAGE_INSTALLED";
    private static Context mContext;
    private UpdateChecker updateChecker;
    public static HashMap<String, String> updateInfo;

    //UI Elements
    Button      buttonUpdate;
    ProgressBar progressBarUpdate;
    ScrollView scrollView;
    LinearLayout linearLayout;
    TextView hintTextBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mContext = getApplicationContext();
        buttonUpdate = findViewById(R.id.updateButton);
        scrollView = findViewById(R.id.updateListScrollView);
        hintTextBox = findViewById(R.id.editTextTextMultiLine);
        hintTextBox.setVisibility(View.GONE);
        linearLayout = (LinearLayout) findViewById(R.id.updateListLinearLayout);

        // Watch for button clicks.
        MainActivity mainActivity = MainActivity.getInstance();
        updateInfo = mainActivity.updateInfo;
        LinearLayout row = new LinearLayout(this);
        TextView tv = new TextView(this);
        String currentVersion = mainActivity.updateInfo.get("currentPackageVersion");
        String newVersion = mainActivity.updateInfo.get("packageVersion");
        SpannableString apkName = new SpannableString(mainActivity.updateInfo.get("packageName") + "\n");
        SpannableString apkUpdateInfo = new SpannableString("current: "+ currentVersion +"\tavailable: " + newVersion + "\n");
        apkName.setSpan(new StyleSpan(Typeface.BOLD), 0, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        apkUpdateInfo.setSpan(new RelativeSizeSpan(0.75f), 0, apkUpdateInfo.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(apkName);
        builder.append(apkUpdateInfo);
        tv.setText(builder);
        row.addView(tv);
        linearLayout.addView(row);

        buttonUpdate.setEnabled(true);

        buttonUpdate.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PackageInstaller.Session session = null;
                try {
                    String apkPath = updateInfo.get("packagePath");
                    PackageInstaller packageInstaller = getPackageManager().getPackageInstaller();
                    PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                            PackageInstaller.SessionParams.MODE_FULL_INSTALL);
                    int sessionId = packageInstaller.createSession(params);
                    session = packageInstaller.openSession(sessionId);
                    addApkToInstallSession(updateInfo.get("packagePath"), session);
//                    addApkToInstallSession("HelloActivity.apk", session);

                    // Create an install status receiver.
                    Context context = InstallApk.this;
                    Intent intent = new Intent(context, InstallApk.class);
                    intent.setAction(PACKAGE_INSTALLED_ACTION);
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                    IntentSender statusReceiver = pendingIntent.getIntentSender();

                    // Commit the session (this will start the installation workflow).
                    session.commit(statusReceiver);
                } catch (IOException e) {
                    throw new RuntimeException("Couldn't install package", e);
                } catch (RuntimeException e) {
                    if (session != null) {
                        session.abandon();
                    }
                    throw e;
                }
            }
        });
    }

    private void addApkToInstallSession(String assetName, PackageInstaller.Session session)
            throws IOException {
        // It's recommended to pass the file size to openWrite(). Otherwise installation may fail
        // if the disk is almost full.
        File apkFile = new File(assetName);
        OutputStream packageInSession = session.openWrite("package", 0, -1);
        InputStream is = new FileInputStream(apkFile);
        byte[] buffer = new byte[(int)apkFile.length()];
        int n;
        while ((n = is.read(buffer)) >= 0) {
            packageInSession.write(buffer, 0, n);
        }
        is.close();
        packageInSession.close();
    }

    // Note: this Activity must run in singleTop launchMode for it to be able to receive the intent
    // in onNewIntent().
    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        if (PACKAGE_INSTALLED_ACTION.equals(intent.getAction())) {
            int status = extras.getInt(PackageInstaller.EXTRA_STATUS);
            String message = extras.getString(PackageInstaller.EXTRA_STATUS_MESSAGE);

            switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    // This test app isn't privileged, so the user has to confirm the install.
                    Intent confirmIntent = (Intent) extras.get(Intent.EXTRA_INTENT);
                    startActivity(confirmIntent);
                    break;

                case PackageInstaller.STATUS_SUCCESS:
                    Toast.makeText(this, "Install succeeded!", Toast.LENGTH_SHORT).show();
                    break;

                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
                    Toast.makeText(this, "Install failed! " + status + ", " + message,
                            Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(this, "Unrecognized status received from installer: " + status,
                            Toast.LENGTH_SHORT).show();
            }
        }
    }
}
