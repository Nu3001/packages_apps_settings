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

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.os.RemoteException;
import android.os.IPowerManager;
import android.os.ServiceManager;
import android.preference.SeekBarPreference;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.AttributeSet;
import android.util.Log;

import java.util.Map;

import java.io.*;
import android.os.SystemProperties;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.SystemProperties;
import android.content.ContentResolver;
import java.io.RandomAccessFile;
import static android.provider.Settings.System.HDMI_LCD_TIMEOUT;
import android.widget.Toast;
import com.android.settings.R;
public class HdmiReceiver extends BroadcastReceiver{
    private final String ACTION = "android.intent.action.HDMI_PLUG";
	private static final String TAG = "HdmiReceiver";
	private File HdmiState = new File("/sys/class/hdmi/hdmi-0/state");
	private File HdmiFile = new File("/sys/class/hdmi/hdmi-0/enable");
	private File HdmiDisplayEnable=new File("/sys/class/display/HDMI/enable");
    private File HdmiDisplayMode=new File("/sys/class/display/HDMI/mode");
    private File HdmiDisplayScale=new File("/sys/class/display/HDMI/scale");
	private Context mcontext;
	
    @Override
    public void onReceive(Context context, Intent intent){
    	mcontext = context;
        if (intent.getAction().equals(ACTION)){
		if(SystemProperties.get("ro.board.platform","none").equals("rk29xx"))
		   {
			SharedPreferences preferences = context.getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
		    int enable = preferences.getInt("enable", 1);
			int scale = preferences.getInt("scale_set", 100);
			int resol = preferences.getInt("resolution", 3);
		    restoreHdmiValue(HdmiFile, enable, "enable");
            restoreHdmiValue(HdmiState, scale, "hdmi_scale");
                        //Log.d(TAG,"rk29board ----enable ="+String.valueOf(enable));
			restoreHdmiValue(HdmiState, resol, "hdmi_resolution");
		    SharedPreferences.Editor  editor = preferences.edit();
		    editor.putInt("enable", enable);
			//restoreDoubleScreenDisplay(HdmiFile);
            Log.d(TAG,"rk2918board ----enable ="+String.valueOf(enable)+ " scale="+String.valueOf(scale)+" resol="+String.valueOf(resol));
		}else
		   {
		    SharedPreferences preferences = context.getSharedPreferences("Settings", context.MODE_PRIVATE);
            int enable = preferences.getInt("enable", 1);
			int resol = preferences.getInt("resolution", 2);
            restoreHdmiValue(HdmiDisplayEnable, enable, "enable");
			restoreHdmiValue(HdmiDisplayMode, resol, "hdmi_resolution");
            SharedPreferences preferences_scale = context.getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
            int scale=preferences_scale.getInt("scale_set",100);
            restoreHdmiValue(HdmiDisplayScale,scale,"hdmi_scale");
			int state=intent.getIntExtra("state", 1);
            if(getFBDualDisplayMode()==1){
               if(state==1){
                  SystemProperties.set("sys.hdmi_screen.scale",String.valueOf((char)scale));
               }else{
                  SystemProperties.set("sys.hdmi_screen.scale",String.valueOf((char)100)); 
               }
            }
			String text=context.getResources().getString((state==1)?R.string.hdmi_connect:R.string.hdmi_disconnect);
			Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            if(getFBDualDisplayMode()!=0){
               TurnonScreen();
            }
            Log.d(TAG,"enable ="+String.valueOf(enable)+ " scale="+String.valueOf(scale)+" resol="+String.valueOf(resol));
	       }
              
        }
    }
    
   
      private void TurnonScreen(){
                //boolean ff = SystemProperties.getBoolean("persist.sys.hdmi_screen", false);
                ContentResolver resolver = mcontext.getContentResolver();
                try {
                    int brightness = Settings.System.getInt(resolver,
                        Settings.System.SCREEN_BRIGHTNESS, 102);
                    IPowerManager power = IPowerManager.Stub.asInterface(
                        ServiceManager.getService("power"));
                    if (power != null) {
                        power.setTemporaryScreenBrightnessSettingOverride(brightness);
                    }
                } catch (Exception e) {
                        Log.e(TAG, "Exception"+e);
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

	 protected void restoreDoubleScreenDisplay(File file){
		if (file.exists()){
			try {
				if(SystemProperties.get("ro.board.platform","none").equals("rk29xx")){
				   SharedPreferences  sharedPreferences1 = mcontext.getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
				   int enable = sharedPreferences1.getInt("enable", 0);
				   String strDouble = "2";
				   RandomAccessFile rdf = null;
				   rdf = new RandomAccessFile(file, "rw");
				   if(enable == 0){
				   rdf.writeBytes("0");
				}else {
					rdf.writeBytes(strDouble);
				} 
				
			}else
			{
				SharedPreferences  sharedPreferences1 = mcontext.getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sharedPreferences1.edit();
				String strDouble = "2";
				RandomAccessFile rdf = null;
				rdf = new RandomAccessFile(file, "rw");
				
				if(HdmiControllerActivity.isHdmiConnected(HdmiState)){
					editor.putInt("enable", 2);
				}else{
					editor.putInt("enable", 0);
				}
				
				//SystemProperties.set("persist.sys.hdmi_screen", "1");
				editor.commit();
				}
			} catch (IOException re) {
				Log.e(TAG, "IO Exception");
			}
		}else{
			Log.e(TAG, "File:" + file + "not exists");
		}
	}


	protected void restoreHdmiValue(File file, int value, String style){
		if (file.exists()){
			try {
				FileReader		fread  = new FileReader(file);
				BufferedReader 	buffer = new BufferedReader(fread);
				StringBuffer	strbuf = new StringBuffer(""); 	
				String  		substr = null;
				String			str = null;
				int				length = 0;	
			    if(SystemProperties.get("ro.board.platform","none").equals("rk29xx")){
			       if(style.equals("enable")){
				      RandomAccessFile rdf = null;
				      rdf = new RandomAccessFile(HdmiFile, "rw");
				      if(value >= 1){
					   if(HdmiControllerActivity.isHdmiEnableDoubleScreen(HdmiState)){
						 rdf.writeBytes(String.valueOf(2));
					   }else{
						 rdf.writeBytes(String.valueOf(value));
					   }
				      }else{
					   rdf.writeBytes(String.valueOf(value));
			          }
				   }else{
				    if (style.equals("hdmi_scale")){	
					substr = "scale_set";
					while ((str = buffer.readLine()) != null){
					 length = str.length();
					 if (length == 13 || length == 12){
						String res = str.substring(0, 9);
						if (substr.equals(res)){
						String strValue = String.valueOf(value);
						String s = substr + "=" + strValue;
						strbuf.append(s + "\n");
					    }else{
						strbuf.append(str + "\n");
					    }
					 }else{
				     strbuf.append(str + "\n");
				     }
				   }
			    }
				if (style.equals("hdmi_resolution")){
					substr = "resolution";
					while ((str = buffer.readLine()) != null){
					if (str.length() == 12){
					    String res = str.substring(0, 10);
						if (substr.equals(res)){
						    String strValue = String.valueOf(value);	
						    String s = substr + "=" + strValue;
							strbuf.append(s + "\n");
						}else{
							strbuf.append(str + "\n");
						}
					}else
					    {
						 strbuf.append(str + "\n");
						}
					}							
				}

				buffer.close();
				fread.close();
				File f = new File("/sys/class/hdmi/hdmi-0/state");
				OutputStream output = null;
				OutputStreamWriter outputWrite = null;
				PrintWriter print = null;
				try {
					 output = new FileOutputStream(f);
					 outputWrite = new OutputStreamWriter(output);
					 print = new PrintWriter(outputWrite);
					 print.print(strbuf.toString());
					 print.flush();
					 output.close();
					}catch (FileNotFoundException e){
					 e.printStackTrace();
					}
			}

			
			}else{
				 if(style.equals("enable")){
				    Log.d(TAG,"restoreHdmiValue enable");
				    RandomAccessFile rdf = null;
					rdf = new RandomAccessFile(file, "rw");
					rdf.writeBytes(String.valueOf(value));
				 }
                 if(style.equals("hdmi_scale")){
                    OutputStream output = null;
                    OutputStreamWriter outputWrite = null;
                    PrintWriter print = null;
                    strbuf.append(value);
                    try {
                        output = new FileOutputStream(file);
                        outputWrite = new OutputStreamWriter(output);
                        print = new PrintWriter(outputWrite);
                        print.print(strbuf.toString());
                        print.flush();
                        output.close();
                    }catch (FileNotFoundException e){
                        e.printStackTrace();
                    }
                 }
				 if(style.equals("hdmi_resolution")){
					Log.d(TAG,"restoreHdmiValue hdmi_resolution");
					OutputStream output = null;
					OutputStreamWriter outputWrite = null;
					PrintWriter print = null;
					switch(value){
					case 1:
						strbuf.append("1920x1080p-50\n");
						break;
					case 2:
						strbuf.append("1920x1080p-60\n");
						break;
					case 3: 
						strbuf.append("1280x720p-50\n");
					    break;
					case 4:
						strbuf.append("1280x720p-60\n");
						break;
					case 5:
						strbuf.append("720x576p-50\n");
						break;
					case 6:
						strbuf.append("720x480p-60\n");
						break;
					}
					try {
						output = new FileOutputStream(file);
						outputWrite = new OutputStreamWriter(output);
						print = new PrintWriter(outputWrite);
						print.print(strbuf.toString());
						print.flush();
						output.close();
					}catch (FileNotFoundException e){
						e.printStackTrace();
					}
				}
                  
				buffer.close();
				fread.close();

			}
		} catch (IOException e){
			Log.e(TAG, "IO Exception");
		}
		}else{
			Log.e(TAG, "File:" + file + "not exists");
		}
	}
}

