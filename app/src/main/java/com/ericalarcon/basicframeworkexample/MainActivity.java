package com.ericalarcon.basicframeworkexample;

import android.app.Fragment;

import com.ericalarcon.basicframework.Templates.NavigationActivity;
import com.ericalarcon.basicframeworkexample.NavigationLevelOne.AppTabsFragment;

public class MainActivity extends NavigationActivity {

    @Override
    public Fragment firstFragment() {
        return new AppTabsFragment();
    }

    @Override
    public Boolean showBackButtonInFirstFragment() {
        return false;
    }

    @Override
    public Boolean showMasterDetailLayoutInTablets() {
        return true;
    }
}
