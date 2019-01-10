package info.japos.pp.retrofit;

import java.util.List;

import info.japos.pp.models.realm.Enums;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by HWAHYUDI on 30-Dec-17.
 */

public interface EnumsService {
    @GET("pilihan/{grup}")
    Call<List<Enums>> getPilihanByGrup(@Path("grup") String grup);
}
