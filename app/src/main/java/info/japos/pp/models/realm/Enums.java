package info.japos.pp.models.realm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by HWAHYUDI on 30-Dec-17.
 */

public class Enums extends RealmObject {
    @SerializedName("id")
    @Expose
    @PrimaryKey
    private int id;

    @SerializedName("grup")
    @Expose
    @Index
    private String grup;

    @SerializedName("value")
    @Expose
    private String value;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGrup() {
        return grup;
    }

    public void setGrup(String grup) {
        this.grup = grup;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}

