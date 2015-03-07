package com.android.settings.ethernet;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ContentResolver;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;

import android.provider.Settings.System;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

import android.text.TextUtils;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.Formatter;

import android.net.NetworkInfo.DetailedState;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;
import android.net.NetworkInfo;

import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.app.AlertDialog;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

public class EthernetStaticIP  extends SettingsPreferenceFragment 
implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "EthernetStaticIP";
    public static final boolean DEBUG = false;
    // public static final boolean DEBUG = false;
    private static void LOG(String msg) {
        if ( DEBUG ) {
            Log.d(TAG, msg);
        }
    }
    
	/*-------------------------------------------------------*/
    
    private static final String KEY_USE_STATIC_IP = "use_static_ip";

    private static final String KEY_IP_ADDRESS = "ip_address";
    private static final String KEY_GATEWAY = "gateway";
    private static final String KEY_NETMASK = "netmask";
    private static final String KEY_DNS1 = "dns1";
    private static final String KEY_DNS2 = "dns2";
    
    private static final int MENU_ITEM_SAVE = Menu.FIRST;
    private static final int MENU_ITEM_CANCEL = Menu.FIRST + 1;
    
    private String[] mSettingNames = {
        System.ETHERNET_STATIC_IP, 
        System.ETHERNET_STATIC_GATEWAY,
        System.ETHERNET_STATIC_NETMASK,
        System.ETHERNET_STATIC_DNS1, 
        System.ETHERNET_STATIC_DNS2
    };
    
    /** ͬ static IP ���� ��ص� Preference ʵ���� key �ִ�����. */
    private String[] mPreferenceKeys = {
        KEY_IP_ADDRESS,
        KEY_GATEWAY,
        KEY_NETMASK,
        KEY_DNS1,
        KEY_DNS2,
    };
    
	/*-------------------------------------------------------*/
    
    private CheckBoxPreference mUseStaticIpCheckBox;
    
    private boolean isOnPause = false;
    // private Preference mIpNetworkStatusPref;

    /** "this" �����, ���ھ�����ɶ� '�û��� "mEnableEthernetCheckBox" �Ĳ���' �� ������Ӧ, 
     * Ҳ��������ϵͳ��״̬, ������ "mEnableEthernetCheckBox" �� UI ���. */
    // private EthernetEnabler mEthernetEnabler;

	/*-------------------------------------------------------*/

    /* eth �� ��Ҫ״̬. */
    //private int mEthState;
    private boolean chageState = false;
    //============================
    // Activity lifecycle
    //============================

    public EthernetStaticIP() {
    }
    

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
		
        addPreferencesFromResource(R.xml.ethernet_static_ip);

        mUseStaticIpCheckBox = (CheckBoxPreference)findPreference(KEY_USE_STATIC_IP);
  
        for ( int i = 0; i < mPreferenceKeys.length; i++ ) {
            Preference preference = findPreference(mPreferenceKeys[i] );
            preference.setOnPreferenceChangeListener(this);
        }

        //mEthMgr = (EthernetManager) getSystemService(ETHERNET_SERVICE);
        
		registerForContextMenu(getListView());
        setHasOptionsMenu(true);
    }
    

    @Override
    public void onResume() {
        super.onResume();

        //mEthState = mEthMgr.getEthernetState();
        if(!isOnPause) {
            updateIpSettingsInfo();
        }
        isOnPause = false;
    }
    
    
    
    @Override
    public void onPause() {
        isOnPause = true;
        super.onPause();
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }
    
    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(!chageState)   //û���޸�ֱ�ӷ���
            {
            	finish();
            	return true;
            }
            
        	new AlertDialog.Builder(EthernetStaticIP.this)
        		.setTitle(R.string.str_about)
        		.setMessage(R.string.str_mesg)
        		.setPositiveButton(R.string.str_ok, 
        		 new DialogInterface.OnClickListener()
        		 {
        		    public void onClick(DialogInterface dialoginterfacd,int i)
        		    {
        		    	saveIpSettingsInfo();
        		    	finish();
        		    }
        		 } 
        		)
        		.setNegativeButton(R.string.str_cancel,
        		 new DialogInterface.OnClickListener()
       		 	{
        			public void onClick(DialogInterface dialoginterfacd,int i)
        			{
        				finish();
        			}
       		 	}  		
        		)
        		.show();
        	return true;
        }
         
        return super.onKeyDown(keyCode, event);
    }*/
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	
        menu.add(Menu.NONE, MENU_ITEM_SAVE, 0, R.string.staticip_save)
                .setEnabled(true)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        menu.add(Menu.NONE, MENU_ITEM_CANCEL, 0, R.string.staticip_cancel)
                .setEnabled(true)			
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
        
            case MENU_ITEM_SAVE:
            	saveIpSettingsInfo();
            	finish();
                return true;
                
            case MENU_ITEM_CANCEL:
                finish();
                return true;
        }
        
        return super.onOptionsItemSelected(item);
    }        
    
    /**
     * ���� ���� IP ������Ϣ�� UI ����.
     */
    private void updateIpSettingsInfo() {
    	LOG("Static IP status updateIpSettingsInfo");
        ContentResolver contentResolver = getContentResolver();
        
        mUseStaticIpCheckBox.setChecked(System.getInt(contentResolver, System.ETHERNET_USE_STATIC_IP, 0) != 0);
        
        for (int i = 0; i < mSettingNames.length; i++) {
            EditTextPreference preference = (EditTextPreference) findPreference(mPreferenceKeys[i]);
            String settingValue = System.getString(contentResolver, mSettingNames[i]);
            preference.setText(settingValue);
            preference.setSummary(settingValue);
        }
    }
        

    /**
     * �� IP ������Ϣ���浽 android.provider.Settings ��. 
     * .! : 
     * ͨ�� ֪ͨ�ص��Ļ���, ��������Ӧ�� EthernetStateTracker ʵ�� ��ɶ� ethernet ���ڵ� ��������. 
     * @see EthernetStateTracker::SettingsObserver. 
     */
    private void saveIpSettingsInfo() {
        ContentResolver contentResolver = getContentResolver();
        
        if(!chageState)   //û���޸�ֱ�ӷ���
        	return;
        
        if(!isIpDataInUiComplete()) //IP�����Ƿ���ȫ
        {     //����ʧ��
        	 //Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_LONG).show();
        	 Toast.makeText(getActivity(), R.string.eth_ip_settings_please_complete_settings, Toast.LENGTH_LONG).show();
        	 return;
        }

        /* ���� static IP settings, ... */
        for (int i = 0; i < mSettingNames.length; i++) {
            /* ��ȡ ����. */
            EditTextPreference preference = (EditTextPreference) findPreference(mPreferenceKeys[i]);
            String text = preference.getText();
            /* �� "text" "��" null �ִ� or ���ִ�, �� ... */
            if ( null == text || TextUtils.isEmpty(text) ) {
                /* �� null �洢Ϊ��Ӧ�� ���� value.     .R : �μ� EthernetStateTracker::checkUseStaticIp(). */
                System.putString(contentResolver, mSettingNames[i], null);
            }
            /* ����, ... */
            else {
                /* ֱ�Ӵ洢 "text". */
                System.putString(contentResolver, mSettingNames[i], text);
            }
        }
        
        /* ���� �ؼ��� "�Ƿ�ʹ�þ�̬ IP" ������. */
        System.putInt(contentResolver, System.ETHERNET_USE_STATIC_IP, mUseStaticIpCheckBox.isChecked() ? 1 : 0);
        
        // disable ethernet
        boolean enable = Secure.getInt(getContentResolver(), Secure.ETHERNET_ON, 1) == 1;
		LOG("notify Secure.ETHERNET_ON changed. enable = " + enable);
        if(enable) {
        	LOG("first disable");
        	Secure.putInt(getContentResolver(), Secure.ETHERNET_ON, 0);
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {}   
			LOG("second enable");
        	Secure.putInt(getContentResolver(), Secure.ETHERNET_ON, 1);    	
        }
    }


    //============================
    // Preference callbacks
    //============================
   
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        
        boolean result = true;     /* ����ֵ buffer. */
        LOG("onPreferenceTreeClick()  chageState = " + chageState);
        chageState = true;

        return result;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {

        boolean result = true;
        String key = preference.getKey();
        LOG("onPreferenceChange() : key = " + key);

        if ( null == key ) {
            return true;
        }   
        /* ������ ������ Preference �� "mPreferenceKeys" �е�ĳ��, �� ... */
        else if ( key.equals(KEY_IP_ADDRESS) 
                || key.equals(KEY_GATEWAY)
                || key.equals(KEY_NETMASK)
                || key.equals(KEY_DNS1)
                || key.equals(KEY_DNS2) ) { 

            String value = (String) newValue;       /* �û�������� IP ��ַ. �����ɳ���Ա��֤�� ����ת��. */
            
            LOG("onPreferenceChange() : value = " + value);

            /* �� "value" �� ���ִ�, �� ... */
            if ( TextUtils.isEmpty(value) ) {
                /* �� "value" ����Ϊ text. */
                ( (EditTextPreference)preference).setText(value);
                /* �� "preference" �� summary Ҳ���ÿ�. */
                preference.setSummary(value);
                /* Ԥ�� ����ֵ true. */
                result = true;
            }
            /* ����, �� "value" ���� ��Ч IP ��ַ, �� ... */
            else  if ( !isValidIpAddress(value) ) {
                LOG("onPreferenceChange() : IP address user inputed is INVALID." );
                /* �� UI ����ʾ�û�, ��ǰ����� IP ��ַ��Ч. */
                Toast.makeText(getActivity(), R.string.ethernet_ip_settings_invalid_ip, Toast.LENGTH_LONG).show();
                /* ���� false. */
                return false;
            }
            /* ����, �� "value" ����Ч�� IP ��ַ, �� ... */
            else {
                /* �� "value" ����Ϊ text. */
                ( (EditTextPreference)preference).setText(value);
                /* �� "preference" �� summary ����Ϊ "value". */
                preference.setSummary(value);
                /* Ԥ�� ����ֵ true. */
                result = true;
            }

            /* ���� "mEnableNewIpSettingsCheckBox" ��״̬. */
            //configEnableNewIpSettingsCheckBox();
        }

        /* ����. */
        return result;
    }    


    /**
     * ���� ָ���� String �Ƿ��� ��Ч�� IP ��ַ. 
     */
    private boolean isValidIpAddress(String value) {
        
        int start = 0;
        int end = value.indexOf('.');
        int numBlocks = 0;
        
        while (start < value.length()) {
            
            if ( -1 == end ) {
                end = value.length();
            }

            try {
                int block = Integer.parseInt(value.substring(start, end));
                if ((block > 255) || (block < 0)) {
                    Log.w(TAG, "isValidIpAddress() : invalid 'block', block = " + block);
                    return false;
                }
            } catch (NumberFormatException e) {
                Log.w(TAG, "isValidIpAddress() : e = " + e);
                return false;
            }
            
            numBlocks++;
            
            start = end + 1;
            end = value.indexOf('.', start);
        }
        
        return numBlocks == 4;
    }
    
    /**
     * �û��� UI ������� static IP ���������Ƿ� �걸.
     * �û������Ƿ��� ��Ч�� IP �ִ�, �ڱ��б�. 
     */
    private boolean isIpDataInUiComplete() {

        ContentResolver contentResolver = getContentResolver();

        /* ���� "mPreferenceKeys" �г��� dns2 ����� Ԫ��, ... */
        for (int i = 0; i < (mPreferenceKeys.length - 1); i++) {
            EditTextPreference preference = (EditTextPreference) findPreference(mPreferenceKeys[i]);
            String text = preference.getText();
            LOG("isIpDataInUiComplete() : text = " + text);

            /* ����ǰ IP ���� Ϊ null ���� Ϊ ���ִ�, �� ... */
            if ( null == text || TextUtils.isEmpty(text) ) {
                /* ���ط񶨽��. */
                return false;
            }
        }
        
        /* ���ؿ϶�. */
        return true;
    }

    /**
     * ���ݵ�ǰ�� UI ״̬, �����Ƿ� ʹ�� "mEnableNewIpSettingsCheckBox". 
     * @param shouldShowToast
     *          �Ƿ�Ҫʹ�� Tosat ����ʽ, ��Ӧ��ʾ�û�.
     */
    private void configEnableNewIpSettingsCheckBox() {
        /* ���û�ѡ���� "mUseStaticIpCheckBox", �� UI �е� satic IP settings ���� "��" �걸, �� ... */    
        if (!isIpDataInUiComplete()) {
                /* ʹ�� Toast, ��ʾ�û� : "Please give complete static IP settings!". */
                Toast.makeText(getActivity(), R.string.eth_ip_settings_please_complete_settings, Toast.LENGTH_LONG).show();
        }
    }
    

}
