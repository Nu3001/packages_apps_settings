package com.android.settings;
import android.util.Log;
import static android.provider.Settings.System.SCREEN_OFF_TIMEOUT;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import android.os.SystemProperties;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
//import static android.provider.Settings.System.HDMI_LCD_TIMEOUT;
import android.content.ContentResolver;
import android.os.Handler;
import android.database.ContentObserver;
public class HdmiControllerActivity extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    /** Called when the activity is first created. */
	 private static final String TAG = "HdmiControllerActivity";
	    private static final String KEY_HDMI_RESOLUTION = "hdmi_resolution";
	    private static final String KEY_HDMI_MODE = "hdmi_mode";
	    private static final String KEY_HDMI = "hdmi";
	    private static final String KEY_HDCP = "HDCP_Setting";
        private static final String KEY_HDMI_LCD ="hdmi_lcd_timeout";
	    //for identify the HdmiFile state
	    private boolean IsHdmiConnect=false;
	    //for identify the Hdmi connection state
	    private boolean IsHdmiPlug = false;
	    private boolean IsHdmiDisplayOn = false;

	    private CheckBoxPreference mHdmi;
	    private CheckBoxPreference mHdcp;
	    private ListPreference     mHdmiResolution;
	    private ListPreference     mHdmiMode;
        private ListPreference     mHdmiLcd;


	    private File HdmiFile = null;
	    private File HdmiState = null;
	    private File HdmiDisplayEnable=null;
	    private File HdmiDisplayMode=null;
	    private File HdmiDisplayConnect=null;
        private Context context;
        private static final int DEF_HDMI_LCD_TIMEOUT_VALUE = 10;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        context=getActivity();
        if(!isDualMode()){
           addPreferencesFromResource(R.xml.hdmi_settings);
        }else{
           addPreferencesFromResource(R.xml.hdmi_settings_timeout);
           mHdmiLcd = (ListPreference)findPreference(KEY_HDMI_LCD);                
           mHdmiLcd.setOnPreferenceChangeListener(this);
           ContentResolver resolver = context.getContentResolver();
           long lcdTimeout = -1;
           if((lcdTimeout = Settings.System.getLong(resolver, Settings.System.HDMI_LCD_TIMEOUT,
              DEF_HDMI_LCD_TIMEOUT_VALUE)) > 0)
              {
                   lcdTimeout/=10;
              }
           mHdmiLcd.setValue(String.valueOf(lcdTimeout));
        }
        HdmiFile = new File("/sys/class/hdmi/hdmi-0/enable");
	    HdmiState = new File("/sys/class/hdmi/hdmi-0/state");
        HdmiDisplayEnable=new File("/sys/class/display/HDMI/enable");
        HdmiDisplayMode=new File("/sys/class/display/HDMI/mode");
        HdmiDisplayConnect=new File("sys/class/display/HDMI/connect");
	    mHdmi = (CheckBoxPreference)findPreference(KEY_HDMI);
		
	    mHdmiResolution = (ListPreference)findPreference(KEY_HDMI_RESOLUTION);
        mHdmiResolution.setOnPreferenceChangeListener(this);
		
         
    }

   private ContentObserver mHdmiTimeoutSettingObserver = new ContentObserver(new Handler()) {
       @Override
       public void onChange(boolean selfChange) {

              ContentResolver resolver = getActivity().getContentResolver();
              final long currentTimeout = Settings.System.getLong(resolver, Settings.System.HDMI_LCD_TIMEOUT,
                      -1);
              long lcdTimeout = -1;
              if((lcdTimeout = Settings.System.getLong(resolver, Settings.System.HDMI_LCD_TIMEOUT,
              DEF_HDMI_LCD_TIMEOUT_VALUE)) > 0)
              {
                   lcdTimeout/=10;
              }
              mHdmiLcd.setValue(String.valueOf(lcdTimeout));
       }
   };    

    @Override
    public void onResume() {
    	// TODO Auto-generated method stub
    	
    	super.onResume();
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.HDMI_LCD_TIMEOUT), true,
                mHdmiTimeoutSettingObserver);
    }

    public void onPause(){
        super.onPause();
       // getContentResolver().unregisterContentObserver(mHdmiTimeoutSettingObserver);
   }
   
   public void onDestroy(){
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mHdmiTimeoutSettingObserver);
   }

    private boolean isDualMode(){
		boolean isDualMode=false;
		File file=new File("/sys/class/graphics/fb0/dual_mode");
		if(file.exists()){
			try{
				FileReader fread=new FileReader(file);
				BufferedReader buffer=new BufferedReader(fread);
				String str=null;
				while((str=buffer.readLine())!=null){
					if(!str.equals("0")){
						isDualMode=true;
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return isDualMode;
    }

    protected static boolean isHdmiEnableDoubleScreen(File file){
        boolean isEnableDoubleScreen = false;
        if (file.exists()){
            try {
                  FileReader       fread = new FileReader(file);
                  BufferedReader   buffer = new BufferedReader(fread);
                  String           strPlug = "dual_disp=1";
                  String           str = null;

                  while ((str = buffer.readLine()) != null){
                    int length = str.length();
                    if((length == 11) && (str.equals(strPlug))){
                        isEnableDoubleScreen = true;
                        break;
                    }
                    else{
                        isEnableDoubleScreen = false;
                    }
                  }
            } catch (IOException e){
                Log.e(TAG, "IO Exception");
            }
        }
        return isEnableDoubleScreen;
    }
    
    protected void setHdmiConfig(File file, boolean enable) {

	if(SystemProperties.get("ro.board.platform","none").equals("rk29xx")){	
	   if (file.exists()) {
		try {
		   SharedPreferences  sharedPreferences1 = getActivity().getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
		   SharedPreferences.Editor editor = sharedPreferences1.edit();
		   String strDouble = "2";
		   String strChecked = "1";
		   String strUnChecked = "0";
		   RandomAccessFile rdf = null;
		   rdf = new RandomAccessFile(file, "rw");	
		   if (enable) {
		      if(isHdmiEnableDoubleScreen(HdmiState)){
			rdf.writeBytes(strDouble);
                        editor.putInt("enable", 2);
		      }else{
			rdf.writeBytes(strChecked);
			editor.putInt("enable", 1);
		      }
		   } else {
			rdf.writeBytes(strUnChecked);
			editor.putInt("enable", 0);
	           }
				
		   editor.commit();
		} catch (IOException re) {
		   Log.e(TAG, "IO Exception");
		}
	   } else {
		Log.i(TAG, "The File " + file + " is not exists");
	   }
	}else{
           if (file.exists()) {
		try {
		    Log.d(TAG,"setHdmiConfig");
		    SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
		    String strChecked = "1";
		    String strUnChecked = "0";		
		    RandomAccessFile rdf = null;
		    rdf = new RandomAccessFile(file, "rw");
		    if (enable) {
			rdf.writeBytes(strChecked);
			editor.putInt("enable", 1);
		    } else {
			rdf.writeBytes(strUnChecked);
			editor.putInt("enable", 0);
		    }
		    editor.commit();
		} catch (IOException re) {
		    Log.e(TAG, "IO Exception");
		    re.printStackTrace();
		}
	    } else {
		Log.i(TAG, "The File " + file + " is not exists");
	    }
	}
    }
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
    		Preference preference) {
    	// TODO Auto-generated method stub
    	if(SystemProperties.get("ro.board.platform","none").equals("rk29xx")){
	        	SharedPreferences  mPreferences = getPreferenceScreen().getSharedPreferences();
		        SharedPreferences  sharedPrefs = getActivity().getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
		        SharedPreferences.Editor  editor = sharedPrefs.edit();
	                boolean isConnected = isHdmiConnected(HdmiState);
	                int timeout = mPreferences.getInt("timeout", 30000);

			if (preference == mHdmi) {
				if (mHdmi.isChecked()) {
						setHdmiConfig(HdmiFile, true);
						mHdmi.setChecked(true);
						// save config
						editor.putInt("enable", 1);
				} else {
					setHdmiConfig(HdmiFile, false);
					mHdmi.setChecked(false);
					editor.putInt("enable", 0);

			    }
				editor.commit();
			} else if (preference == mHdcp) {
				String strHdcp = "hdmi_hdcp";
				if (mHdcp.isChecked()) {
					setHdmiOutputStyle(HdmiState, 1, strHdcp);
					mHdcp.setChecked(false);// not open HDCP now
				} else {
					setHdmiOutputStyle(HdmiState, 0, strHdcp);
					mHdcp.setChecked(false);
				}
			}
	  }else{	
        	SharedPreferences  mPreferences = getPreferenceScreen().getSharedPreferences();
	        SharedPreferences  sharedPrefs = getActivity().getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE);
	        SharedPreferences.Editor  editor = sharedPrefs.edit();
                boolean isConnected = isHdmiConnected(HdmiState);
                int timeout = mPreferences.getInt("timeout", 30000);
                Log.d(TAG, "onPreferenceThreeClick() ==>> isConnected=" + isConnected);

		if (preference == mHdmi) {
			if (mHdmi.isChecked()) {
 
				setHdmiConfig(HdmiDisplayEnable,true);
				mHdmi.setChecked(true);
				
			} else {
				setHdmiConfig(HdmiDisplayEnable, false);
				mHdmi.setChecked(false);
				editor.putInt("enable", 0);

				int mFlag = mPreferences.getInt("mAccelerometer", 0);
				if (mFlag != 0) {
			}
		    }
			editor.commit();
		} else if (preference == mHdcp) {
			String strHdcp = "hdmi_hdcp";
			if (mHdcp.isChecked()) {
				setHdmiOutputStyle(HdmiState, 1, strHdcp);
				mHdcp.setChecked(false);// not open HDCP now
			} else {
				setHdmiOutputStyle(HdmiState, 0, strHdcp);
				mHdcp.setChecked(false);
			}
		} 
     }
	 return true;
    }

    private void setHdmiLcdTimeout(int value){
        if(value != -1){
	    value = (value) * 10;
	    }
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.HDMI_LCD_TIMEOUT, value);
        } catch (NumberFormatException e) {
            Log.e(TAG, "could not persist hdmi lcd timeout setting", e);
        }
      }  
	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
		// TODO Auto-generated method stub
	if(SystemProperties.get("ro.board.platform","none").equals("rk29xx")){
		SharedPreferences.Editor editor = getActivity().getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE).edit();
		final String key = preference.getKey();
		if (KEY_HDMI_RESOLUTION.equals(key)){		
			
		    try {
			String strResolution = "hdmi_resolution";
			int value = Integer.parseInt((String) objValue);
			editor.putInt("resolution", value);		
			setHdmiOutputStyle(HdmiState, value, strResolution);
			} catch (NumberFormatException e){
				Log.e(TAG, "onPreferenceChanged hdmi_resolution setting error");
			}
		}

		if (KEY_HDMI_MODE.equals(key)){
		    try {
			String strMode = "hdmi_mode";
			int value = Integer.parseInt((String) objValue);
			editor.putInt("mode", value);
			setHdmiOutputStyle(HdmiState, value, strMode);
			} catch (NumberFormatException e){
				Log.e(TAG, "onPreferenceChanged hdmi_mode setting error");
			}
		}
			
		editor.commit();
			
		/*	if (KEY_HDMI_LCD.equals(key)){
				try {
					String strMode = "hdmi_display";
					int value = Integer.parseInt((String) objValue);
					//editor.putInt("enable", value);
					setHdmiLcdTimeout(value);
				} catch (NumberFormatException e){
					Log.e(TAG, "onPreferenceChanged hdmi_mode setting error");
				}
			} */
			
			
	}else{
	       if (true){
			SharedPreferences.Editor editor = getActivity().getPreferences(0).edit();
			final String key = preference.getKey();
			if (KEY_HDMI_RESOLUTION.equals(key)){
		            try {
				String strResolution = "hdmi_resolution";
				int value = Integer.parseInt((String) objValue);
				editor.putInt("resolution", value);
				setHdmiOutputStyle(HdmiDisplayMode, value, strResolution);
				} catch (NumberFormatException e){
					Log.e(TAG, "onPreferenceChanged hdmi_resolution setting error");
				}
			}

			if (KEY_HDMI_MODE.equals(key)){
				try {
					String strMode = "hdmi_mode";
					int value = Integer.parseInt((String) objValue);
					editor.putInt("mode", value);
					setHdmiOutputStyle(HdmiState, value, strMode);
				} catch (NumberFormatException e){
					Log.e(TAG, "onPreferenceChanged hdmi_mode setting error");
				}
			}
                        if (KEY_HDMI_LCD.equals(key)){
				try {
					String strMode = "hdmi_display";
					int value = Integer.parseInt((String) objValue);
					//editor.putInt("enable", value);
					setHdmiLcdTimeout(value);
				} catch (NumberFormatException e){
					Log.e(TAG, "onPreferenceChanged hdmi_mode setting error");
				}
			}
			editor.commit();
		}
	  }
	  return true;
	}
    
	
	public static boolean isHdmiConnected(File file){
        boolean isConnected = false;
        if (file.exists()){
            try {
                  FileReader       fread = new FileReader(file);
                  BufferedReader   buffer = new BufferedReader(fread);
                  String           strPlug = "plug=1";
                  String           str = null;

                  while ((str = buffer.readLine()) != null){
                    int length = str.length();
                   // if((length == 6) && (str.equals(strPlug))){
                    if(str.equals("1")){
                        isConnected = true;
                        break;
                    }
                    else{
                        //isConnected = false;
                    }
                  }
            } catch (IOException e){
                Log.e(TAG, "IO Exception");
            }
        }
        return isConnected;
    }
	
	protected void setHdmiOutputStyle(File file, int style, String string){
		
	   if(SystemProperties.get("ro.board.platform","none").equals("rk29xx"))
		{
			 if (file.exists()){
		 	   try {
				FileReader		fread  = new FileReader(file);
				BufferedReader 	buffer = new BufferedReader(fread);
				StringBuffer	strbuf = new StringBuffer(""); 	
				String			str = null;
				String  		substr = null;
				SharedPreferences.Editor editor = getActivity().getSharedPreferences("HdmiSettings", Context.MODE_PRIVATE).edit();
				if (string.equals("hdmi_resolution")){		
					substr = "resolution";
					if(style == 6)
					{
					}
					else
					{
						style--;
					}
					while ((str = buffer.readLine()) != null){
					    if (str.length() == 12){
						String  res = str.substring(0, 10);	
						if (substr.equals(res)){ 
						   String strValue = String.valueOf(style);
						   String s = substr + "=" + strValue;
						   strbuf.append(s + "\n");
						   //for save config
					           editor.putInt("resolution", Integer.parseInt(strValue));
						   editor.commit();
						}else{
						   strbuf.append(str + "\n");
						}
					     }else{
						   strbuf.append(str + "\n");
					     }
					}
				}

				if (string.equals("hdmi_mode")){
					boolean flag = false;
					substr = "mode";
						
					while ((str = buffer.readLine()) != null){ 
					    if (str.length() == 6){
						String res = str.substring(0, 4);
						if (substr.equals(res)){
							flag = true;
							String strValue = String.valueOf(style);
							String s = substr + "=" + strValue;
							strbuf.append(s + "\n");
							editor.putInt("mode",  Integer.parseInt(strValue));
							editor.commit();
						}else{
							strbuf.append(str + "\n");
						}
					     }else{
							strbuf.append(str + "\n");
				  	     }
					}
						
					if (!flag){
						String s = "mode=0";
						strbuf.append(s + "\n");
						editor.putInt("mode", 0);
						editor.commit();
					}
				}

				if (string.equals("hdmi_hdcp")){
					substr = "hdcp_on";
					while ((str = buffer.readLine()) != null){

						if (str.length() == 9){
							String res = str.substring(0, 7);
							if (substr.equals(res)){
								String strValue = String.valueOf(style);
								String s = substr + "=" + strValue;
								strbuf.append(s + "\n");
								editor.putInt("hdcp_on", Integer.parseInt(strValue));
								editor.commit();
							}else{
								strbuf.append(str + "\n");
								}
						}else{
							strbuf.append(str + "\n");
						}
					}
				}


				buffer.close();
				fread.close();

				//write into file
				File f = new File("/sys/class/hdmi/hdmi-0/state");
				OutputStream output = null;
				OutputStreamWriter outputWrite = null;
				PrintWriter	 print = null;
					
				try{
					output = new FileOutputStream(f);
					outputWrite = new OutputStreamWriter(output);
					print = new PrintWriter(outputWrite);
					print.print(strbuf.toString());
					print.flush();
					output.close();
				} catch (FileNotFoundException e) {
						e.printStackTrace();
				}
			} catch (IOException e){
				Log.e(TAG, "IO Exception");
			}
	          }else{
				Log.i(TAG, "The File " + file + " is not exists");
		}
			
	  }else{
		if (file.exists()){
		    try {
			FileReader fread  = new FileReader(file);
			BufferedReader 	buffer = new BufferedReader(fread);
			StringBuffer	strbuf = new StringBuffer(""); 	
			Log.d(TAG,"setHdmiOutputStyle");
			if (string.equals("hdmi_resolution")){
	                     switch(style){
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
				}
	
				
	
				buffer.close();
				fread.close();
	
				//write into file
				File f = new File("/sys/class/display/HDMI/mode");
				OutputStream output = null;
				OutputStreamWriter outputWrite = null;
				PrintWriter	 print = null;
					
				try{
				   output = new FileOutputStream(f);
				   outputWrite = new OutputStreamWriter(output);
				   print = new PrintWriter(outputWrite);
	                           Log.d(TAG,"strbuf="+strbuf.toString());
				   print.print(strbuf.toString());
				   //print.print("1920x1080p-50");
	                           print.flush();
				   output.close();
	                 	} catch (FileNotFoundException e) {
				   e.printStackTrace();
				}
			} catch (IOException e){
	                         e.printStackTrace();
	
				Log.e(TAG, "IO Exception");
			}
		}else{
				Log.i(TAG, "The File " + file + " is not exists");
		}
	}
 }
}
