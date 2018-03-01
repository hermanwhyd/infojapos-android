package info.japos.pp.models.statistik;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HWAHYUDI on 18-Feb-18.
 */

public class StatistikKehadiran {
    @SerializedName("H")
    @Expose
    private Integer hadir;
    @SerializedName("A")
    @Expose
    private Integer alpa;
    @SerializedName("I")
    @Expose
    private Integer izin;

    public Integer getHadir() {
        return hadir;
    }

    public void setHadir(Integer hadir) {
        this.hadir = hadir;
    }

    public Integer getAlpa() {
        return alpa;
    }

    public void setAlpa(Integer alpa) {
        this.alpa = alpa;
    }

    public Integer getIzin() {
        return izin;
    }

    public void setIzin(Integer izin) {
        this.izin = izin;
    }

    public Integer getTotal() {
        return getHadir().intValue() + getAlpa().intValue() + getIzin().intValue();
    }
}
