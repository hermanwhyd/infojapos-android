package info.japos.utils;

import java.io.IOException;
import java.lang.annotation.Annotation;

import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.retrofit.ServiceGenerator;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Response;

/**
 * Created by HWAHYUDI on 05-Jan-18.
 */

public class ErrorUtils {

    public static CommonResponse parseError(Response<?> response) {
        Converter<ResponseBody, CommonResponse> converter =
                ServiceGenerator
                        .retrofit
                        .responseBodyConverter(CommonResponse.class, new Annotation[0]);

        CommonResponse error;

        try {
            error = converter.convert(response.errorBody());
        } catch (IOException e) {
            return new CommonResponse();
        }

        return error;
    }
}

