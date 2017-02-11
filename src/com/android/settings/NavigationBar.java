package com.android.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.provider.Settings;

public class NavigationBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener{
	 /** Called when the activity is first created. */

    private static final String KEY_IMMERSIVE_NAV="immersive_nav";
    private static final String KEY_RIGHTSIDE_NAV="rightside_nav";
    private static final String KEY_LEFTSIDE_NAV="leftside_nav";
    private static final String KEY_VOLUME_UP="volume_up";
    private static final String KEY_VOLUME_DOWN="volume_down";
    private static final String KEY_VOLUME_SLIDER="volume_slider";
    private static final String KEY_MUSIC_BUTTON="music_button";
    private static final String KEY_HOME_CLOCK_BUTTON="home_clock_button";
    private static final String KEY_NAVIGATION_BUTTON="navigation_button";
    private static final String KEY_PHONE_BUTTON="phone_button";
    private static final String KEY_AUTOMOTIVE_BUTTON="automotive_button";
    private static final String KEY_MEDIA_PREV_BUTTON="media_prev_button";
    private static final String KEY_MEDIA_PLAY_BUTTON="media_play_button";
    private static final String KEY_MEDIA_NEXT_BUTTON="media_next_button";
    private static final String KEY_SHOW_BACK_BUTTON="show_back_button";
    private static final String KEY_SHOW_HOME_BUTTON="show_home_button";
    private static final String KEY_SHOW_RECENTS_BUTTON="show_recents_button";

    private CheckBoxPreference mImmersiveNavbar;
    private CheckBoxPreference mRightsideNavbar;
    private CheckBoxPreference mLeftsideNavbar;
    private CheckBoxPreference mVolumeUp;
    private CheckBoxPreference mVolumeDown;
    private CheckBoxPreference mVolumeSlider;
    private CheckBoxPreference mMusic;
    private CheckBoxPreference mHomeClock;
    private CheckBoxPreference mNavigation;
    private CheckBoxPreference mPhone;
    private CheckBoxPreference mAutomotive;
    private CheckBoxPreference mMediaPrev;
    private CheckBoxPreference mMediaPlay;
    private CheckBoxPreference mMediaNext;
    private CheckBoxPreference mShowBack;
    private CheckBoxPreference mShowHome;
    private CheckBoxPreference mShowRecents;

	private Context mContext;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.navigationbar);
        
        mContext=getActivity();
        mImmersiveNavbar=(CheckBoxPreference)findPreference(KEY_IMMERSIVE_NAV);
        mRightsideNavbar=(CheckBoxPreference)findPreference(KEY_RIGHTSIDE_NAV);
        mLeftsideNavbar=(CheckBoxPreference)findPreference(KEY_LEFTSIDE_NAV);
        mVolumeUp=(CheckBoxPreference)findPreference(KEY_VOLUME_UP);
        mVolumeDown=(CheckBoxPreference)findPreference(KEY_VOLUME_DOWN);
        mVolumeSlider=(CheckBoxPreference)findPreference(KEY_VOLUME_SLIDER);
        mMusic=(CheckBoxPreference)findPreference(KEY_MUSIC_BUTTON);
        mHomeClock=(CheckBoxPreference)findPreference(KEY_HOME_CLOCK_BUTTON);
        mNavigation=(CheckBoxPreference)findPreference(KEY_NAVIGATION_BUTTON);
        mPhone=(CheckBoxPreference)findPreference(KEY_PHONE_BUTTON);
        mAutomotive=(CheckBoxPreference)findPreference(KEY_AUTOMOTIVE_BUTTON);
        mMediaPrev=(CheckBoxPreference)findPreference(KEY_MEDIA_PREV_BUTTON);
        mMediaPlay=(CheckBoxPreference)findPreference(KEY_MEDIA_PLAY_BUTTON);
        mMediaNext=(CheckBoxPreference)findPreference(KEY_MEDIA_NEXT_BUTTON);
        mShowBack=(CheckBoxPreference)findPreference(KEY_SHOW_BACK_BUTTON);
        mShowHome=(CheckBoxPreference)findPreference(KEY_SHOW_HOME_BUTTON);
        mShowRecents=(CheckBoxPreference)findPreference(KEY_SHOW_RECENTS_BUTTON);
        
        mImmersiveNavbar.setOnPreferenceChangeListener(this);
        mRightsideNavbar.setOnPreferenceChangeListener(this);
        mLeftsideNavbar.setOnPreferenceChangeListener(this);
        mVolumeUp.setOnPreferenceChangeListener(this);
        mVolumeDown.setOnPreferenceChangeListener(this);
        mVolumeSlider.setOnPreferenceChangeListener(this);
        mMusic.setOnPreferenceChangeListener(this);
        mHomeClock.setOnPreferenceChangeListener(this);
        mNavigation.setOnPreferenceChangeListener(this);
        mPhone.setOnPreferenceChangeListener(this);
        mAutomotive.setOnPreferenceChangeListener(this);
        mMediaPrev.setOnPreferenceChangeListener(this);
        mMediaPlay.setOnPreferenceChangeListener(this);
        mMediaNext.setOnPreferenceChangeListener(this);
        mShowBack.setOnPreferenceChangeListener(this);
        mShowHome.setOnPreferenceChangeListener(this);
        mShowRecents.setOnPreferenceChangeListener(this);

        boolean checked = Settings.System.getBoolean(
             mContext.getContentResolver(),Settings.System.IMMERSIVE_MODE_NAV, false);
        mImmersiveNavbar.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.ENABLE_RIGHT_NAVBAR, false);
        mRightsideNavbar.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.ENABLE_LEFT_NAVBAR, false);
        mLeftsideNavbar.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_VOLUME_UP, false);
        mVolumeUp.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_VOLUME_DOWN, false);
        mVolumeDown.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_VOLUME_SLIDER, true);
        mVolumeSlider.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_MUSIC, true);
        mMusic.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_HOME_CLOCK, true);
        mHomeClock.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_NAVIGATION, true);
        mNavigation.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_PHONE, true);
        mPhone.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_AUTOMOTIVE, true);
        mAutomotive.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_MEDIA_PREV, false);
        mMediaPrev.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_MEDIA_PLAY, false);
        mMediaPlay.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_MEDIA_NEXT, false);
        mMediaNext.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_BACK, true);
        mShowBack.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_HOME, false);
        mShowHome.setChecked(checked);
        checked = Settings.System.getBoolean(
                mContext.getContentResolver(),Settings.System.NAVBAR_SHOW_RECENTS, false);
        mShowRecents.setChecked(checked);
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
        if(preference==mRightsideNavbar){
            boolean checked=mRightsideNavbar.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.ENABLE_RIGHT_NAVBAR, checked);
            if (checked) {
                mLeftsideNavbar.setChecked(false);
                Settings.System.putBoolean(getContentResolver(), Settings.System.ENABLE_LEFT_NAVBAR, false);
            }
        }
        if(preference==mLeftsideNavbar){
            boolean checked=mLeftsideNavbar.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.ENABLE_LEFT_NAVBAR, checked);
            if (checked) {
                mRightsideNavbar.setChecked(false);
                Settings.System.putBoolean(getContentResolver(), Settings.System.ENABLE_RIGHT_NAVBAR, false);
            }
        }
        if(preference==mVolumeUp){
            boolean checked=mVolumeUp.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_VOLUME_UP, checked);
        }
        if(preference==mVolumeDown){
            boolean checked=mVolumeDown.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_VOLUME_DOWN, checked);
        }
        if(preference==mVolumeSlider){
            boolean checked=mVolumeSlider.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_VOLUME_SLIDER, checked);
        }
        if(preference==mMusic){
            boolean checked=mMusic.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_MUSIC, checked);
        }
        if(preference==mHomeClock){
            boolean checked=mHomeClock.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_HOME_CLOCK, checked);
        }
        if(preference==mNavigation){
            boolean checked=mNavigation.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_NAVIGATION, checked);
        }
        if(preference==mPhone){
            boolean checked=mPhone.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_PHONE, checked);
        }
        if(preference==mAutomotive){
            boolean checked=mAutomotive.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_AUTOMOTIVE, checked);
        }
        if(preference==mMediaNext){
            boolean checked=mMediaNext.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_MEDIA_NEXT, checked);
        }
        if(preference==mMediaPlay){
            boolean checked=mMediaPlay.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_MEDIA_PLAY, checked);
        }
        if(preference==mMediaPrev){
            boolean checked=mMediaPrev.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_MEDIA_PREV, checked);
        }
        if(preference==mShowBack){
            boolean checked=mShowBack.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_BACK, checked);
        }
        if(preference==mShowHome){
            boolean checked=mShowHome.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_HOME, checked);
        }
        if(preference==mShowRecents){
            boolean checked=mShowRecents.isChecked();
            Settings.System.putBoolean(getContentResolver(), Settings.System.NAVBAR_SHOW_RECENTS, checked);
        }
		return true;
	}

}
