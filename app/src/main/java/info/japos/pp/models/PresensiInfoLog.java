package info.japos.pp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import info.japos.pp.models.statistik.StatistikKehadiran;

/**
 * Created by HWAHYUDI on 01-Mar-18.
 */

public class PresensiInfoLog {
    @SerializedName("nama_lengkap")
    @Expose
    private String namaLengkap;
    @SerializedName("created_by")
    @Expose
    private String createdBy;
    @SerializedName("updated_by")
    @Expose
    private String updatedBy;
    @SerializedName("created_date")
    @Expose
    private String createdDate;
    @SerializedName("updated_date")
    @Expose
    private String updatedDate;
    @SerializedName("statistik")
    @Expose
    private StatistikKehadiran statistik;

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    public StatistikKehadiran getStatistik() {
        return statistik;
    }

    public void setStatistik(StatistikKehadiran statistik) {
        this.statistik = statistik;
    }
}
