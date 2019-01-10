package info.japos.pp.models.statistik;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HWAHYUDI on 18-Feb-18.
 */

public class StatistikGeneral {
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("statistik")
    @Expose
    private StatistikKehadiran statistik;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public StatistikKehadiran getStatistik() {
        return statistik;
    }

    public void setStatistik(StatistikKehadiran statistik) {
        this.statistik = statistik;
    }
}
