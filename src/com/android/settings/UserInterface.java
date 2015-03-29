package com.android.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

public class UserInterface extends SettingsPreferenceFragment implements OnPreferenceChangeListener{
	 /** Called when the activity is first created. */

    private static final String KEY_IMMERSIVE_NAV="immersive_nav";
    private static final String KEY_IMMERSIVE_SB="immersive_sb";
    private static final String KEY_MEDIA_MODE="media_mode";

	private CheckBoxPreference mImmersiveStatusbar;
    private CheckBoxPreference mImmersiveNavbar;
    private CheckBoxPreference mMediaMode;

	private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.userinterface);
        
        mContext=getActivity();
        mImmersiveNavbar=(CheckBoxPreference)findPreference(KEY_IMMERSIVE_NAV);
        mImmersiveStatusbar=(CheckBoxPreference)findPreference(KEY_IMMERSIVE_SB);
        mMediaMode=(CheckBoxPreference)findPreference(KEY_MEDIA_MODE);
        
        mImmersiveNavbar.setOnPreferenceChangeListener(this);
        mImmersiveStatusbar.setOnPreferenceChangeListener(this);
        mMediaMode.setOnPreferenceChangeListener(this);

        boolean checked = Settings.System.getBoolean(
             mContext.getContentResolver(),Settings.System.IMMERSIVE_MODE_NAV, false);
        mImmersiveNavbar.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.IMMERSIVE_MODE_SB, false);
        mImmersiveStatusbar.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_MEDIA_MODE, false);
        mMediaMode.setChecked(checked);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		if(preference==mImmersiveNavbar){
			boolean checked=mImmersiveNavbar.isChecked();
			Settings.System.putBoolean(getContentResolver(), Settings.System.IMMERSIVE_MODE_NAV, checked);
		}
        if(preference==mImmersiveStatusbar){
            boolean checked=mImmersiveStatusbar.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.IMMERSIVE_MODE_SB, checked);
        }
        if(preference==mMediaMode){
            boolean checked=mMediaMode.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_MEDIA_MODE, checked);
        }
		return true;
	}

}
