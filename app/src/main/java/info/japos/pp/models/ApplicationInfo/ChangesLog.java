package info.japos.pp.models.ApplicationInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HWAHYUDI on 06-Jan-18.
 */

public class ChangesLog {
    @SerializedName("version")
    @Expose
    private String version;
    @SerializedName("remark")
    @Expose
    private String remark;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}