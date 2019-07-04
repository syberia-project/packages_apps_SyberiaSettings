/*
 * Copyright © 2018 Syberia Project
 * Date: 22.08.2018
 * Time: 21:21
 * Author: @alexxxdev <alexxxdev@ya.ru>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.syberia.settings.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.support.v4.app.Fragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;

import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;

import com.android.settings.SettingsPreferenceFragment;

import com.syberia.settings.preference.SystemSettingSwitchPreference;
import com.syberia.settings.preference.CustomSeekBarPreference;

import com.android.internal.logging.nano.MetricsProto;

import java.util.ArrayList;
import java.util.List;

public class StatusBarSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener, Indexable {

	private CustomSeekBarPreference mThreshold;
	private SystemSettingSwitchPreference mNetMonitor;

	private ListPreference mTickerAnimationMode;
	private CustomSeekBarPreference mTickerAnimationDuration;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        addPreferencesFromResource(R.xml.statusbar_settings);

        PreferenceScreen prefSet = getPreferenceScreen();

		final ContentResolver resolver = getActivity().getContentResolver();
		boolean isNetMonitorEnabled = Settings.System.getIntForUser(resolver,
		    Settings.System.NETWORK_TRAFFIC_STATE, 1, UserHandle.USER_CURRENT) == 1;
		mNetMonitor = (SystemSettingSwitchPreference) findPreference("network_traffic_state");
		mNetMonitor.setChecked(isNetMonitorEnabled);
		mNetMonitor.setOnPreferenceChangeListener(this);
		int value = Settings.System.getIntForUser(resolver,
		    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, 1, UserHandle.USER_CURRENT);
		mThreshold = (CustomSeekBarPreference) findPreference("network_traffic_autohide_threshold");
		mThreshold.setValue(value);
		mThreshold.setOnPreferenceChangeListener(this);
		mThreshold.setEnabled(isNetMonitorEnabled);

        mTickerAnimationMode = (ListPreference) findPreference("status_bar_ticker_animation_mode");
        mTickerAnimationMode.setOnPreferenceChangeListener(this);
        int tickerAnimationMode = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TICKER_ANIMATION_MODE, 0);
        mTickerAnimationMode.setValue(String.valueOf(tickerAnimationMode));
        updateTickerAnimationModeSummary(tickerAnimationMode);

	mTickerAnimationDuration = (CustomSeekBarPreference) findPreference("status_bar_ticker_tick_duration");
        int tickerAnimationDuration = Settings.System.getIntForUser(resolver,
                Settings.System.STATUS_BAR_TICKER_TICK_DURATION, 3000, UserHandle.USER_CURRENT);
        mTickerAnimationDuration.setValue(tickerAnimationDuration);
        mTickerAnimationDuration.setOnPreferenceChangeListener(this);

    }

    private void updateTickerAnimationModeSummary(int value) {
        Resources res = getResources();
         if (value == 0) {
            // Fade
             mTickerAnimationMode.setSummary(res.getString(R.string.ticker_animation_mode_alpha_fade));
        } else if (value == 1) {
            // Scroll
            mTickerAnimationMode.setSummary(res.getString(R.string.ticker_animation_mode_scroll));
        }
    }

	@Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
         if (preference == mNetMonitor) {
            boolean value = (Boolean) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_STATE, value ? 1 : 0,
                    UserHandle.USER_CURRENT);
            mNetMonitor.setChecked(value);
            mThreshold.setEnabled(value);
            return true;
        } else if (preference == mThreshold) {
            int val = (Integer) objValue;
            Settings.System.putIntForUser(getContentResolver(),
                    Settings.System.NETWORK_TRAFFIC_AUTOHIDE_THRESHOLD, val,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mTickerAnimationMode) {
            int tickerAnimationMode = Integer.valueOf((String) objValue);
            Settings.System.putInt(getActivity().getContentResolver(),
		     Settings.System.STATUS_BAR_TICKER_ANIMATION_MODE, tickerAnimationMode);
            updateTickerAnimationModeSummary(tickerAnimationMode);
            return true;
        } else if (preference == mTickerAnimationDuration) {
            int tickerAnimationDuration = (Integer) objValue;
            Settings.System.putIntForUser(getActivity().getContentResolver(),
                    Settings.System.STATUS_BAR_TICKER_TICK_DURATION, tickerAnimationDuration,
                    UserHandle.USER_CURRENT);
            return true;
	}
        return false;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.SYBERIA;
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
                    sir.xmlResId = R.xml.statusbar_settings;
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