package com.dongah.smartcharger.basefunction;

import androidx.fragment.app.Fragment;

import com.dongah.smartcharger.MainActivity;
import com.dongah.smartcharger.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FragmentCurrent {
    private static final Logger logger = LoggerFactory.getLogger(FragmentCurrent.class);

    public FragmentCurrent() {
    }

    public Fragment getCurrentFragment() {
        return ((MainActivity) MainActivity.mContext).getSupportFragmentManager().findFragmentById(R.id.frameBody);
    }
}
