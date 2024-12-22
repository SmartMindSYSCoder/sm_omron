package com.sm.sm_omron;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
  private final   Activity activity;
  private final   Context applicationContext;

    PermissionHelper(Activity activity, Context applicationContext){

        this.activity=activity;
        this.applicationContext =applicationContext;
    };


    public void checkPermissions() {

       final int PERMISSIONS_REQUEST_CODE = 1;
        if(Build.VERSION.SDK_INT < 31){
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        PERMISSIONS_REQUEST_CODE);
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{
                                Manifest.permission.BLUETOOTH_SCAN,
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                                , Manifest.permission.ACCESS_FINE_LOCATION
                        },
                        PERMISSIONS_REQUEST_CODE);
            }
        }


    }
    public boolean isPermissionsGranted() {

       final int PERMISSIONS_REQUEST_CODE = 1;
        if(Build.VERSION.SDK_INT < 31){
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return  false;
            }
        }
        else {
            if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
              return  false;
            }
        }
return  true;

    }

    public void checkRecordAudioPermission() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.RECORD_AUDIO}, 1001);
        } else {
            // Permission is granted
        }
    }


    public boolean isRecordAudioPermissionGranted() {
        if (ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
           return  false;
        } else {
            // Permission is granted
return true;        }
    }

}