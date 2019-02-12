package info.japos.pp.models.realm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class UserDomain extends RealmObject {
    @SerializedName("id")
    @Expose
    @PrimaryKey
    private int id;

    @SerializedName("nama")
    @Expose
    private String nama;

    @SerializedName("inisial")
    @Expose
    @Index
    private String inisial;

    private int parentId;

    @SerializedName("lv_pembina")
    @Expose
    private String pembina;

    @SerializedName("childs")
    @Expose
    @Ignore
    private List<UserDomain> childs;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getInisial() {
        return inisial;
    }

    public void setInisial(String inisial) {
        this.inisial = inisial;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public String getPembina() {
        return pembina;
    }

    public void setPembina(String pembina) {
        this.pembina = pembina;
    }

    public List<UserDomain> getChilds() {
        return childs;
    }

    public void setChilds(List<UserDomain> childs) {
        this.childs = childs;
    }

}
