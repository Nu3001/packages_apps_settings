/*
 * Copyright (C) 2010 The Android Open Source Project
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

package com.android.settings;

import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

import com.android.internal.view.RotationPolicy;
import com.android.settings.DreamSettings;

import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.LinkedList;
import android.os.SystemProperties;

import android.widget.Toast;
import android.os.PowerManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
public class DisplaySettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener, OnPreferenceClickListener {
    private static final String TAG = "DisplaySettings";

    /** If there is no setting in the provider, use this. */
    private static final int FALLBACK_SCREEN_TIMEOUT_VALUE = 30000;

    private static final String KEY_SCREEN_TIMEOUT = "screen_timeout";
    private static final String KEY_ACCELEROMETER = "accelerometer";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_NOTIFICATION_PULSE = "notification_pulse";
    private static final String KEY_SCREEN_SAVER = "screensaver";

    private static final int DLG_GLOBAL_CHANGE_WARNING = 1;

    private CheckBoxPreference mAccelerometer;
    private WarnedListPreference mFontSizePref;
    private CheckBoxPreference mNotificationPulse;

    private final Configuration mCurConfig = new Configuration();
    
    private ListPreference mScreenTimeoutPreference;
    private Preference mScreenSaverPreference;

//add lly@rock-chips.com
    private static final String KEY_RESOLUTION = "resolution";
    private ListPreference mResolutionPreference;
    private int mCurrentResolution = 1 ;
    private String mRequestResolution = "";
    private LinkedList<String> mResolutionDpis = new LinkedList<String>();
    private LinkedList<String> mResolutionEntries = new LinkedList<String>();
    private LinkedList<String> mResolutionEntryValues = new LinkedList<String>();
    
    private static final String DUMPSYS_DATA_PATH = "/data/system/";
    private static final String STORE_RESOLUTION_PATH = "storeresolution";

       private boolean applyResolution() {
        // String[] cmd = { "sh", "-c", "echo " + mRequestResolution + " > " +
        // RESOLUTION_FILE };
        /*
         * String cmd = "flash_store " + mRequestResolution; Process ps =
         * Runtime.getRuntime().exec(cmd); Log.d(TAG,"====================CMD "+
         * cmd);
         */
        File storeFile = null;
        FileOutputStream outFileStream = null;
        BufferedOutputStream buffstream = null;
        try {
            storeFile = new File(DUMPSYS_DATA_PATH + STORE_RESOLUTION_PATH + ".bin");
            if (!storeFile.exists()) {
                storeFile.createNewFile();
            }
            outFileStream = new FileOutputStream(storeFile);
            buffstream = new BufferedOutputStream(outFileStream);

            buffstream.write(mRequestResolution.getBytes());

            buffstream.flush();
        } catch (Exception e) {
           
            e.printStackTrace();
            return false;
        } finally {
            if (outFileStream != null) {
                try {
                    outFileStream.close();
                } catch (IOException e) {
                    Log.e(TAG, "failed to close dumpsys output stream");
                }
            }
            if (buffstream != null) {
                try {
                    buffstream.close();
                } catch (IOException e) {
                    Log.e(TAG, "failed to close dumpsys output stream");
                }
            }
        }
        return true;
    }
    private void updateResolution() {
        mCurrentResolution = 0;
        mResolutionEntryValues.clear();
        mResolutionEntries.clear();
        mResolutionDpis.clear();

        mResolutionDpis.add("240");
        mResolutionDpis.add("320");             

        mResolutionEntries.add("1600x1200");
        mResolutionEntries.add("2048x1536");
        mResolutionEntryValues.add("0");
        mResolutionEntryValues.add("1");
	Log.d(TAG, "current resolution:" + mCurrentResolution);
	Log.d(TAG, "mRequestResolution resolution:" + mRequestResolution);
	Log.d(TAG, "entries:" + mResolutionEntries);
	Log.d(TAG, "values:" + mResolutionEntryValues);
	Log.d(TAG, "dpis:" + mResolutionDpis);
        FileInputStream input = null;
        File filename = new File(DUMPSYS_DATA_PATH + STORE_RESOLUTION_PATH + ".bin");
        if(filename.exists()) {
        
          try {
             input = new FileInputStream(filename);
             byte[] buffer = new byte[(int) filename.length()];
             input.read(buffer);
              mCurrentResolution = Integer.parseInt(new String(buffer));
           } catch (IOException e) {
              Log.w(TAG, "Can't read service dump: " , e);
           } finally {
                 if (input != null)
                  try { 
                       input.close();
                   } catch (IOException e) {}

           }
	}else{
          mCurrentResolution = 1;
        }
      //  mResolutionPreference.setEntries(mResolutionEntries.toArray(new CharSequence[mResolutionEntries.size()]));
        mResolutionPreference.setEntryValues(mResolutionEntryValues.toArray(new CharSequence[mResolutionEntryValues.size()]));
        mResolutionPreference.setValueIndex(mCurrentResolution);
        //mResolutionPreference.setSummary(String.format(getResources().getString(R.string.summary_resolution), mResolutionEntries.get(mCurrentResolution)));
        mResolutionPreference.setSummary(String.format(getResources().getString(R.string.summary_resolution), mResolutionPreference.getEntry()));

    }
    private static final int DLG_CONFIRM_REBOOT = 2;
    private void showRebootDialog() {
        removeDialog(DLG_CONFIRM_REBOOT);
        showDialog(DLG_CONFIRM_REBOOT);
    }
    @Override
    public Dialog onCreateDialog(int id) {
        switch (id) {
        case DLG_CONFIRM_REBOOT:
                return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.dlg_confirm_reboot_title)
                    .setPositiveButton(R.string.dlg_ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            if(!applyResolution()) {
                                Toast.makeText(getActivity(),
                                    R.string.set_resolution_failed_message,
                                    Toast.LENGTH_SHORT).show();
                            } else {
                                updateResolution();
                                SystemProperties.set("persist.sys.lcd_density", 
                                    mResolutionDpis.get(mResolutionEntryValues.indexOf(mRequestResolution)));
                                PowerManager pm = (PowerManager) mResolutionPreference.getContext().getSystemService(Context.POWER_SERVICE);
                                pm.reboot("resolution");
                            }
                        }})
                    .setNegativeButton(R.string.cancel, null)
                    .setMessage(R.string.dlg_confirm_reboot_text)
                    .create();
        case DLG_GLOBAL_CHANGE_WARNING:
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_font_change_title,
                    new Runnable() {
                        public void run() {
                            mFontSizePref.click();
                        }
                    });
        
        }
        return super.onCreateDialog(id);
    }

    private final RotationPolicy.RotationPolicyListener mRotationPolicyListener =
            new RotationPolicy.RotationPolicyListener() {
        @Override
        public void onChange() {
            updateAccelerometerRotationCheckbox();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ContentResolver resolver = getActivity().getContentResolver();

        addPreferencesFromResource(R.xml.display_settings);

        mAccelerometer = (CheckBoxPreference) findPreference(KEY_ACCELEROMETER);
        mAccelerometer.setPersistent(false);
        if (!RotationPolicy.isRotationSupported(getActivity())
                || RotationPolicy.isRotationLockToggleSupported(getActivity())) {
            // If rotation lock is supported, then we do not provide this option in
            // Display settings.  However, is still available in Accessibility settings,
            // if the device supports rotation.
            getPreferenceScreen().removePreference(mAccelerometer);
        }

        mScreenSaverPreference = findPreference(KEY_SCREEN_SAVER);
        if (mScreenSaverPreference != null
                && getResources().getBoolean(
                        com.android.internal.R.bool.config_dreamsSupported) == false) {
            getPreferenceScreen().removePreference(mScreenSaverPreference);
        }

        mScreenTimeoutPreference = (ListPreference) findPreference(KEY_SCREEN_TIMEOUT);
        final long currentTimeout = Settings.System.getLong(resolver, SCREEN_OFF_TIMEOUT,
                FALLBACK_SCREEN_TIMEOUT_VALUE);
        mScreenTimeoutPreference.setValue(String.valueOf(currentTimeout));
        mScreenTimeoutPreference.setOnPreferenceChangeListener(this);
        disableUnusableTimeouts(mScreenTimeoutPreference);
        updateTimeoutPreferenceDescription(currentTimeout);

        mFontSizePref = (WarnedListPreference) findPreference(KEY_FONT_SIZE);
        mFontSizePref.setOnPreferenceChangeListener(this);
        mFontSizePref.setOnPreferenceClickListener(this);
        mNotificationPulse = (CheckBoxPreference) findPreference(KEY_NOTIFICATION_PULSE);
        if (mNotificationPulse != null
                && getResources().getBoolean(
                        com.android.internal.R.bool.config_intrusiveNotificationLed) == false) {
            getPreferenceScreen().removePreference(mNotificationPulse);
        } else {
            try {
                mNotificationPulse.setChecked(Settings.System.getInt(resolver,
                        Settings.System.NOTIFICATION_LIGHT_PULSE) == 1);
                mNotificationPulse.setOnPreferenceChangeListener(this);
            } catch (SettingNotFoundException snfe) {
                Log.e(TAG, Settings.System.NOTIFICATION_LIGHT_PULSE + " not found");
            }
        }

       boolean isChange ="true".equals(SystemProperties.get("sys.resolution.changed", "false"));
       mResolutionPreference = (ListPreference) findPreference(KEY_RESOLUTION);
       if(isChange){
      
       mResolutionPreference.setOnPreferenceChangeListener(this);
       updateResolution();
     }else{
          getPreferenceScreen().removePreference(mResolutionPreference);
      }
    }

    private void updateTimeoutPreferenceDescription(long currentTimeout) {
        ListPreference preference = mScreenTimeoutPreference;
        String summary;
        //************************************
        //* modify by bonovo zbiao
        //************************************
        Log.e(TAG, "====== updateTimeoutPreferenceDescription(" + currentTimeout + ")");
        //if (currentTimeout < 0) {
        if (currentTimeout < -1) {
        //************************************
            // Unsupported value
            summary = "";
        } else {
            final CharSequence[] entries = preference.getEntries();
            final CharSequence[] values = preference.getEntryValues();
            if (entries == null || entries.length == 0) {
                summary = "";
            } else {
                int best = 0;
                for (int i = 0; i < values.length; i++) {
                    long timeout = Long.parseLong(values[i].toString());
                    //************************************
                    //* modify by bonovo zbiao
                    //************************************
                    Log.e(TAG, "====== timeout:" + timeout);
                    //if (currentTimeout >= timeout) {
                    //    best = i;
                    //}
                    if ((currentTimeout > 0) && (currentTimeout >= timeout)){
                        best = i;
                    } else if ((currentTimeout < 0) && (currentTimeout == timeout)){
                        best = i;
                        break;
                    }
                    //************************************
                }
                //************************************
                //* modify by bonovo zbiao
                //************************************
                //summary = preference.getContext().getString(R.string.screen_timeout_summary,
                //        entries[best]);
                if (currentTimeout < 0){
                    summary = entries[best].toString();
                } else {
                    summary = preference.getContext().getString(R.string.screen_timeout_summary,
                            entries[best]);
                }
                //************************************
            }
        }
        preference.setSummary(summary);
    }

    private void disableUnusableTimeouts(ListPreference screenTimeoutPreference) {
        final DevicePolicyManager dpm =
                (DevicePolicyManager) getActivity().getSystemService(
                Context.DEVICE_POLICY_SERVICE);
        final long maxTimeout = dpm != null ? dpm.getMaximumTimeToLock(null) : 0;
        if (maxTimeout == 0) {
            return; // policy not enforced
        }
        final CharSequence[] entries = screenTimeoutPreference.getEntries();
        final CharSequence[] values = screenTimeoutPreference.getEntryValues();
        ArrayList<CharSequence> revisedEntries = new ArrayList<CharSequence>();
        ArrayList<CharSequence> revisedValues = new ArrayList<CharSequence>();
        for (int i = 0; i < values.length; i++) {
            long timeout = Long.parseLong(values[i].toString());
            if (timeout <= maxTimeout) {
                revisedEntries.add(entries[i]);
                revisedValues.add(values[i]);
            }
        }
        if (revisedEntries.size() != entries.length || revisedValues.size() != values.length) {
            final int userPreference = Integer.parseInt(screenTimeoutPreference.getValue());
            screenTimeoutPreference.setEntries(
                    revisedEntries.toArray(new CharSequence[revisedEntries.size()]));
            screenTimeoutPreference.setEntryValues(
                    revisedValues.toArray(new CharSequence[revisedValues.size()]));
            if (userPreference <= maxTimeout) {
                screenTimeoutPreference.setValue(String.valueOf(userPreference));
            } else if (revisedValues.size() > 0
                    && Long.parseLong(revisedValues.get(revisedValues.size() - 1).toString())
                    == maxTimeout) {
                // If the last one happens to be the same as the max timeout, select that
                screenTimeoutPreference.setValue(String.valueOf(maxTimeout));
            } else {
                // There will be no highlighted selection since nothing in the list matches
                // maxTimeout. The user can still select anything less than maxTimeout.
                // TODO: maybe append maxTimeout to the list and mark selected.
            }
        }
        screenTimeoutPreference.setEnabled(revisedEntries.size() > 0);
    }

    int floatToIndex(float val) {
        String[] indices = getResources().getStringArray(R.array.entryvalues_font_size);
        float lastVal = Float.parseFloat(indices[0]);
        for (int i=1; i<indices.length; i++) {
            float thisVal = Float.parseFloat(indices[i]);
            if (val < (lastVal + (thisVal-lastVal)*.5f)) {
                return i-1;
            }
            lastVal = thisVal;
        }
        return indices.length-1;
    }
    
    public void readFontSizePreference(ListPreference pref) {
        try {
            mCurConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to retrieve font size");
        }

        // mark the appropriate item in the preferences list
        int index = floatToIndex(mCurConfig.fontScale);
        pref.setValueIndex(index);

        // report the current size in the summary text
        final Resources res = getResources();
        String[] fontSizeNames = res.getStringArray(R.array.entries_font_size);
        pref.setSummary(String.format(res.getString(R.string.summary_font_size),
                fontSizeNames[index]));
    }
    
    @Override
    public void onResume() {
        super.onResume();

        RotationPolicy.registerRotationPolicyListener(getActivity(),
                mRotationPolicyListener);

        updateState();
    }

    @Override
    public void onPause() {
        super.onPause();

        RotationPolicy.unregisterRotationPolicyListener(getActivity(),
                mRotationPolicyListener);
    }
/*
    @Override
    public Dialog onCreateDialog(int dialogId) {
        if (dialogId == DLG_GLOBAL_CHANGE_WARNING) {
            return Utils.buildGlobalChangeWarningDialog(getActivity(),
                    R.string.global_font_change_title,
                    new Runnable() {
                        public void run() {
                            mFontSizePref.click();
                        }
                    });
        }
        return null;
    }
*/
    private void updateState() {
        updateAccelerometerRotationCheckbox();
        readFontSizePreference(mFontSizePref);
        updateScreenSaverSummary();
    }

    private void updateScreenSaverSummary() {
        if (mScreenSaverPreference != null) {
            mScreenSaverPreference.setSummary(
                    DreamSettings.getSummaryTextWithDreamName(getActivity()));
        }
    }

    private void updateAccelerometerRotationCheckbox() {
        if (getActivity() == null) return;

        mAccelerometer.setChecked(!RotationPolicy.isRotationLocked(getActivity()));
    }

    public void writeFontSizePreference(Object objValue) {
        try {
            mCurConfig.fontScale = Float.parseFloat(objValue.toString());
            ActivityManagerNative.getDefault().updatePersistentConfiguration(mCurConfig);
        } catch (RemoteException e) {
            Log.w(TAG, "Unable to save font size");
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference == mAccelerometer) {
            RotationPolicy.setRotationLockForAccessibility(
                    getActivity(), !mAccelerometer.isChecked());
        } else if (preference == mNotificationPulse) {
            boolean value = mNotificationPulse.isChecked();
            Settings.System.putInt(getContentResolver(), Settings.System.NOTIFICATION_LIGHT_PULSE,
                    value ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        final String key = preference.getKey();
        if (KEY_SCREEN_TIMEOUT.equals(key)) {
            int value = Integer.parseInt((String) objValue);
            try {
                Settings.System.putInt(getContentResolver(), SCREEN_OFF_TIMEOUT, value);
                updateTimeoutPreferenceDescription(value);
            } catch (NumberFormatException e) {
                Log.e(TAG, "could not persist screen timeout setting", e);
            }
        }
        if (KEY_FONT_SIZE.equals(key)) {
            writeFontSizePreference(objValue);
        }
         if (KEY_RESOLUTION.equals(key)) {
            mRequestResolution = objValue.toString();
            if(mCurrentResolution != mResolutionEntryValues.indexOf(mRequestResolution)) {
                showRebootDialog();
            }
            return false;
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mFontSizePref) {
            if (Utils.hasMultipleUsers(getActivity())) {
                showDialog(DLG_GLOBAL_CHANGE_WARNING);
                return true;
            } else {
                mFontSizePref.click();
            }
        }
        return false;
    }
}
