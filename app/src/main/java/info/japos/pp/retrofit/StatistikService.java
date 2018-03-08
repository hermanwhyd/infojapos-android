package info.japos.pp.retrofit;

import info.japos.pp.models.statistik.StatistikKelas;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by HWAHYUDI on 23-Dec-17.
 */

public interface StatistikService {
    /**
     * to get statistik attendance by kelas
     * @param kelasId a int kelas.id
     * @param timestamp1 a String date with format dd-MM-yyyy. e.g. 01-12-2018
     * @param timestamp2 a String date with format dd-MM-yyyy. e.g. 01-12-2018
     * @return
     */
    @GET("statistik/kelas/{kelasId}/{timestamp1}/{timestamp2}")
    Call<StatistikKelas> getStatistikKelas(@Path("kelasId") int kelasId, @Path("timestamp1") String timestamp1, @Path("timestamp2") String timestamp2);


    /**
     * to get statistik attendance by kelas
     * @param kelasId a int kelas.id
     * @param timestamp1 a String date with format dd-MM-yyyy. e.g. 01-12-2018
     * @param timestamp2 a String date with format dd-MM-yyyy. e.g. 01-12-2018
     * @return
     */
    @GET("statistik/kelas/{kelasId}/{timestamp1}/{timestamp2}/peserta/new")
    Call<StatistikKelas> getStatistikPeserta(@Path("kelasId") int kelasId, @Path("timestamp1") String timestamp1, @Path("timestamp2") String timestamp2);

    /**
     * to download statistik attendance by kelas
     * @param kelasId a int kelas.id
     * @param timestamp1 a String date with format dd-MM-yyyy. e.g. 01-12-2018
     * @param timestamp2 a String date with format dd-MM-yyyy. e.g. 01-12-2018
     * @return
     */
    @GET("statistik/kelas/{kelasId}/{timestamp1}/{timestamp2}/download")
    Call<ResponseBody> downloadStatistik(@Path("kelasId") int kelasId, @Path("timestamp1") String timestamp1, @Path("timestamp2") String timestamp2);
}
