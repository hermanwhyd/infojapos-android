package info.japos.pp.retrofit;

import info.japos.pp.models.User;
import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.models.network.LoginResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by HWAHYUDI on 03-Jan-18.
 */

public interface LoginService {
    @FormUrlEncoded
    @POST("login")
    Call<LoginResponse> doLogin(@Field("email") String email, @Field("password") String password);

    @GET("logout")
    Call<CommonResponse> doLogout(@Query("api_token") String token);

    @POST("register")
    Call<CommonResponse> doRegister(@Body User user);
}
