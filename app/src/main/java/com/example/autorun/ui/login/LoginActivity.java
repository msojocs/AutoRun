package com.example.autorun.ui.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.autorun.R;
import com.example.autorun.databinding.ActivityLoginBinding;
import com.example.autorun.helper.App;
import com.example.autorun.helper.CheckAllow;
import com.example.autorun.helper.SystemUtil;

import org.runrun.entity.AppConfig;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Objects;


public class LoginActivity extends AppCompatActivity {

    private static String TAG = LoginActivity.class.getSimpleName();
    public static final String PREFS_NAME = LoginActivity.class.getName();
    public static final String IS_LOCAL = "IS_LOCAL";
    public static final String HOSTS_URI = "HOST_URI";
    public static final String NET_HOST_FILE = "net_hosts";
    public static final String MAP_PREFIX = "地图：";
    AppConfig appConfig = new AppConfig();
    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    ActivityResultLauncher<Intent> selectFileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.i(TAG, String.format("code:%d", result.getResultCode()));
                if (result.getResultCode() == RESULT_OK) {
                    // There are no request codes
                    Intent data = result.getData();
                    assert data != null;
                    setUriByPREFS(data);
                }
            }
    );
    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final EditText appVersionEditText = binding.inputAppVersion;
        final EditText inputDistance = binding.inputDistance;
        final EditText inputTimeEditText = binding.inputTime;
        final TextView resultArea = binding.result;
        final EditText mapFileArea = binding.mapFile;
        final Button loginButton = binding.login;
        final Button loadMapButton = binding.loadMap;
        final Button signInButton = binding.signInOrBack;
        final ProgressBar loadingProgressBar = binding.loading;

        usernameEditText.setText(settings.getString("phone", null));
        passwordEditText.setText(settings.getString("password", null));

        resultArea.setMovementMethod(ScrollingMovementMethod.getInstance());
        resultArea.setHorizontallyScrolling(true);
        resultArea.setVerticalScrollBarEnabled(true);
        resultArea.setFocusable(true);

        loginViewModel.getLoginFormState().observe(this, loginFormState -> {
            if (loginFormState == null) {
                return;
            }
            loginButton.setEnabled(loginFormState.isDataValid());
            signInButton.setEnabled(loginFormState.isDataValid());
            if (loginFormState.getUsernameError() != null) {
                usernameEditText.setError(getString(loginFormState.getUsernameError()));
            }
            if (loginFormState.getPasswordError() != null) {
                passwordEditText.setError(getString(loginFormState.getPasswordError()));
            }
        });

        loginViewModel.getLoginResult().observe(this, loginResult -> {
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
        passwordEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                App app = new App(appConfig);
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
                app.setResultArea(resultArea);
                app.setLoadingProgressBar(loadingProgressBar);
                app.start();
            }
            return false;
        });

        String mapPath = settings.getString(HOSTS_URI, null);
        if(mapPath != null) {
            Log.i(TAG, "map-path: " + mapPath);
            String[] split = mapPath.split("%2F");
            if(split.length > 0) {
                mapFileArea.setText(MAP_PREFIX + split[split.length - 1]);
            }
        }
//        loginButton.setEnabled(true);
        // 配置系统信息
        appConfig.setBrand(SystemUtil.getDeviceBrand());
        appConfig.setMobileType(SystemUtil.getSystemModel());
        appConfig.setSysVersion(SystemUtil.getSystemVersion());
        loginButton.setOnClickListener(v -> {
            App app = new App(appConfig);
            resultArea.setText("操作结果：");
            loadingProgressBar.setVisibility(View.VISIBLE);

            // 配置填写的信息
            appConfig.setPhone(usernameEditText.getText().toString());
            appConfig.setPassword(passwordEditText.getText().toString());
            appConfig.setAppVersion(appVersionEditText.getText().toString());

            resultArea.append("存储账户信息到本地...");
            editor.putString("phone", appConfig.getPhone());
            editor.putString("password", appConfig.getPassword());
            editor.apply();

            String distance = inputDistance.getText().toString();
            if(distance.length() > 0)
                appConfig.setDistance(Long.parseLong(distance));
            String time = inputTimeEditText.getText().toString();
            if(time.length() > 0)
                appConfig.setRunTime(Integer.parseInt(inputTimeEditText.getText().toString()));

//            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            InputStream inputStream;

            try {
                inputStream = getContentResolver().openInputStream(Uri.parse(settings.getString(HOSTS_URI, null)));
            } catch (Exception e) {
                Log.e(TAG, "HOSTS FILE NOT FOUND", e);
                inputStream = null;
            }

            Log.i(TAG, appConfig.toString());
            app.setResultArea(resultArea);
            app.setLoadingProgressBar(loadingProgressBar);
            app.setMapInput(inputStream);
            app.setType("run");
            app.start();
//                loginViewModel.login(usernameEditText.getText().toString(),
//                        passwordEditText.getText().toString());
        });

        loadMapButton.setOnClickListener(view -> selectFile());
        signInButton.setOnClickListener(v -> {
            App app = new App(appConfig);
            resultArea.setText("操作结果：");
            loadingProgressBar.setVisibility(View.VISIBLE);

            // 配置填写的信息
            appConfig.setPhone(usernameEditText.getText().toString());
            appConfig.setPassword(passwordEditText.getText().toString());
            appConfig.setAppVersion(appVersionEditText.getText().toString());

//            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

            Log.i(TAG, appConfig.toString());
            app.setResultArea(resultArea);
            app.setLoadingProgressBar(loadingProgressBar);
            app.setType("signInOrBack");
            app.start();
        });
        String id = getAndroidId(this);
//        String uuid = SystemUtil.getUUID(this);
        CheckAllow checkAllow = new CheckAllow();
        checkAllow.setApkVersion(getVersionName(this));
        checkAllow.setResultArea(resultArea);
        checkAllow.setConsumer(e -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            String message = (String)e.get("message");
            boolean cancelable = (boolean)e.get("cancelable");
            builder = builder
                    .setTitle("提示")
                    .setCancelable(false)
                    .setMessage(message);
            if (cancelable) {
                builder = builder.setNegativeButton("确定", (dialogInterface, i) -> {

                });
            }
            AlertDialog dialog = builder.create();
            dialog.show();
        });
        checkAllow.setAndroidId(id);
//        checkAllow.setUuid(uuid);
        checkAllow.start();

    }
    /**
     * 获取当前apk的版本名
     *
     * @param context 上下文
     * @return
     */
    public static String getVersionName(Context context) {
        String versionName = "";
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionName
            versionName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    public static String getAndroidId (Context context) {
        return Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    private void selectFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        try {
            String SHOW_ADVANCED;
            try {
                Field f = android.provider.DocumentsContract.class.getField("EXTRA_PROMPT");
                SHOW_ADVANCED = Objects.requireNonNull(f.get(f.getName())).toString();
            }catch (NoSuchFieldException e){
                Log.e(TAG,e.getMessage(),e);
                SHOW_ADVANCED = "android.content.extra.SHOW_ADVANCED";
            }
            intent.putExtra(SHOW_ADVANCED, true);
        } catch (Throwable e) {
            Log.e(TAG, "SET EXTRA_SHOW_ADVANCED", e);
        }

        try {
            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            startActivityForResult(intent, SELECT_FILE_CODE);
            selectFileLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, R.string.file_select_error, Toast.LENGTH_LONG).show();
            Log.e(TAG, "START SELECT_FILE_ACTIVE FAIL",e);
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(IS_LOCAL, false);
            editor.apply();
            startActivity(new Intent(getApplicationContext(), LoginActivity.class));
        }

    }

    @SuppressLint("SetTextI18n")
    private void setUriByPREFS(Intent intent) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = settings.edit();
        Uri uri = intent.getData();
        int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        try {
            getContentResolver().takePersistableUriPermission(uri, takeFlags);
            editor.putString(HOSTS_URI, uri.toString());
            editor.apply();
            if (checkHostUri() == 1) {
                String[] split = uri.toString().split("%2F");
                final EditText mapFileArea = binding.mapFile;
                mapFileArea.setText(MAP_PREFIX + split[split.length - 1]);
                Toast.makeText(this, R.string.file_select_ok, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.permission_error, Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            Log.e(TAG, "permission error", e);
        }

    }
    private int checkHostUri() {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.getBoolean(LoginActivity.IS_LOCAL, true)) {
            try {
                getContentResolver().openInputStream(Uri.parse(settings.getString(HOSTS_URI, null))).close();
                return 1;
            } catch (Exception e) {
                Log.e(TAG, "HOSTS FILE NOT FOUND", e);
                return -1;
            }
        } else {
            try {
                openFileInput(LoginActivity.NET_HOST_FILE).close();
                return 2;
            } catch (Exception e) {
                Log.e(TAG, "NET HOSTS FILE NOT FOUND", e);
                return -2;
            }
        }
    }
}