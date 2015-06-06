package com.android.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener{
	 /** Called when the activity is first created. */

    private static final String KEY_IMMERSIVE_SB="immersive_sb";
    private static final String KEY_ENABLE_HEADS_UP="enable_heads_up";


	private CheckBoxPreference mImmersiveStatusbar;
    private CheckBoxPreference mEnableHeadsUp;

	private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.statusbar);
        
        mContext=getActivity();
        mImmersiveStatusbar=(CheckBoxPreference)findPreference(KEY_IMMERSIVE_SB);
        mEnableHeadsUp=(CheckBoxPreference)findPreference(KEY_ENABLE_HEADS_UP);

        mImmersiveStatusbar.setOnPreferenceChangeListener(this);
        mEnableHeadsUp.setOnPreferenceChangeListener(this);

        boolean checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.IMMERSIVE_MODE_SB, false);
        mImmersiveStatusbar.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.ENABLE_HEADS_UP, false);
        mEnableHeadsUp.setChecked(checked);

    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        return true;
    }

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		// TODO Auto-generated method stub
		if(preference==mImmersiveStatusbar){
            boolean checked=mImmersiveStatusbar.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.IMMERSIVE_MODE_SB, checked);
        }
        if(preference==mEnableHeadsUp) {
            boolean checked = mEnableHeadsUp.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.ENABLE_HEADS_UP, checked);
        }
		return true;
	}

}
