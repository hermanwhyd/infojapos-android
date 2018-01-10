package info.japos.pp.retrofit;

import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.models.Presensi;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by HWAHYUDI on 23-Dec-17.
 */

public interface PresensiService {
    /**
     * to create a new Presensi
     * @param jadwalID a int jadwal.id
     * @param timestamp a String date with format dd-MM-yyyy. e.g. 01-12-2018
     * @return
     */
    @FormUrlEncoded
    @POST("class-presences/{scdID}")
    Call<CommonResponse> createPresensi(@Path("scdID") int jadwalID, @Field("timestamp") String timestamp, @Query("api_token") String token);

    /**
     * untuk update status kehadiran (keterangan)
     * @param presensiID
     * @param presensi an object of Presensi
     * @return
     */
    @PUT("class-presences/{pssID}")
    Call<CommonResponse> updatePresensi(@Path("pssID") int presensiID, @Body Presensi presensi, @Query("api_token") String token);
}
