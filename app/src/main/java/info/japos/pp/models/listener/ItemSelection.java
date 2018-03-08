package info.japos.pp.models.listener;

import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by HWAHYUDI on 03-Mar-18.
 */

public interface ItemSelection {
    void itemSelectionChanged(Boolean isAnyItemSelected);
    void menuSelection(MenuItem menuItem, @Nullable Integer reffId, Object ... adds);
}
