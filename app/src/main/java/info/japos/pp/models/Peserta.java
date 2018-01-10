package info.japos.pp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HWAHYUDI on 06-Dec-17.
 */

public class Peserta {

    @SerializedName("jamaah_id")
    @Expose
    private Integer jamaahId;
    @SerializedName("nama_panggilan")
    @Expose
    private String namaPanggilan;
    @SerializedName("nama_lengkap")
    @Expose
    private String namaLengkap;
    @SerializedName("kelompok")
    @Expose
    private String kelompok;
    @SerializedName("keterangan")
    @Expose
    private String keterangan;
    @SerializedName("alasan")
    @Expose
    private String alasan;

    public Peserta() {
    }

    public Peserta(Integer jamaahId) {
        this.jamaahId = jamaahId;
    }

    public Integer getJamaahId() {
        return jamaahId;
    }

    public void setJamaahId(Integer jamaahId) {
        this.jamaahId = jamaahId;
    }

    public String getNamaPanggilan() {
        return namaPanggilan;
    }

    public void setNamaPanggilan(String namaPanggilan) {
        this.namaPanggilan = namaPanggilan;
    }

    public String getNamaLengkap() {
        return namaLengkap;
    }

    public void setNamaLengkap(String namaLengkap) {
        this.namaLengkap = namaLengkap;
    }

    public String getKelompok() {
        return kelompok;
    }

    public void setKelompok(String kelompok) {
        this.kelompok = kelompok;
    }

    public String getKeterangan() { return keterangan; }

    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public String getAlasan() { return alasan; }

    public void setAlasan(String alasan) { this.alasan = alasan; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peserta peserta = (Peserta) o;

        return jamaahId != null ? jamaahId.equals(peserta.jamaahId) : peserta.jamaahId == null;
    }

    @Override
    public int hashCode() {
        return jamaahId != null ? jamaahId.hashCode() : 0;
    }
}