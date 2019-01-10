package info.japos.pp.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import info.japos.pp.constants.NetworkConstant;
import info.japos.pp.retrofit.interceptor.AddHeaderInterceptor;
import info.japos.pp.retrofit.interceptor.LoggingInterceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Helper class untuk mendapatkan retrofit service
 *
 * @author hendrawd on 7/31/17
 */

public class ServiceGenerator {

    private static OkHttpClient sHttpClient =
            new OkHttpClient.Builder()
//                    .addInterceptor(new ApiTokenAdderInterceptor())
                    .addInterceptor(new AddHeaderInterceptor())
                    //.addInterceptor(new LoggingInterceptor())
                    .build();

    private static Gson gson = new GsonBuilder()
            .serializeNulls()
            .excludeFieldsWithoutExposeAnnotation()
            .create();

    private static Retrofit.Builder sBuilder =
            new Retrofit.Builder()
                    .baseUrl(NetworkConstant.API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(sHttpClient);

    public static <T> T createService(Class<T> serviceClass) {
        return sBuilder.build().create(serviceClass);
    }

    public static Retrofit retrofit = sBuilder.build();

    public static void cancelAllRequests() {
        sHttpClient.dispatcher().cancelAll();
    }
}
