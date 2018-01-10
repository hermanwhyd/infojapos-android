package info.japos.pp.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by HWAHYUDI on 06-Dec-17.
 */
public class Presensi {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("nama_kelas")
    @Expose
    private String kelas;
    @SerializedName("lv_pembina")
    @Expose
    private String lvPembina;
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

}
