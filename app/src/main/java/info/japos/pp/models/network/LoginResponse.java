package info.japos.pp.models.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import info.japos.pp.models.realm.User;
import info.japos.pp.models.realm.UserDomain;

/**
 * Created by HWAHYUDI on 03-Jan-18.
 */

public class LoginResponse {
    @SerializedName("api_token")
    @Expose
    private String apiToken;
    @SerializedName("user")
    @Expose
    private User user;
    @SerializedName("userDomain")
    @Expose
    private UserDomain userDomain;

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public UserDomain getUserDomain() {
        return userDomain;
    }

    public void setUserDomain(UserDomain userDomain) {
        this.userDomain = userDomain;
    }
}
