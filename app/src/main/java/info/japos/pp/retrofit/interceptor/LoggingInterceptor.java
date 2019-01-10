package info.japos.pp.retrofit.interceptor;

import android.util.Log;

import java.io.IOException;

import info.japos.utils.GsonUtil;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by HWAHYUDI on 31-Dec-17.
 */

public class LoggingInterceptor implements Interceptor {
    private static String TAG = "OKHTTPInteception";

    @Override public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        Log.d(TAG, String.format("Sending request %s on %s%n%s%s",
                request.url()
                , chain.connection()
                , request.headers()
                , GsonUtil.getInstance().toJson(request.body())));

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        Log.d(TAG, String.format("Received response for %s in %.1fms%n%s",
                response.request().url()
                , (t2 - t1) / 1e6d
                , GsonUtil.getInstance().toJson(response.body())));

        return response;
    }
}
