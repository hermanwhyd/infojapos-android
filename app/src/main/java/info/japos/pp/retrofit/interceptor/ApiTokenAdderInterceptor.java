package info.japos.pp.retrofit.interceptor;

import android.support.annotation.NonNull;

import java.io.IOException;

import info.japos.pp.application.AppController;
import info.japos.pp.constants.AppConstant;
import info.japos.pp.helper.SessionManager;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Interceptor yang digunakan untuk menambahkan API TOKEN secara dynamic saat request
 *
 * @author hendrawd on 7/31/17
 */

public class ApiTokenAdderInterceptor implements Interceptor {
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        // Session manager
        SessionManager sessionManager = new SessionManager(AppController.getInstance());
        final HttpUrl url = chain.request()
                .url()
                .newBuilder()
                .addQueryParameter("api_token", sessionManager.getApiToken())
                .build();
        final Request request = chain.request().newBuilder().url(url).build();
        return chain.proceed(request);
    }
}
