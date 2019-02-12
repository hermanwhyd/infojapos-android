package info.japos.pp.models.realm;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by HWAHYUDI on 03-Jan-18.
 */

public class User extends RealmObject {
    @SerializedName("id")
    @Expose
    @PrimaryKey
    private Integer id;
    @SerializedName("username")
    @Expose
    @Index
    private String username;
    @SerializedName("email")
    @Expose
    @Index
    private String email;
    @SerializedName("nama")
    @Expose
    private String nama;
    @SerializedName("password")
    @Expose
    private String password;

    private UserDomain activeUserDomain;

    public User() {
    }

    public User(Integer id, String username, String email, String nama, String password, UserDomain activeUserDomain) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.nama = nama;
        this.password = password;
        this.activeUserDomain = activeUserDomain;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserDomain getActiveUserDomain() {
        return activeUserDomain;
    }

    public void setActiveUserDomain(UserDomain activeUserDomain) {
        this.activeUserDomain = activeUserDomain;
    }
}