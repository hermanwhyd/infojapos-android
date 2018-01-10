package info.japos.pp.helper;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import info.japos.pp.R;
import info.japos.pp.activities.PresensiActivity;
import info.japos.pp.models.enums.PresensiKet;

/**
 * Created by HWAHYUDI on 02-Jan-18.
 */

public class ToolbarPresensiActionModeCallback implements ActionMode.Callback {
    private AppCompatActivity activity;

    public ToolbarPresensiActionModeCallback(AppCompatActivity activity) {
        this.activity = activity;
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        actionMode.getMenuInflater().inflate(R.menu.presensi_options_actionmode, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
//        menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        menu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
//        menu.findItem(R.id.action_forward).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.mn_sa_hadir:
                ((PresensiActivity)activity).markAllSelected(PresensiKet.H);
                break;
            case R.id.mn_sa_alpa:
                AppCompatActivity presensiAct = new PresensiActivity();
                ((PresensiActivity)activity).markAllSelected(PresensiKet.A);
                break;
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        ((PresensiActivity)activity).setNullToActionMode();
    }

}
