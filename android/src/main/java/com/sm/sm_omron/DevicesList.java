package com.sm.sm_omron;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.gson.Gson;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.LibraryManager.OmronPeripheralManager;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.flutter.plugin.common.MethodChannel;

public class DevicesList {
    /// variables
    private MethodChannel.Result result;
    private final   Activity activity;
    private final   Context applicationContext;

    DevicesList( Activity activity, Context applicationContext){

        this.activity=activity;
        this.applicationContext =applicationContext;
        this.result =result;
    };




//    private List<HashMap<String, String>> fullDeviceList;

    // methods
    void initializeFun(MethodChannel.Result result) {
        this.result = result;
//       fullDeviceList = new ArrayList<HashMap<String, String>>();
        OmronPeripheralManager.sharedManager(activity).setAPIKey("F8C4D353-1309-41A4-A190-34C1101CC43D", null);
        // Notification Listener for Configuration Availability
        LocalBroadcastManager.getInstance(activity).registerReceiver(mMessageReceiver,
                new IntentFilter(OmronConstants.OMRONBLEConfigDeviceAvailabilityNotification));

    }


    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (mMessageReceiver != null)
                LocalBroadcastManager.getInstance(context).unregisterReceiver(mMessageReceiver);

            // Get extra data included in the Intent
            final int status = intent.getIntExtra(OmronConstants.OMRONConfigurationStatusKey, 0);

            if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileSuccess) {
                Log.d("ddddd", "Config File Extract Success");
                loadDeviceList();
            } else if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileError) {
                Log.d("TAGDDD", "Config File Extract Failure");
            //    result.error("1", "OMRONConfigurationFileError", null);
            } else if (status == OmronConstants.OMRONConfigurationStatus.OMRONConfigurationFileUpdateError) {
                Log.d("TAGdddds", "OMRONConfigurationFileUpdateError");
               // result.error("2", "Config File Update Failure", null);
//                showErrorLoadingDevices();
            }
        }

    };

    private void loadDeviceList() {
        List<HashMap<String, String>>  fullDeviceList = new ArrayList<HashMap<String, String>>();
       // Context ctx = App.getInstance().getApplicationContext();
        if (OmronPeripheralManager.sharedManager(applicationContext).retrieveManagerConfiguration(applicationContext) != null) {
            fullDeviceList = (List<HashMap<String, String>>) OmronPeripheralManager.sharedManager(applicationContext).retrieveManagerConfiguration(applicationContext).get(OmronConstants.OMRONBLEConfigDeviceKey);
          //  Log.d("devicesList", fullDeviceList.toString());

            List<String> jsonList = new ArrayList<String>();

            for (HashMap<String, String> var : fullDeviceList){
               jsonList.add(new Gson().toJson(var));
            }
                result.success(jsonList);
        } else {
            result.error("3", "OMRONConfigurationFileError", null);
            // show error
        }
    }
}
