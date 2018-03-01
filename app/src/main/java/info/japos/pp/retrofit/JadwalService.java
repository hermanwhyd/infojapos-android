package info.japos.pp.retrofit;

import java.util.List;

import info.japos.pp.models.Presensi;
import info.japos.pp.models.view.SelectableJadwal;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Pad on 8/5/2017.
 */

public interface JadwalService {
    @GET("class-schedules/{scdID}/presences/{timestamp}")
    Call<Presensi> getStudentPresences(@Path("scdID") int jadwalID, @Path("timestamp") String timestamp);

    @GET("class-schedules/{timestamp}")
    Call<List<SelectableJadwal>> getSchedule(@Path("timestamp") String timestamp);
}
