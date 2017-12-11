package cn.iscas.xlab.uxbot;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

/**
 * Created by lisongting on 2017/10/16.
 */

public class SettingFragment extends PreferenceFragment {

    private static final String TAG = "SettingFragment";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_settings);
    }
}
