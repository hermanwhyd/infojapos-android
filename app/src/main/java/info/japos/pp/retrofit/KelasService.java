package info.japos.pp.retrofit;

import java.util.List;

import info.japos.pp.models.ClassParticipant;
import info.japos.pp.models.kbm.kelas.Kelas;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by HWAHYUDI on 28-Jan-18.
 */

public interface KelasService {
    @GET("class/{mtId}/{timestamp}")
    Call<List<Kelas>> getClassActive(@Path("timestamp") String period, @Path("mtId") int mtId);

    @GET("class/{mtId}/{timestamp1}/{timestamp2}")
    Call<List<Kelas>> getClassActive(@Path("timestamp1") String periodStart, @Path("timestamp2") String periodEnd, @Path("mtId") int mtId);

    @GET("class/{kelas_id}/peserta")
    Call<List<ClassParticipant>> getClassParticipant(@Path("kelas_id") int kelasId);
}
