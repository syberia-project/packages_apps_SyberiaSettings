package com.syberia.settings.fragments;
 
import android.app.ActionBar; 
import android.content.Context;
import android.os.Bundle; 
import android.os.UserHandle; 
import android.provider.SearchIndexableResource;
import android.provider.Settings; 
import android.support.v14.preference.SwitchPreference; 
import android.support.v7.preference.Preference; 
 
import com.android.internal.logging.nano.MetricsProto; 
import com.android.internal.util.hwkeys.ActionUtils; 
import com.android.settings.R; 

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.settings.SettingsPreferenceFragment; 
import com.syberia.settings.preference.SecureSettingSeekBarPreference; 

import java.util.ArrayList;
import java.util.List;
 
public class EdgeGesturesSettings extends SettingsPreferenceFragment implements 
        Preference.OnPreferenceChangeListener, Indexable { 
 
    public static final String EDGE_GESTURES_ENABLED = "edge_gestures_enabled"; 
    public static final String EDGE_GESTURES_SCREEN_PERCENT = "edge_gestures_back_screen_percent"; 
 
    private String previousTitle; 
 
    private SwitchPreference enabledPreference; 
    private SecureSettingSeekBarPreference screenPercentPreference; 
 
    @Override 
    public void onCreate(Bundle savedInstanceState) { 
        super.onCreate(savedInstanceState); 
 
        addPreferencesFromResource(R.xml.edge_gestures); 
 
        enabledPreference = (SwitchPreference) findPreference(EDGE_GESTURES_ENABLED); 
        enabledPreference.setOnPreferenceChangeListener(this); 
 
        screenPercentPreference = (SecureSettingSeekBarPreference) findPreference(EDGE_GESTURES_SCREEN_PERCENT); 
        int percent = Settings.Secure.getIntForUser(getContentResolver(), Settings.Secure.EDGE_GESTURES_BACK_SCREEN_PERCENT, 60, UserHandle.USER_CURRENT); 
        screenPercentPreference.setValue(percent); 
    } 
 
    @Override 
    public void onStart() { 
        super.onStart(); 
 
        ActionBar actionBar = getActivity().getActionBar(); 
        previousTitle = actionBar.getTitle().toString(); 
        actionBar.setTitle(R.string.edge_gestures_title); 
    } 
 
    @Override 
    public void onStop() { 
        super.onStop(); 
 
        ActionBar actionBar = getActivity().getActionBar(); 
        actionBar.setTitle(previousTitle); 
    } 
 
    @Override 
    public int getMetricsCategory() { 
        return MetricsProto.MetricsEvent.SYBERIA; 
    } 
 
    @Override 
    public boolean onPreferenceChange(Preference preference, Object newValue) { 
        if (preference == enabledPreference) { 
            int enabled = ((boolean) newValue) ? 1 : 0; 
 
            if (enabled == 1) { 
                Settings.Secure.putInt(getContentResolver(), 
                        Settings.Secure.NAVIGATION_BAR_VISIBLE, 0);
                Settings.System.putInt(getContentResolver(),
                        Settings.System.OMNI_USE_BOTTOM_GESTURE_NAVIGATION, 0);
            } else { 
                if (ActionUtils.hasNavbarByDefault(getPrefContext())) { 
                    Settings.Secure.putInt(getContentResolver(), 
                            Settings.Secure.NAVIGATION_BAR_VISIBLE, 
                            1); 
                } 
            }
            return true; 
        }
        return false; 
    } 

    /**
     * For Search.
     */
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER =
            new BaseSearchIndexProvider() {

                @Override
                public List<SearchIndexableResource> getXmlResourcesToIndex(Context context,
                        boolean enabled) {
                    final ArrayList<SearchIndexableResource> result = new ArrayList<>();

                    final SearchIndexableResource sir = new SearchIndexableResource(context);
                    sir.xmlResId = R.xml.edge_gestures;
                    result.add(sir);
                    return result;
                }

                @Override
                public List<String> getNonIndexableKeys(Context context) {
                    final List<String> keys = super.getNonIndexableKeys(context);
                    return keys;
                }
    };
} 