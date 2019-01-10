package info.japos.pp.models.view;

import info.japos.pp.models.Jadwal;

/**
 * Created by HWAHYUDI on 16-Dec-17.
 */

public class SelectableJadwal extends Jadwal {
    private boolean isSelected = false;

    public SelectableJadwal(Jadwal jadwal, boolean isSelected) {
        super(jadwal.getId(), jadwal.getKelas(), jadwal.getLokasi(), jadwal.getPembinaan(), jadwal.getJamMulai(), jadwal.getJamSelesai(), jadwal.getStatus());
        this.isSelected = isSelected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
