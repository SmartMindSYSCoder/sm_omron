package com.sm.sm_omron;
import android.app.Activity;
import android.content.Context;

import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.Model.OmronPeripheral;
import com.omronhealthcare.OmronConnectivityLibrary.OmronLibrary.OmronUtility.OmronConstants;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OmronManager {


    static HashMap<String, String> device = null;
    static HashMap<String, String> personalSettings = null;
    static Boolean isScan = false;
    static OmronPeripheral mSelectedPeripheral;
    static ArrayList<Integer> selectedUsers = new ArrayList<>();

    static List<OmronPeripheral> mPeripheralList = new ArrayList<OmronPeripheral>();

    private final   Activity activity;
    private final   Context applicationContext;

    OmronManager(Activity activity, Context applicationContext){

        this.activity=activity;
        this.applicationContext =applicationContext;
    };







}
