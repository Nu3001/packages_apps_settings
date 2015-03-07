/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.android.settings.ethernet;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.preference.CheckBoxPreference;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.net.ethernet.EthernetManager;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import android.net.EthernetDataTracker;

public class EthernetSettings extends SettingsPreferenceFragment {
    private static final String TAG = "EthernetSettings";

		private static final String USB_ETHERNET_SETTINGS = "ethernet";
	
    private static final String KEY_ETH_IP_ADDRESS = "ethernet_ip_addr";
    private static final String KEY_ETH_NET_MASK = "ethernet_netmask";
    private static final String KEY_ETH_GATEWAY = "ethernet_gateway";
    private static final String KEY_ETH_DNS1 = "ethernet_dns1";
    private static final String KEY_ETH_DNS2 = "ethernet_dns2";
	private static final String KEY_ETH_MAC = "ethernet_mac";
	
	EthernetManager mEthManager;
	
   	private  static String mEthIpAddress = null;
	private  static String mEthNetmask = null;
	private  static String mEthGateway = null;
	private  static String mEthdns1 = null;
	private  static String mEthdns2 = null;
	private  static String mEthmac= null;
	private final static String nullIpInfo = "0.0.0.0";
	
	private CheckBoxPreference mUseEthernet;
	
    private IntentFilter mIntentFilter;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(EthernetDataTracker.EXTRA_ETHERNET_STATE, 0);
			Log.d(TAG, "BroadcastReceiver: Ethernet current state:" + state);
			getEthInfo(state);
        }
    };

	private boolean isEthernetEnabled() {
		return Settings.Secure.getInt(getContentResolver(), Settings.Secure.ETHERNET_ON, 1) == 1 ? true : false;
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ethernet_settings);
        
		mUseEthernet = (CheckBoxPreference) findPreference(USB_ETHERNET_SETTINGS);
		mUseEthernet.setSummary(R.string.ethernet_unconnected);
		if(isEthernetEnabled()) {
			mUseEthernet.setChecked(true);
		} else {
			mUseEthernet.setChecked(false);
		}
		
		mEthManager = (EthernetManager) getSystemService(Context.ETHERNET_SERVICE);
		if (mEthManager == null) {
			Log.e(TAG, "get ethernet manager failed");
			return;
		}
		
		getEthInfo(mEthManager.getEthernetConnectState());
		mIntentFilter = new IntentFilter(EthernetDataTracker.ETHERNET_STATE_CHANGED_ACTION);
	}
	
    @Override
    public void onResume() {
        super.onResume();

        getActivity().registerReceiver(mReceiver, mIntentFilter);
		if (mEthManager == null) return;

		getEthInfo(mEthManager.getEthernetConnectState());
	}

    /*@Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }
*/

     @Override
     public void onPause() {
        super.onPause();
        getActivity().unregisterReceiver(mReceiver);
    }	

   @Override
    public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference preference) {

        if (preference == mUseEthernet) {
            boolean newState = mUseEthernet.isChecked();

            if (newState) {
                mEthManager.setEthernetEnabled(true);
            } else {
                mEthManager.setEthernetEnabled(false);
            }
        } 

        return super.onPreferenceTreeClick(screen, preference);
    }

    private void setStringSummary(String preference, String value) {
        try {
            findPreference(preference).setSummary(value);
        } catch (RuntimeException e) {
            findPreference(preference).setSummary("");
        }
    }

	public void getEthInfoFromDhcp(){	
		String tempIpInfo;
		String iface = mEthManager.getEthernetIfaceName();
		
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".ipaddress");
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")) ){ 
			mEthIpAddress = tempIpInfo;
    	} else {  
    		mEthIpAddress = nullIpInfo;
    	}
				
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".mask");	
		if ((tempIpInfo != null) && (!tempIpInfo.equals("")) ){
       		mEthNetmask = tempIpInfo;
    	} else {           		
    		mEthNetmask = nullIpInfo;
    	}
					
		tempIpInfo = SystemProperties.get("dhcp."+ iface +".gateway");	
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
        	mEthGateway = tempIpInfo;
    	} else {
    		mEthGateway = nullIpInfo;        		
    	}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".dns1");
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
       		mEthdns1 = tempIpInfo;
    	} else {
    		mEthdns1 = nullIpInfo;      		
    	}

		tempIpInfo = SystemProperties.get("dhcp."+ iface +".dns2");
		if ((tempIpInfo != null) && (!tempIpInfo.equals(""))){
       		mEthdns2 = tempIpInfo;
    	} else {
    		mEthdns2 = nullIpInfo;       		
    	}
	}

	public void getEthInfoFromStaticIP(){	
		mEthIpAddress = Settings.System.getString(getContentResolver(), Settings.System.ETHERNET_STATIC_IP);
		mEthNetmask = Settings.System.getString(getContentResolver(), Settings.System.ETHERNET_STATIC_NETMASK);
		mEthGateway = Settings.System.getString(getContentResolver(), Settings.System.ETHERNET_STATIC_GATEWAY);
		mEthdns1 = Settings.System.getString(getContentResolver(), Settings.System.ETHERNET_STATIC_DNS1);
		mEthdns2 = Settings.System.getString(getContentResolver(), Settings.System.ETHERNET_STATIC_DNS2);
	}	

	public String getEthMac(){	
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("sys/class/net/eth0/address"));
            return reader.readLine();
        }catch (Exception e) {
            Log.e(TAG, "open sys/class/net/eth0/address failed : " + e);
            return "";
        }finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Log.e(TAG, "close sys/class/net/eth0/address failed : " + e);
            }
        }		
	}
	
	private void clearIpInfo() {
		mEthIpAddress = nullIpInfo;
		mEthNetmask = nullIpInfo;
		mEthGateway = nullIpInfo;        		
		mEthdns1 = nullIpInfo;      		
		mEthdns2 = nullIpInfo;    
		mEthmac = "00.00.00.00.00.00";  		
	}

	private boolean isUsingStaticIp() {
		return Settings.System.getInt(getContentResolver(), Settings.System.ETHERNET_USE_STATIC_IP, 0) == 1 ? true : false;
	}
	public void getEthInfo(int state){	
		if (state == EthernetDataTracker.ETHER_STATE_CONNECTING) {
				mUseEthernet.setEnabled(false);
    		mUseEthernet.setSummary(R.string.ethernet_connecting);
				clearIpInfo();
		} else if (state == EthernetDataTracker.ETHER_STATE_DISCONNECTED) {
				mUseEthernet.setEnabled(true);
    		mUseEthernet.setSummary(R.string.ethernet_unconnected);
				clearIpInfo();
		} else if (state == EthernetDataTracker.ETHER_STATE_CONNECTED) {
			mUseEthernet.setEnabled(true);
			mUseEthernet.setSummary(R.string.ethernet_connected);
			
			if(isUsingStaticIp()) {
				getEthInfoFromStaticIP();
			} else {
				getEthInfoFromDhcp();
			}
			mEthmac = getEthMac();
		}

		setStringSummary(KEY_ETH_IP_ADDRESS, mEthIpAddress);
		//setStringSummary(KEY_ETH_NET_MASK, mEthNetmask);
		//setStringSummary(KEY_ETH_GATEWAY, mEthGateway);
		//setStringSummary(KEY_ETH_DNS1, mEthdns1); 
		//setStringSummary(KEY_ETH_DNS2, mEthdns2); 
		setStringSummary(KEY_ETH_MAC, mEthmac); 
	}
}

