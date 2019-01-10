package info.japos.pp.retrofit;

import info.japos.pp.models.PresensiInfoLog;
import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.models.Presensi;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by HWAHYUDI on 23-Dec-17.
 */

public interface PresensiService {

    /**
     * untuk mendapatkan informasi user yang mengupdate presensi
     * @param presensiId a int kelas_presensi.id
     * @param pesertaId a int jamaah.id
     * @return
     */
    @GET("class-presences/{presensiid}/{pesertaid}/info")
    Call<PresensiInfoLog> getPresensiWhoUpdate(@Path("presensiid") int presensiId, @Path("pesertaid") int pesertaId);

    /**
     * untuk mendapatkan informasi user yang membuat presensi dan statistik
     * @param presensiId a int kelas_presensi.id
     * @return
     */
    @GET("class-presences/{presensiid}/statistic")
    Call<PresensiInfoLog> getPresensiStatistik(@Path("presensiid") int presensiId);

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

    /**
     * untuk hapus kehadiran (keterangan)
     * @param presensiID
     * @return
     */
    @DELETE("class-presences/{pssID}")
    Call<CommonResponse> deletePresensi(@Path("pssID") int presensiID, @Query("api_token") String token);
}
