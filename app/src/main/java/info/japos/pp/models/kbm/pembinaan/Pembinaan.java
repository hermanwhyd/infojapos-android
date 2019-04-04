package info.japos.pp.models.kbm.pembinaan;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Objects;

import info.japos.pp.models.Peserta;
import info.japos.pp.models.kbm.common.ItemSectionInterface;
import info.japos.pp.models.kbm.kelas.Kelas;

public class Pembinaan implements ItemSectionInterface {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("lv_pembinaan")
    @Expose
    private String pembinaan;
    @SerializedName("ttl_kbm")
    @Expose
    private Integer totalKBM;
    @SerializedName("ttl_kelas")
    @Expose
    private Integer totalKelas;

    public Pembinaan(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getPembinaan() {
        return pembinaan;
    }

    public void setPembinaan(String pembinaan) {
        this.pembinaan = pembinaan;
    }

    public Integer getTotalKBM() {
        return totalKBM;
    }

    public void setTotalKBM(Integer totalKBM) {
        this.totalKBM = totalKBM;
    }

    public Integer getTotalKelas() {
        return totalKelas;
    }

    public void setTotalKelas(Integer totalKelas) {
        this.totalKelas = totalKelas;
    }

    @Override
    public boolean isSection() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pembinaan pembinaan = (Pembinaan) o;
        return Objects.equals(id, pembinaan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
