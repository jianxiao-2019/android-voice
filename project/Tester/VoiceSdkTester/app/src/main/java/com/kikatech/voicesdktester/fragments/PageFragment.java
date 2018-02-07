package com.kikatech.voicesdktester.fragments;

import android.support.v4.app.Fragment;

/**
 * Created by ryanlin on 07/02/2018.
 */

public abstract class PageFragment extends Fragment {

    public abstract void onPagePause();
    public abstract void onPageResume();
}
