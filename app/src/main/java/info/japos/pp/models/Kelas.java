package info.japos.pp.models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HWAHYUDI on 06-Dec-17.
 */
public class Kelas {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("nama_kelas")
    @Expose
    private String kelas;
    @SerializedName("lv_pembinaan")
    @Expose
    private String lvPembinaan;
    @SerializedName("lv_pembina")
    @Expose
    private String lvPembina;
    @SerializedName("nama_mt")
    @Expose
    private String namaMajelisTaklim;
    @SerializedName("list_siswa")
    @Expose
    private List<Peserta> listPeserta = null;

    public Kelas() {
    }

    public Kelas(Integer id) {
        this.id = id;
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

    public String getLvPembinaan() {
        return lvPembinaan;
    }

    public void setLvPembinaan(String lvPembinaan) {
        this.lvPembinaan = lvPembinaan;
    }

    public String getLvPembina() {
        return lvPembina;
    }

    public void setLvPembina(String lvPembina) {
        this.lvPembina = lvPembina;
    }

    public List<Peserta> getListPeserta() {
        return listPeserta;
    }

    public void setListPeserta(List<Peserta> listPeserta) {
        this.listPeserta = listPeserta;
    }

    public String getNamaMajelisTaklim() {
        return namaMajelisTaklim;
    }

    public void setNamaMajelisTaklim(String namaMajelisTaklim) {
        this.namaMajelisTaklim = namaMajelisTaklim;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Kelas kelas = (Kelas) o;

        return id != null ? id.equals(kelas.id) : kelas.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
