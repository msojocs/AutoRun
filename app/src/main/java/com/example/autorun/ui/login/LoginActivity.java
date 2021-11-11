package com.example.autorun.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.autorun.R;
import com.example.autorun.databinding.ActivityLoginBinding;
import com.example.autorun.helper.App;
import com.example.autorun.helper.SystemUtil;

import org.example.entity.AppConfig;



public class LoginActivity extends AppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final EditText appVersionEditText = binding.inputAppVersion;
        final EditText  inputDistance = binding.inputDistance;
        final EditText inputTimeEditText = binding.inputTime;
        final TextView resultArea = binding.result;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;

        resultArea.setMovementMethod(ScrollingMovementMethod.getInstance());
        resultArea.setHorizontallyScrolling(true);
        resultArea.setVerticalScrollBarEnabled(true);
        resultArea.setFocusable(true);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    resultArea.setText("操作结果：");
                    loadingProgressBar.setVisibility(View.VISIBLE);

                    AppConfig appConfig = new AppConfig();
                    appConfig.setPhone(usernameEditText.getText().toString());
                    appConfig.setPassword(passwordEditText.getText().toString());
                    appConfig.setAppVersion(appVersionEditText.getText().toString());

                    appConfig.setBrand(SystemUtil.getDeviceBrand());
                    appConfig.setMobileType(SystemUtil.getSystemModel());
                    appConfig.setSysVersion(SystemUtil.getSystemVersion());

                    String distance = inputDistance.getText().toString();
                    if(distance.length()>0)
                        appConfig.setDistance(Long.parseLong(distance));
                    String time = inputTimeEditText.getText().toString();
                    if(time.length()>0)
                        appConfig.setRunTime(Integer.parseInt(inputTimeEditText.getText().toString()));

                    System.out.println(appConfig);
                    App app = new App(appConfig);
                    app.setResultArea(resultArea);
                    app.setLoadingProgressBar(loadingProgressBar);
                    app.start();
                }
                return false;
            }
        });

//        loginButton.setEnabled(true);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultArea.setText("操作结果：");
                loadingProgressBar.setVisibility(View.VISIBLE);

                AppConfig appConfig = new AppConfig();
                appConfig.setPhone(usernameEditText.getText().toString());
                appConfig.setPassword(passwordEditText.getText().toString());
                appConfig.setAppVersion(appVersionEditText.getText().toString());

                appConfig.setBrand(SystemUtil.getDeviceBrand());
                appConfig.setMobileType(SystemUtil.getSystemModel());
                appConfig.setSysVersion(SystemUtil.getSystemVersion());

                String distance = inputDistance.getText().toString();
                if(distance.length()>0)
                appConfig.setDistance(Long.parseLong(distance));
                String time = inputTimeEditText.getText().toString();
                if(time.length()>0)
                appConfig.setRunTime(Integer.parseInt(inputTimeEditText.getText().toString()));

                System.out.println(appConfig);
                App app = new App(appConfig);
                app.setResultArea(resultArea);
                app.setLoadingProgressBar(loadingProgressBar);
                app.start();
//                loginViewModel.login(usernameEditText.getText().toString(),
//                        passwordEditText.getText().toString());
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}