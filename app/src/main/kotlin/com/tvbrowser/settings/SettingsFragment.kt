package com.tvbrowser.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SeekBarPreference
import com.tvbrowser.R

class SettingsFragment : PreferenceFragmentCompat() {
    private lateinit var preferencesManager: PreferencesManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
        preferencesManager = PreferencesManager(requireContext())
        bindPercentSummaries()
    }

    private fun bindPercentSummaries() {
        findPreference<SeekBarPreference>("text_size")?.apply {
            value = preferencesManager.getTextSize()
            summary = getString(R.string.text_size_current, value)
            setOnPreferenceChangeListener { _, newValue ->
                val percent = (newValue as Int).coerceIn(50, 200)
                preferencesManager.setTextSize(percent)
                summary = getString(R.string.text_size_current, percent)
                true
            }
        }

        findPreference<SeekBarPreference>("screen_size")?.apply {
            value = preferencesManager.getScreenSize()
            summary = getString(R.string.screen_size_current, value)
            setOnPreferenceChangeListener { _, newValue ->
                val percent = (newValue as Int).coerceIn(25, 100)
                preferencesManager.setScreenSize(percent)
                summary = getString(R.string.screen_size_current, percent)
                true
            }
        }

        findPreference<Preference>("pc_mode_enabled")?.setOnPreferenceChangeListener { preference, newValue ->
            preference.summary = if (newValue as Boolean) {
                getString(R.string.pc_mode_enabled_summary)
            } else {
                getString(R.string.pc_mode_summary)
            }
            true
        }
    }
}
