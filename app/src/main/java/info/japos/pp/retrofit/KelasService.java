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
    @GET("class/{timestamp1}/{timestamp2}")
    Call<List<Kelas>> getClassActive(@Path("timestamp1") String periodStart, @Path("timestamp2") String periodEnd);
}
