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
    @SerializedName("list_siswa")
    @Expose
    private List<Peserta> listPeserta = null;

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

    public List<Peserta> getListPeserta() {
        return listPeserta;
    }

    public void setListPeserta(List<Peserta> listPeserta) {
        this.listPeserta = listPeserta;
    }

}
