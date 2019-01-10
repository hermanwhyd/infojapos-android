package info.japos.pp.models.ApplicationInfo;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by HWAHYUDI on 06-Jan-18.
 */

public class ApplicationInfo extends RealmObject {
    @Index
    private String versionName;
    @PrimaryKey
    @Index
    private int versionCode;

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

}
