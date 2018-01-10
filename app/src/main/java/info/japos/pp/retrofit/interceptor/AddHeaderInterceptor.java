package info.japos.pp.retrofit.interceptor;


import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by HWAHYUDI on 31-Dec-17.
 */

public class AddHeaderInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        builder.addHeader("Authorization", "JaposAuthStandard");
        builder.addHeader("Content-Type", "application/json");

        return chain.proceed(builder.build());
    }
}
