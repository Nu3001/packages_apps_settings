/*
 * Copyright (C) 2008 The Android Open Source Project
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
import android.util.Log;
import android.content.Context;
import android.os.RemoteException;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SeekBarDialogPreference;
import android.os.SystemProperties;
import java.util.Map;

import java.io.*;
import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.Writer;
import java.io.FileReader;
import java.io.FileWriter;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class HdmiScreenZoomPreference extends SeekBarDialogPreference implements
        SeekBar.OnSeekBarChangeListener, CheckBox.OnCheckedChangeListener {

    private static final String TAG = "HdmiScreenZoomPreference";
    private static final int MINIMUN_SCREEN_SCALE = 0;
    private static final int MAXIMUN_SCREEN_SCALE = 20;

    private File HdmiScale = new File("/sys/class/display/HDMI/scale");
    private SeekBar mSeekBar;
    private int     mOldScale = 0;
    private int     mValue = 0;
    private int     mRestoreValue = 0;
    private boolean mFlag  = false;
	//for save hdmi config
    private	Context	context;

    public HdmiScreenZoomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setDialogLayoutResource(R.layout.preference_dialog_screen_scale);
        setDialogIcon(R.drawable.ic_settings_screen_scale);
		if(SystemProperties.get("ro.board.platform","none").equals("rk29xx"))
		{
			HdmiScale =  new File("/sys/class/hdmi/hdmi-0/state");
		}
    }

    protected void setHdmiScreenScale(File file, int value){
        if (file.exists()){
 	    try {
		StringBuffer	strbuf = new StringBuffer(""); 	
		if(SystemProperties.get("ro.board.platform","none").equals("rk29xx"))
		{
				FileReader		fread  = new FileReader(file);
				BufferedReader 	buffer = new BufferedReader(fread);
				String  		substr = "scale_set";
				String			str = null;
				int				length = 0;	

				while ((str = buffer.readLine()) != null){
				    length = str.length();
				    if (length == 13 || length == 12){
				        String res = str.substring(0, 9);
				        if (substr.equals(res)){
			                 String strValue = String.valueOf(value);
				         String s = substr + "=" + strValue;
				         strbuf.append(s + "\n");
				        }else
				         {
					    strbuf.append(str + "\n");
					 }

				    }else{
					    strbuf.append(str + "\n");
				    }
				}
				buffer.close();
				fread.close();
		}else
		{
                strbuf.append(value);
		}
		OutputStream output = null;
		OutputStreamWriter outputWrite = null;
		PrintWriter print = null;

		try {
                        //SystemProperties.set("sys.hdmi_screen.scale",String.valueOf(value));
			output = new FileOutputStream(file);
			outputWrite = new OutputStreamWriter(output);
			print = new PrintWriter(outputWrite);

			print.print(strbuf.toString());
			print.flush();
			output.close();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}
            } catch (IOException e){
				Log.e(TAG, "IO Exception");
	     }

	}else{
			Log.e(TAG, "File:" + file + "not exists");
	}
	if(getFBDualDisplayMode() == 1){
		SystemProperties.set("sys.hdmi_screen.scale",String.valueOf((char)value));
	}else{
		SystemProperties.set("sys.hdmi_screen.scale",String.valueOf((char)100));
	}
    }

    private int getFBDualDisplayMode(){
	    int mode = 0;
	    File DualModeFile = new File("/sys/class/graphics/fb0/dual_mode");
	    if(DualModeFile.exists()){
		    try {
			    byte[] buf = new byte[10];
			    int len = 0;
			    RandomAccessFile rdf = new RandomAccessFile(DualModeFile, "r");
			    len = rdf.read(buf);
			    String modeStr = new String(buf,0,1);
			    mode = Integer.valueOf(modeStr);
		    } catch (IOException re) {
			    Log.e(TAG, "IO Exception");
		    } catch (NumberFormatException re) {
			    Log.e(TAG, "NumberFormatException");
		    }
	    }
	    return mode;
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
		
	mFlag = false;
        mSeekBar = getSeekBar(view);
	//resotre value
	SharedPreferences preferences = context.getSharedPreferences("HdmiSettings", context.MODE_PRIVATE);
	mOldScale = preferences.getInt("scale_set", 100);
	mOldScale = mOldScale - 80;
		
        mSeekBar.setProgress(mOldScale);
        mSeekBar.setOnSeekBarChangeListener(this);
    }

    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromTouch) {
	mValue = progress + 80;
	if (mValue > 100){
		mValue = 100;
	}
	setHdmiScreenScale(HdmiScale, mValue);
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
	//If start tracking, record the initial position
	mFlag = true;
	mRestoreValue = seekBar.getProgress();
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
	setHdmiScreenScale(HdmiScale, mValue);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked){

    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
	//for save config
	SharedPreferences preferences = context.getSharedPreferences("HdmiSettings", context.MODE_PRIVATE);
	SharedPreferences.Editor	editor = preferences.edit();

	if (positiveResult){
		int value = mSeekBar.getProgress() + 80;
		setHdmiScreenScale(HdmiScale, value);
	        editor.putInt("scale_set", value);
	}else{
		if (mFlag){
			mRestoreValue = mRestoreValue + 80;
			if (mRestoreValue > 100){
				mRestoreValue = 100;
			}
			setHdmiScreenScale(HdmiScale, mRestoreValue);
			editor.putInt("scale_set", mRestoreValue);
		}else{
			//click cancel without any other operations
			int value = mSeekBar.getProgress() + 80;
			setHdmiScreenScale(HdmiScale, value);
			editor.putInt("scale_set", value);
		}
	}
			editor.commit();
        }
   }

