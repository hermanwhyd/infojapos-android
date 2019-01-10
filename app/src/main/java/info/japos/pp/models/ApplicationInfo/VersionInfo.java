package info.japos.pp.models.ApplicationInfo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import info.japos.utils.GsonUtil;


/**
 * Created by HWAHYUDI on 06-Jan-18.
 */

public class VersionInfo {
    @SerializedName("version_name")
    @Expose
    private String versionName;
    @SerializedName("version_code")
    @Expose
    private Integer versionCode;
    @SerializedName("prev_version_action")
    @Expose
    private String prevVersionAction;
    @SerializedName("min_version_allowed")
    @Expose
    private Integer minVersionAllowed;
    @SerializedName("download_url")
    @Expose
    private String downloadUrl;
    @SerializedName("changes_log")
    @Expose
    private List<ChangesLog> changesLog = null;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public Integer getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(Integer versionCode) {
        this.versionCode = versionCode;
    }

    public String getPrevVersionAction() {
        return prevVersionAction;
    }

    public void setPrevVersionAction(String prevVersionAction) {
        this.prevVersionAction = prevVersionAction;
    }

    public Integer getMinVersionAllowed() {
        return minVersionAllowed;
    }

    public void setMinVersionAllowed(Integer minVersionAllowed) {
        this.minVersionAllowed = minVersionAllowed;
    }

    public List<ChangesLog> getChangesLog() {
        return changesLog;
    }

    public void setChangesLog(List<ChangesLog> changesLog) {
        this.changesLog = changesLog;
    }

    @Override
    public String toString() {
        return GsonUtil.getInstance().toJson(this);
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }
}
