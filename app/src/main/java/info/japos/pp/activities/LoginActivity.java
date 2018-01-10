package info.japos.pp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import info.japos.pp.helper.SessionManager;
import info.japos.pp.models.User;
import info.japos.pp.models.network.CommonResponse;
import info.japos.pp.models.network.LoginResponse;
import info.japos.pp.models.realm.UserRepository;
import info.japos.pp.retrofit.LoginService;
import info.japos.pp.retrofit.ServiceGenerator;
import info.japos.pp.view.ProgresDialog;
import info.japos.utils.ErrorUtils;
import info.japos.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private static final int REQUEST_SIGNUP = 101;

    @BindView(R.id.input_email)
    EditText inputEmail;
    @BindView(R.id.input_password)
    EditText inputPassword;
    @BindView(R.id.btn_login)
    AppCompatButton btnLogin;
    @BindView(R.id.link_signup)
    TextView linkSignup;

    private SessionManager session;
    private SharedPreferences sharedpreferences;

    private Call<LoginResponse> mCallLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        // init
        // Session manager
        session = new SessionManager(getApplicationContext());
        // shared preferences
        sharedpreferences = PreferenceManager.getDefaultSharedPreferences(this);

        checkIsLogged();
    }

    private void checkIsLogged() {
        if (session.isLoggedIn() && session.getUserLoged() != null) {
            gotoMainAcivity();
            this.finish();
        }
    }

    @OnClick({R.id.btn_login, R.id.link_signup})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                login();
                break;
            case R.id.link_signup:
                register();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {
                inputEmail.setText(data.getStringExtra("email"));
                inputPassword.setText(data.getStringExtra("password"));
            }
        }
    }

    public void login() {
        Log.d(TAG, "LOGIN Started");

        // validate input form
        if (!validate())  return;

        MaterialDialog materialDialog = ProgresDialog.showIndeterminateProgressDialog(this, R.string.progress_authenticating_dialog, R.string.please_wait, true);
        materialDialog.show();

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

        mCallLogin = ServiceGenerator
                    .createService(LoginService.class)
                    .doLogin(email, password);

        mCallLogin.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                materialDialog.dismiss();
                if (response.isSuccessful() && response.code() == 200) {
                    onLoginSuccess(response.body());
                } else {
                    CommonResponse cr = ErrorUtils.parseError(response);
                    onLoginFailed(cr);
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                materialDialog.dismiss();
                View view = findViewById(android.R.id.content);
                if (view != null) Utils.displayNetworkErrorSnackBar(view, null);
            }
        });
    }

    public void onLoginSuccess(LoginResponse response) {
        Log.d(TAG, "LOGIN Successfull");

        User userLogged = response.getUser();
        String apiToken = response.getApiToken();

        // save into realm
        UserRepository uRepo = new UserRepository();
        uRepo.AddUser(userLogged);

        // save to session
        session.setLogin(Boolean.TRUE);
        session.setUserLogged(userLogged.getId());
        session.setApiToken(apiToken);

        // go to main activity
        gotoMainAcivity();

        this.finish();
    }

    public void onLoginFailed(CommonResponse cr) {
        Toast.makeText(getBaseContext(), cr.getMessage(), Toast.LENGTH_SHORT).show();
    }

    public boolean validate() {
        boolean valid = true;

        String email = inputEmail.getText().toString();
        String password = inputPassword.getText().toString();

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

    private void register() {
        Intent goToSignupActivity = new Intent(this, SignupActivity.class);
        startActivityForResult(goToSignupActivity, REQUEST_SIGNUP);
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    private void gotoMainAcivity() {
        Intent goToMainActivity = new Intent(this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED|Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(goToMainActivity);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mCallLogin != null)
            mCallLogin.cancel();
    }
}
