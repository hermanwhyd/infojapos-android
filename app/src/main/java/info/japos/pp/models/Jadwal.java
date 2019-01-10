package info.japos.pp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HWAHYUDI on 09-Dec-17.
 */

public class Jadwal {
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("nama_kelas")
    @Expose
    private String kelas;
    @SerializedName("lokasi")
    @Expose
    private String lokasi;
    @SerializedName("pembinaan")
    @Expose
    private String pembinaan;
    @SerializedName("jam_mulai")
    @Expose
    private String jamMulai;
    @SerializedName("jam_selesai")
    @Expose
    private String jamSelesai;
    @SerializedName("status_presensi")
    @Expose
    private String status;
    @SerializedName("presensi_id")
    @Expose
    private Integer presensiId;
    @SerializedName("ttl_peserta")
    @Expose
    private Integer totalPeserta;
    @SerializedName("H")
    @Expose
    private Integer hadir;
    @SerializedName("A")
    @Expose
    private Integer alpa;
    @SerializedName("I")
    @Expose
    private Integer izin;

    public Jadwal() {
    }

    public Jadwal(Integer id, String kelas, String lokasi, String pembinaan, String jamMulai, String jamSelesi, String status) {
        this.id = id;
        this.kelas = kelas;
        this.lokasi = lokasi;
        this.pembinaan = pembinaan;
        this.jamMulai = jamMulai;
        this.jamSelesai = jamSelesi;
        this.status = status;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getKelas() {
        return kelas;
    }

    public void setKelas(String kelas) {
        this.kelas = kelas;
    }

    public String getLokasi() {return lokasi;}

    public void setLokasi(String lokasi) {this.lokasi = lokasi;}

    public String getPembinaan() {
        return pembinaan;
    }

    public void setPembinaan(String pembinaan) {
        this.pembinaan = pembinaan;
    }

    public String getJamMulai() {
        return jamMulai;
    }

    public void setJamMulai(String jamMulai) {
        this.jamMulai = jamMulai;
    }

    public String getJamSelesai() {
        return jamSelesai;
    }

    public void setJamSelesai(String jamSelesai) {
        this.jamSelesai = jamSelesai;
    }

    public String getStatus() {return status;}

    public void setStatus(String status) {this.status = status;}

    public Integer getTotalPeserta() {
        return totalPeserta;
    }

    public void setTotalPeserta(Integer totalPeserta) {
        this.totalPeserta = totalPeserta;
    }

    public Integer getPresensiId() {
        return presensiId;
    }

    public void setPresensiId(Integer presensiId) {
        this.presensiId = presensiId;
    }

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
}
