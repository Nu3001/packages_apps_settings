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
    
    /** 同 static IP 设置 相关的 Preference 实例的 key 字串数组. */
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

    /** "this" 的组件, 用于具体完成对 '用户对 "mEnableEthernetCheckBox" 的操作' 的 具体响应, 
     * 也用来根据系统的状态, 来更新 "mEnableEthernetCheckBox" 的 UI 外观. */
    // private EthernetEnabler mEthernetEnabler;

	/*-------------------------------------------------------*/

    /* eth 的 概要状态. */
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
            if(!chageState)   //没有修改直接返回
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
     * 更新 关于 IP 设置信息的 UI 内容.
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
     * 将 IP 设置信息保存到 android.provider.Settings 中. 
     * .! : 
     * 通过 通知回调的机制, 将驱动对应的 EthernetStateTracker 实例 完成对 ethernet 网口的 具体配置. 
     * @see EthernetStateTracker::SettingsObserver. 
     */
    private void saveIpSettingsInfo() {
        ContentResolver contentResolver = getContentResolver();
        
        if(!chageState)   //没有修改直接返回
        	return;
        
        if(!isIpDataInUiComplete()) //IP设置是否完全
        {     //保存失败
        	 //Toast.makeText(getActivity(), R.string.save_failed, Toast.LENGTH_LONG).show();
        	 Toast.makeText(getActivity(), R.string.eth_ip_settings_please_complete_settings, Toast.LENGTH_LONG).show();
        	 return;
        }

        /* 遍历 static IP settings, ... */
        for (int i = 0; i < mSettingNames.length; i++) {
            /* 获取 引用. */
            EditTextPreference preference = (EditTextPreference) findPreference(mPreferenceKeys[i]);
            String text = preference.getText();
            /* 若 "text" "是" null 字串 or 空字串, 则 ... */
            if ( null == text || TextUtils.isEmpty(text) ) {
                /* 将 null 存储为对应的 配置 value.     .R : 参见 EthernetStateTracker::checkUseStaticIp(). */
                System.putString(contentResolver, mSettingNames[i], null);
            }
            /* 否则, ... */
            else {
                /* 直接存储 "text". */
                System.putString(contentResolver, mSettingNames[i], text);
            }
        }
        
        /* 保存 关键的 "是否使用静态 IP" 的配置. */
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
        
        boolean result = true;     /* 返回值 buffer. */
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
        /* 否则若 当事人 Preference 是 "mPreferenceKeys" 中的某个, 则 ... */
        else if ( key.equals(KEY_IP_ADDRESS) 
                || key.equals(KEY_GATEWAY)
                || key.equals(KEY_NETMASK)
                || key.equals(KEY_DNS1)
                || key.equals(KEY_DNS2) ) { 

            String value = (String) newValue;       /* 用户新输入的 IP 地址. 这是由程序员保证的 向下转型. */
            
            LOG("onPreferenceChange() : value = " + value);

            /* 若 "value" 是 空字串, 则 ... */
            if ( TextUtils.isEmpty(value) ) {
                /* 将 "value" 设置为 text. */
                ( (EditTextPreference)preference).setText(value);
                /* 将 "preference" 的 summary 也设置空. */
                preference.setSummary(value);
                /* 预置 返回值 true. */
                result = true;
            }
            /* 否则, 若 "value" 不是 有效 IP 地址, 则 ... */
            else  if ( !isValidIpAddress(value) ) {
                LOG("onPreferenceChange() : IP address user inputed is INVALID." );
                /* 在 UI 上提示用户, 先前输入的 IP 地址无效. */
                Toast.makeText(getActivity(), R.string.ethernet_ip_settings_invalid_ip, Toast.LENGTH_LONG).show();
                /* 返回 false. */
                return false;
            }
            /* 否则, 即 "value" 是有效的 IP 地址, 则 ... */
            else {
                /* 将 "value" 设置为 text. */
                ( (EditTextPreference)preference).setText(value);
                /* 将 "preference" 的 summary 设置为 "value". */
                preference.setSummary(value);
                /* 预置 返回值 true. */
                result = true;
            }

            /* 配置 "mEnableNewIpSettingsCheckBox" 的状态. */
            //configEnableNewIpSettingsCheckBox();
        }

        /* 返回. */
        return result;
    }    


    /**
     * 返回 指定的 String 是否是 有效的 IP 地址. 
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
     * 用户在 UI 中输入的 static IP 设置数据是否 完备.
     * 用户输入是否是 有效的 IP 字串, 在别处判别. 
     */
    private boolean isIpDataInUiComplete() {

        ContentResolver contentResolver = getContentResolver();

        /* 遍历 "mPreferenceKeys" 中除了 dns2 以外的 元素, ... */
        for (int i = 0; i < (mPreferenceKeys.length - 1); i++) {
            EditTextPreference preference = (EditTextPreference) findPreference(mPreferenceKeys[i]);
            String text = preference.getText();
            LOG("isIpDataInUiComplete() : text = " + text);

            /* 若当前 IP 参数 为 null 或者 为 空字串, 则 ... */
            if ( null == text || TextUtils.isEmpty(text) ) {
                /* 返回否定结果. */
                return false;
            }
        }
        
        /* 返回肯定. */
        return true;
    }

    /**
     * 根据当前的 UI 状态, 配置是否 使能 "mEnableNewIpSettingsCheckBox". 
     * @param shouldShowToast
     *          是否要使用 Tosat 的形式, 对应提示用户.
     */
    private void configEnableNewIpSettingsCheckBox() {
        /* 若用户选中了 "mUseStaticIpCheckBox", 且 UI 中的 satic IP settings 数据 "不" 完备, 则 ... */    
        if (!isIpDataInUiComplete()) {
                /* 使用 Toast, 提示用户 : "Please give complete static IP settings!". */
                Toast.makeText(getActivity(), R.string.eth_ip_settings_please_complete_settings, Toast.LENGTH_LONG).show();
        }
    }
    

}
