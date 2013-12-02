/**
 * Mupen64PlusAE, an N64 emulator for the Android platform
 * 
 * Copyright (C) 2013 Paul Lamb
 * 
 * This file is part of Mupen64PlusAE.
 * 
 * Mupen64PlusAE is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * Mupen64PlusAE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Mupen64PlusAE. If
 * not, see <http://www.gnu.org/licenses/>.
 * 
 * Authors: littleguy77
 */
package paulscode.android.mupen64plusae;

import java.io.File;

import paulscode.android.mupen64plusae.input.TouchController;
import paulscode.android.mupen64plusae.persistent.AppData;
import paulscode.android.mupen64plusae.persistent.UserPrefs;
import paulscode.android.mupen64plusae.util.CrashTester;
import paulscode.android.mupen64plusae.util.DeviceUtil;
import paulscode.android.mupen64plusae.util.PrefUtil;
import paulscode.android.mupen64plusae.util.Prompt;
import paulscode.android.mupen64plusae.util.Prompt.PromptConfirmListener;
import android.annotation.TargetApi;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;

public class SettingsGlobalActivity extends PreferenceActivity implements OnPreferenceClickListener,
        OnSharedPreferenceChangeListener
{
    // These constants must match the keys used in res/xml/preferences_global.xml
    
    private static final String SCREEN_SETTINGS_GLOBAL = "screenSettingsGlobal";
    private static final String SCREEN_DISPLAY = "screenDisplay";
    private static final String SCREEN_TOUCHPAD = "screenTouchpad";
    
    private static final String ACTION_DEVICE_INFO = "actionDeviceInfo";
    private static final String ACTION_CRASH_TEST = "actionCrashTest";
    private static final String ACTION_RELOAD_ASSETS = "actionReloadAssets";
    private static final String ACTION_RESET_USER_PREFS = "actionResetUserPrefs";
    
    private static final String DISPLAY_POSITION = "displayPosition";
    private static final String DISPLAY_RESOLUTION = "displayResolution";
    private static final String DISPLAY_SCALING = "displayScaling";
    private static final String DISPLAY_IMMERSIVE_MODE = "displayImmersiveMode";
    private static final String DISPLAY_ACTION_BAR_TRANSPARENCY = "displayActionBarTransparency";
    private static final String TOUCHSCREEN_AUTO_HOLDABLES = "touchscreenAutoHoldables";
    private static final String TOUCHSCREEN_STYLE = "touchscreenStyle";
    private static final String TOUCHSCREEN_HEIGHT = "touchscreenHeight";
    private static final String TOUCHSCREEN_LAYOUT = "touchscreenLayout";
    private static final String PATH_CUSTOM_TOUCHSCREEN = "pathCustomTouchscreen";
    private static final String TOUCHPAD_ENABLED = "touchpadEnabled";
    private static final String TOUCHPAD_LAYOUT = "touchpadLayout";
    private static final String AUDIO_PLUGIN = "audioPlugin";
    private static final String AUDIO_BUFFER_SIZE = "audioBufferSize";
    private static final String AUDIO_SWAP_CHANNELS = "audioSwapChannels";
    private static final String NAVIGATION_MODE = "navigationMode";
    private static final String ACRA_USER_EMAIL = "acra.user.email";
    
    // App data and user preferences
    private AppData mAppData = null;
    private UserPrefs mUserPrefs = null;
    
    @SuppressWarnings( "deprecation" )
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        
        // Get app data and user preferences
        mAppData = new AppData( this );
        mUserPrefs = new UserPrefs( this );
        mUserPrefs.enforceLocale( this );
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( this );
        
        // Ensure that any missing preferences are populated with defaults (e.g. preference added to
        // new release)
        PreferenceManager.setDefaultValues( this, R.xml.preferences_global, false );
        
        // Ensure that selected plugin names and other list preferences are valid
        Resources res = getResources();
        PrefUtil.validateListPreference( res, prefs, DISPLAY_POSITION, R.string.displayPosition_default,
                R.array.displayPosition_values );
        PrefUtil.validateListPreference( res, prefs, DISPLAY_RESOLUTION, R.string.displayResolution_default,
                R.array.displayResolution_values );
        PrefUtil.validateListPreference( res, prefs, DISPLAY_SCALING, R.string.displayScaling_default,
                R.array.displayScaling_values );
        PrefUtil.validateListPreference( res, prefs, TOUCHSCREEN_STYLE, R.string.touchscreenStyle_default,
                R.array.touchscreenStyle_values );
        PrefUtil.validateListPreference( res, prefs, TOUCHSCREEN_HEIGHT, R.string.touchscreenHeight_default,
                R.array.touchscreenHeight_values );
        PrefUtil.validateListPreference( res, prefs, TOUCHSCREEN_LAYOUT, R.string.touchscreenLayout_default,
                R.array.touchscreenLayout_values );
        PrefUtil.validateListPreference( res, prefs, TOUCHPAD_LAYOUT, R.string.touchpadLayout_default,
                R.array.touchpadLayout_values );
        PrefUtil.validateListPreference( res, prefs, AUDIO_PLUGIN, R.string.audioPlugin_default,
                R.array.audioPlugin_values );
        PrefUtil.validateListPreference( res, prefs, AUDIO_BUFFER_SIZE, R.string.audioBufferSize_default,
                R.array.audioBufferSize_values );
        PrefUtil.validateListPreference( res, prefs, NAVIGATION_MODE, R.string.navigationMode_default,
                R.array.navigationMode_values );
        
        // Load user preference menu structure from XML and update view
        addPreferencesFromResource( R.xml.preferences_global );
        
        // Refresh the preference data wrapper
        mUserPrefs = new UserPrefs( this );
        
        // Handle certain menu items that require extra processing or aren't actually preferences
        PrefUtil.setOnPreferenceClickListener( this, ACTION_DEVICE_INFO, this );
        PrefUtil.setOnPreferenceClickListener( this, ACTION_RELOAD_ASSETS, this );
        PrefUtil.setOnPreferenceClickListener( this, ACTION_RESET_USER_PREFS, this );
        
        // Handle crash tests in a particular way (see CrashTester for more info)
        findPreference( ACTION_CRASH_TEST ).setOnPreferenceClickListener( new CrashTester( this ) );
        
        // Hide certain categories altogether if they're not applicable. Normally we just rely on
        // the built-in dependency disabler, but here the categories are so large that hiding them
        // provides a better user experience.
        if( !AppData.IS_KITKAT )
            PrefUtil.removePreference( this, SCREEN_DISPLAY, DISPLAY_IMMERSIVE_MODE );
        
        if( !mUserPrefs.isActionBarAvailable )
            PrefUtil.removePreference( this, SCREEN_DISPLAY, DISPLAY_ACTION_BAR_TRANSPARENCY );
        
        if( !mAppData.hardwareInfo.isXperiaPlay )
            PrefUtil.removePreference( this, SCREEN_SETTINGS_GLOBAL, SCREEN_TOUCHPAD );
    }
    
    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        sharedPreferences.unregisterOnSharedPreferenceChangeListener( this );
    }
    
    @Override
    protected void onResume()
    {
        super.onResume();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences( this );
        sharedPreferences.registerOnSharedPreferenceChangeListener( this );
        refreshViews();
    }
    
    @Override
    public void finish()
    {
        // Disable transition animation to behave like any other screen in the menu hierarchy
        super.finish();
        overridePendingTransition( 0, 0 );
    }
    
    @Override
    public void onSharedPreferenceChanged( SharedPreferences sharedPreferences, String key )
    {
        if( key.equals( TOUCHPAD_ENABLED ) || key.equals( NAVIGATION_MODE ) )
        {
            // Rebuild the menu; the easiest way is to simply restart the activity
            finish();
            startActivity( getIntent() );
        }
        else
        {
            // Just refresh the preference screens in place
            refreshViews();
        }
    }
    
    @TargetApi( 9 )
    @SuppressWarnings( "deprecation" )
    private void refreshViews()
    {
        // Refresh the preferences object
        mUserPrefs = new UserPrefs( this );
        
        // Enable the auto-holdables pref if auto-hold is not disabled
        PrefUtil.enablePreference( this, TOUCHSCREEN_AUTO_HOLDABLES, mUserPrefs.isTouchscreenEnabled
                && mUserPrefs.touchscreenAutoHold != TouchController.AUTOHOLD_METHOD_DISABLED );
        
        // Enable the custom touchscreen prefs under certain conditions
        PrefUtil.enablePreference( this, PATH_CUSTOM_TOUCHSCREEN, mUserPrefs.isTouchscreenEnabled
                && mUserPrefs.isTouchscreenCustom );
        PrefUtil.enablePreference( this, TOUCHSCREEN_STYLE, mUserPrefs.isTouchscreenEnabled
                && !mUserPrefs.isTouchscreenCustom );
        PrefUtil.enablePreference( this, TOUCHSCREEN_HEIGHT, mUserPrefs.isTouchscreenEnabled
                && !mUserPrefs.isTouchscreenCustom );
        
        // Enable audio prefs if audio is enabled
        PrefUtil.enablePreference( this, AUDIO_BUFFER_SIZE, mUserPrefs.audioPlugin.enabled );
        PrefUtil.enablePreference( this, AUDIO_SWAP_CHANNELS, mUserPrefs.audioPlugin.enabled );
        
        // Update the summary text in a particular way for ACRA user info
        EditTextPreference pref = (EditTextPreference) findPreference( ACRA_USER_EMAIL );
        String value = pref.getText();
        if( TextUtils.isEmpty( value ) )
            pref.setSummary( getString( R.string.acraUserEmail_summary ) );
        else
            pref.setSummary( value );
    }
    
    @Override
    public boolean onPreferenceClick( Preference preference )
    {
        // Handle the clicks on certain menu items that aren't actually preferences
        String key = preference.getKey();
        
        if( key.equals( ACTION_DEVICE_INFO ) )
            actionDeviceInfo();
        
        else if( key.equals( ACTION_RELOAD_ASSETS ) )
            actionReloadAssets();
        
        else if( key.equals( ACTION_RESET_USER_PREFS ) )
            actionResetUserPrefs();
        
        else
            // Let Android handle all other preference clicks
            return false;
        
        // Tell Android that we handled the click
        return true;
    }
    
    private void actionDeviceInfo()
    {
        String title = getString( R.string.actionDeviceInfo_title );
        String message = DeviceUtil.getCpuInfo();
        new Builder( this ).setTitle( title ).setMessage( message ).create().show();
    }
    
    private void actionReloadAssets()
    {
        mAppData.putAssetVersion( 0 );
        startActivity( new Intent( this, SplashActivity.class ) );
        finish();
    }
    
    private void actionResetUserPrefs()
    {
        String title = getString( R.string.confirm_title );
        String message = getString( R.string.actionResetUserPrefs_popupMessage );
        Prompt.promptConfirm( this, title, message, new PromptConfirmListener()
        {
            @Override
            public void onConfirm()
            {
                // Reset the user preferences
                SharedPreferences preferences = PreferenceManager
                        .getDefaultSharedPreferences( SettingsGlobalActivity.this );
                preferences.unregisterOnSharedPreferenceChangeListener( SettingsGlobalActivity.this );
                preferences.edit().clear().commit();
                PreferenceManager.setDefaultValues( SettingsGlobalActivity.this, R.xml.preferences, true );
                
                // Also reset any manual overrides the user may have made in the config file
                File configFile = new File( mAppData.mupen64plus_cfg );
                if( configFile.exists() )
                    configFile.delete();
                
                // Rebuild the menu system by restarting the activity
                finish();
                startActivity( getIntent() );
            }
        } );
    }
}
