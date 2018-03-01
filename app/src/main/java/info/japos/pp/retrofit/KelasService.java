package info.japos.pp.retrofit;

import java.util.List;

import info.japos.pp.models.Kelas;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by HWAHYUDI on 28-Jan-18.
 */

public interface KelasService {
    @GET("class/{timestamp}")
    Call<List<Kelas>> getClassActive(@Path("timestamp") String activeTimestamp);
}
