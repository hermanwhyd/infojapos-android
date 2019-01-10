package info.japos.pp.adapters;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import info.japos.pp.fragments.ChartSttFragment;
import info.japos.pp.fragments.TableviewSttFragment;

/**
 * Created by HWAHYUDI on 07-Mar-18.
 */

public class TabsPagerAdapter extends FragmentPagerAdapter {
    private Bundle bundle;
    public TabsPagerAdapter(FragmentManager fm, Bundle bundle) {
        super(fm);
        this.bundle = bundle;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                final ChartSttFragment f0 = new ChartSttFragment();
                f0.setArguments(this.bundle);
                return f0;
            case 1:
                final TableviewSttFragment f1 = new TableviewSttFragment();
                f1.setArguments(this.bundle);
                return f1;
        }

        return null;
    }

    @Override
    public int getCount() {
        // get item count - equal to number of tabs
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Diagram";
            case 1:
                return "Tabel";
        }
        return null;
    }
}
