package info.japos.pp.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Comparator;

/**
 * Created by HWAHYUDI on 06-Dec-17.
 */

public class Peserta implements Comparable<Peserta>{

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
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("keterangan")
    @Expose
    private String keterangan;
    @SerializedName("jenis_kelamin")
    @Expose
    private String gender;

    public Peserta() {
    }

    public Peserta(Integer jamaahId) {
        this.jamaahId = jamaahId;
    }

    public Peserta(Peserta peserta, String status, String keterangan) {
        this.jamaahId = peserta.getJamaahId();
        this.namaPanggilan = peserta.getNamaPanggilan();
        this.namaLengkap = peserta.getNamaLengkap();
        this.kelompok = peserta.getKelompok();
        this.status = status;
        this.keterangan = keterangan;
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

    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }

    public String getKeterangan() { return keterangan; }

    public void setKeterangan(String keterangan) { this.keterangan = keterangan; }

    public String getGender() {
        if (gender != null) {
            return gender.equalsIgnoreCase("L") ? "Laki-laki" : "Perempuan";
        }
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Override
    public int compareTo(@NonNull Peserta peserta2) {
        String fullName1 = getNamaLengkap().toUpperCase();
        String fullName2 = peserta2.getNamaLengkap().toUpperCase();

        // ascending order
        return fullName1.compareTo(fullName2);
    }

    public static Comparator<Peserta> NicknameComparator = new Comparator<Peserta>() {
        @Override
        public int compare(Peserta peserta1, Peserta peserta2) {
            String prop1 = peserta1.getNamaPanggilan().toUpperCase();
            String prop2 = peserta2.getNamaPanggilan().toUpperCase();

            // ascending order
            return prop1.compareTo(prop2);
        }
    };

    public static Comparator<Peserta> KelompokComparator = new Comparator<Peserta>() {
        @Override
        public int compare(Peserta peserta1, Peserta peserta2) {
            String prop1 = peserta1.getKelompok().toUpperCase();
            String prop2 = peserta2.getKelompok().toUpperCase();

            // ascending order
            return prop1.compareTo(prop2);
        }
    };

    public static Comparator<Peserta> GenderComparator = new Comparator<Peserta>() {
        @Override
        public int compare(Peserta peserta1, Peserta peserta2) {
            String prop1 = peserta1.getGender().toUpperCase();
            String prop2 = peserta2.getGender().toUpperCase();

            // ascending order
            return prop1.compareTo(prop2);
        }
    };

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