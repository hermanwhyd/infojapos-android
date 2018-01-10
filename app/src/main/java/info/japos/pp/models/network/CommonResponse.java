package info.japos.pp.models.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by HWAHYUDI on 23-Dec-17.
 */

public class CommonResponse {

    @SerializedName("response_status")
    @Expose
    private String responseStatus;
    @SerializedName("message")
    @Expose
    private String message;

    public String getStatus() {
        return responseStatus;
    }

    public String getMessage() {
        return message;
    }

}