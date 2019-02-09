package info.japos.pp.models.statistik;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HWAHYUDI on 21-Feb-18.
 */

public class StatistikKelas {
    @SerializedName("id")
    @Expose
    private int idKelas;
    @SerializedName("nama_kelas")
    @Expose
    private String namaKelas;
    @SerializedName("lv_pembinaan")
    @Expose
    private String pembinaan;
    @SerializedName("statistik_list")
    @Expose
    private List<StatistikGeneral> statistikGenerals = new ArrayList<>(0);

    public int getIdKelas() {
        return idKelas;
    }

    public void setIdKelas(int idKelas) {
        this.idKelas = idKelas;
    }

    public String getNamaKelas() {
        return namaKelas;
    }

    public void setNamaKelas(String namaKelas) {
        this.namaKelas = namaKelas;
    }

    public String getPembinaan() {
        return pembinaan;
    }

    public void setPembinaan(String pembinaan) {
        this.pembinaan = pembinaan;
    }

    public List<StatistikGeneral> getStatistikGenerals() {
        return statistikGenerals;
    }

    public void setStatistikGenerals(List<StatistikGeneral> statistikGenerals) {
        this.statistikGenerals = statistikGenerals;
    }
}
