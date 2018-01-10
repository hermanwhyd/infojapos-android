package info.japos.pp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import info.japos.pp.R;
import info.japos.pp.models.User;
import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.retrofit.LoginService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.view.CustomToast;
import info.japos.pp.view.ProgresDialog;
import info.japos.utils.ErrorUtils;
import info.japos.utils.GsonUtil;
import info.japos.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    private static String TAG = SignupActivity.class.getSimpleName();

    @BindView(R.id.input_name)
    EditText inputName;
    @BindView(R.id.input_username)
    EditText inputUsername;
    @BindView(R.id.input_email)
    EditText inputEmail;
    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.btn_signup)
    AppCompatButton btnSignup;
    @BindView(R.id.link_login)
    TextView linkLogin;

    private Call<CommonResponse> mCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.btn_signup, R.id.link_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_signup:
                signUp();
                break;
            case R.id.link_login:
                doLogin();
                break;
        }
    }

    private void doLogin() {
        this.finish();
    }

    private void signUp() {
        if (!validate()) return;

        MaterialDialog materialDialog = ProgresDialog.showIndeterminateProgressDialog(this, R.string.progress_registering_dialog, R.string.please_wait, true);
        materialDialog.show();

        User newUser = new User();
        newUser.setNama(inputName.getText().toString());
        newUser.setUsername(inputUsername.getText().toString());
        newUser.setEmail(inputEmail.getText().toString());
        newUser.setPassword(inputPassword.getText().toString());
        Log.d(TAG, GsonUtil.getInstance().toJson(newUser));

        mCall = ServiceGenerator
                .createService(LoginService.class)
                .doRegister(newUser);

        mCall.enqueue(new Callback<CommonResponse>() {
            @Override
            public void onResponse(Call<CommonResponse> call, Response<CommonResponse> response) {
                materialDialog.dismiss();
                if (response.isSuccessful() && response.code() == 200) {
                    Toast.makeText(getBaseContext(), "Signup sukses, silakan login", Toast.LENGTH_SHORT).show();

                    // finish this
                    Intent intent = new Intent();
                    intent.putExtra("email", inputEmail.getText().toString());
                    intent.putExtra("password", inputPassword.getText().toString());
                    setResult(RESULT_OK, intent);
                    finish();
                } else {
                    CommonResponse cr = ErrorUtils.parseError(response);
                    CustomToast.show(getBaseContext(), cr != null ? cr.getMessage() : "Registrasi gagal");
                }
            }

            @Override
            public void onFailure(Call<CommonResponse> call, Throwable t) {
                materialDialog.dismiss();
                View view = findViewById(android.R.id.content);
                if (view != null) Utils.displayNetworkErrorSnackBar(view, null);
            }
        });
    }

    public boolean validate() {
        boolean valid = true;

        String name = inputName.getText().toString();
        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();
        String username = inputUsername.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            inputName.setError("Min 3 karakter");
            valid = false;
        } else {
            inputName.setError(null);
        }

        if (username .isEmpty() || username .length() < 3) {
            inputUsername.setError("Min 3 karakter");
            valid = false;
        } else if (!username.matches("[a-zA-Z]+")) {
            inputUsername.setError("hanya huruf dan tanpa spasi");
            valid = false;
        } else {
            inputUsername.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            inputEmail.setError("input email yang valid");
            valid = false;
        } else {
            inputEmail.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            inputPassword.setError("input 4 - 10 karakter");
            valid = false;
        } else {
            inputPassword.setError(null);
        }

        return valid;
    }
}
