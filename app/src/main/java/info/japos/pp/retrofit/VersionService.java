package info.japos.pp.retrofit;

import info.japos.pp.models.ApplicationInfo.VersionInfo;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by HWAHYUDI on 06-Jan-18.
 */

public interface VersionService {

    @GET("version")
    Call<VersionInfo> getVersion(@Query("version_code") int versionCode);

}
