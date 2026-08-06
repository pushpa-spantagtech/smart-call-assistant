package com.mobile.sca.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobile.sca.R;
import com.mobile.sca.api.ApiEndpoints;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class login extends AppCompatActivity {

    private static final String LOGIN_URL =
            ApiEndpoints.LOGIN;

    private static final String PREF_NAME = "app";
    private static final String KEY_LOGGED_IN = "logged_in";
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_PHONE = "phone";

    private NestedScrollView loginScrollView;
    private LinearLayout loginContent;

    private TextInputLayout phoneInputLayout;
    private TextInputLayout passwordInputLayout;

    private TextInputEditText usernameEditText;
    private TextInputEditText passwordEditText;

    private Button loginButton;
    private TextView forgotPasswordText;
    private TextView signUpText;

    private boolean isPasswordVisible = false;
    private boolean isKeyboardOpen = false;
    private boolean isRequestInProgress = false;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(R.layout.activity_login);

        initializeViews();
        setupPasswordToggle();
        setupListeners();
        setupErrorClearListeners();
        setupKeyboardAwareScroll();
        setupFocusScrollListeners();
    }

    private void initializeViews() {

        loginScrollView = findViewById(R.id.loginScrollView);
        loginContent = findViewById(R.id.loginContent);

        phoneInputLayout = findViewById(R.id.phoneInputLayout);
        passwordInputLayout = findViewById(R.id.passwordInputLayout);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);

        loginButton = findViewById(R.id.loginButton);
        forgotPasswordText = findViewById(R.id.forgotPasswordText);
        signUpText = findViewById(R.id.signUpText);

        passwordEditText.setTransformationMethod(
                PasswordTransformationMethod.getInstance()
        );

        passwordInputLayout.setEndIconDrawable(
                R.drawable.ic_eye_off
        );

        passwordInputLayout.setEndIconContentDescription(
                "Show password"
        );
    }

    private void setupPasswordToggle() {

        passwordInputLayout.setEndIconOnClickListener(view -> {

            int cursorPosition =
                    passwordEditText.getSelectionStart();

            if (isPasswordVisible) {

                passwordEditText.setTransformationMethod(
                        PasswordTransformationMethod.getInstance()
                );

                passwordInputLayout.setEndIconDrawable(
                        R.drawable.ic_eye_off
                );

                passwordInputLayout.setEndIconContentDescription(
                        "Show password"
                );

                isPasswordVisible = false;

            } else {

                passwordEditText.setTransformationMethod(
                        HideReturnsTransformationMethod.getInstance()
                );

                passwordInputLayout.setEndIconDrawable(
                        R.drawable.ic_eye
                );

                passwordInputLayout.setEndIconContentDescription(
                        "Hide password"
                );

                isPasswordVisible = true;
            }

            if (cursorPosition >= 0) {

                passwordEditText.setSelection(
                        Math.min(
                                cursorPosition,
                                passwordEditText.length()
                        )
                );
            }
        });
    }

    private void setupListeners() {

        loginButton.setOnClickListener(
                view -> validateLogin()
        );

        signUpText.setOnClickListener(view -> {

            if (isRequestInProgress) {
                return;
            }

            Intent intent = new Intent(
                    login.this,
                    signup.class
            );

            startActivity(intent);
        });

        forgotPasswordText.setOnClickListener(view -> {

            if (isRequestInProgress) {
                return;
            }

            Intent intent = new Intent(
                    login.this,
                    forgotpassword.class
            );

            startActivity(intent);
        });
    }

    private void setupErrorClearListeners() {

        addErrorClearWatcher(
                usernameEditText,
                phoneInputLayout
        );

        addErrorClearWatcher(
                passwordEditText,
                passwordInputLayout
        );
    }

    private void addErrorClearWatcher(
            TextInputEditText editText,
            TextInputLayout inputLayout
    ) {

        editText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence text,
                    int start,
                    int count,
                    int after
            ) {
                // No action required.
            }

            @Override
            public void onTextChanged(
                    CharSequence text,
                    int start,
                    int before,
                    int count
            ) {

                if (inputLayout.getError() != null) {
                    clearInputError(inputLayout);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action required.
            }
        });
    }

    private void setupKeyboardAwareScroll() {

        View rootView = findViewById(android.R.id.content);

        rootView.getViewTreeObserver()
                .addOnGlobalLayoutListener(() -> {

                    Rect visibleFrame = new Rect();
                    rootView.getWindowVisibleDisplayFrame(visibleFrame);

                    int totalHeight =
                            rootView.getRootView().getHeight();

                    int visibleHeight =
                            visibleFrame.height();

                    int keyboardHeight =
                            totalHeight - visibleHeight;

                    boolean keyboardNowOpen =
                            keyboardHeight > totalHeight * 0.15f;

                    if (keyboardNowOpen == isKeyboardOpen) {
                        return;
                    }

                    isKeyboardOpen = keyboardNowOpen;

                    ViewGroup.LayoutParams layoutParams =
                            loginContent.getLayoutParams();

                    if (keyboardNowOpen) {

                        layoutParams.height =
                                ViewGroup.LayoutParams.WRAP_CONTENT;

                        loginContent.setLayoutParams(layoutParams);

                        loginContent.setGravity(
                                Gravity.TOP |
                                        Gravity.CENTER_HORIZONTAL
                        );

                        loginContent.setPadding(
                                loginContent.getPaddingLeft(),
                                dpToPx(18),
                                loginContent.getPaddingRight(),
                                dpToPx(36)
                        );

                        loginScrollView.postDelayed(
                                this::scrollFocusedViewIntoPosition,
                                180
                        );

                    } else {

                        layoutParams.height =
                                ViewGroup.LayoutParams.MATCH_PARENT;

                        loginContent.setLayoutParams(layoutParams);

                        loginContent.setGravity(
                                Gravity.CENTER_VERTICAL |
                                        Gravity.CENTER_HORIZONTAL
                        );

                        loginContent.setPadding(
                                loginContent.getPaddingLeft(),
                                dpToPx(24),
                                loginContent.getPaddingRight(),
                                dpToPx(24)
                        );

                        loginScrollView.post(
                                () -> loginScrollView.smoothScrollTo(0, 0)
                        );
                    }
                });
    }

    private void setupFocusScrollListeners() {

        View.OnFocusChangeListener focusChangeListener =
                (view, hasFocus) -> {

                    if (hasFocus && isKeyboardOpen) {

                        loginScrollView.postDelayed(
                                this::scrollFocusedViewIntoPosition,
                                120
                        );
                    }
                };

        usernameEditText.setOnFocusChangeListener(
                focusChangeListener
        );

        passwordEditText.setOnFocusChangeListener(
                focusChangeListener
        );
    }

    private void scrollFocusedViewIntoPosition() {

        View focusedView = getCurrentFocus();

        if (focusedView == null ||
                loginScrollView == null) {
            return;
        }

        Rect focusedRect = new Rect();
        focusedView.getDrawingRect(focusedRect);

        loginScrollView.offsetDescendantRectToMyCoords(
                focusedView,
                focusedRect
        );

        int visibleBottomPadding = dpToPx(96);

        int targetScrollY =
                focusedRect.bottom -
                        loginScrollView.getHeight() +
                        visibleBottomPadding;

        loginScrollView.smoothScrollTo(
                0,
                Math.max(0, targetScrollY)
        );
    }

    private int dpToPx(int dp) {

        return Math.round(
                dp * getResources()
                        .getDisplayMetrics()
                        .density
        );
    }

    private void validateLogin() {

        if (isRequestInProgress) {
            return;
        }

        clearErrors();

        String phoneNumber =
                getInputValue(usernameEditText);

        String password =
                getInputValue(passwordEditText);

        if (TextUtils.isEmpty(phoneNumber)) {

            phoneInputLayout.setErrorEnabled(true);
            phoneInputLayout.setError(
                    "Enter your phone number"
            );

            usernameEditText.requestFocus();
            return;
        }

        if (!phoneNumber.matches("[0-9]{10}")) {

            phoneInputLayout.setErrorEnabled(true);
            phoneInputLayout.setError(
                    "Enter a valid 10-digit phone number"
            );

            usernameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {

            passwordInputLayout.setErrorEnabled(true);
            passwordInputLayout.setError(
                    "Enter your password"
            );

            passwordEditText.requestFocus();
            return;
        }

        callLoginApi(
                phoneNumber,
                password
        );
    }

    private void callLoginApi(
            String phoneNumber,
            String password
    ) {

        setLoadingState(true);

        executorService.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(ApiEndpoints.LOGIN);

                connection =
                        (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("POST");
                connection.setConnectTimeout(20000);
                connection.setReadTimeout(20000);
                connection.setDoOutput(true);
                connection.setDoInput(true);

                connection.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                connection.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                connection.setRequestProperty(
                        "ngrok-skip-browser-warning",
                        "true"
                );

                JSONObject requestBody = new JSONObject();

                requestBody.put(
                        "phone",
                        phoneNumber
                );

                requestBody.put(
                        "password",
                        password
                );

                byte[] requestBytes =
                        requestBody
                                .toString()
                                .getBytes(StandardCharsets.UTF_8);

                connection.setFixedLengthStreamingMode(
                        requestBytes.length
                );

                try (
                        OutputStream outputStream =
                                connection.getOutputStream()
                ) {

                    outputStream.write(requestBytes);
                    outputStream.flush();
                }

                int responseCode =
                        connection.getResponseCode();

                InputStream responseStream;

                if (responseCode >= 200 &&
                        responseCode < 300) {

                    responseStream =
                            connection.getInputStream();

                } else {

                    responseStream =
                            connection.getErrorStream();
                }

                String responseBody =
                        readResponse(responseStream);

                handleLoginResponse(
                        responseCode,
                        responseBody,
                        phoneNumber
                );

            } catch (IOException exception) {

                runOnUiThread(() -> {

                    setLoadingState(false);

                    showErrorDialog(
                            "Connection failed",
                            "Unable to connect to the server. " +
                                    "Check your internet connection and try again."
                    );
                });

            } catch (JSONException exception) {

                runOnUiThread(() -> {

                    setLoadingState(false);

                    showErrorDialog(
                            "Request error",
                            "Unable to create the login request."
                    );
                });

            } finally {

                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private String readResponse(
            InputStream inputStream
    ) throws IOException {

        if (inputStream == null) {
            return "";
        }

        StringBuilder responseBuilder =
                new StringBuilder();

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream,
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
        }

        return responseBuilder.toString();
    }

    private void handleLoginResponse(
            int responseCode,
            String responseBody,
            String phoneNumber
    ) {

        boolean success = false;
        String message = "";
        String token = "";

        try {

            if (!TextUtils.isEmpty(responseBody)) {

                JSONObject responseJson =
                        new JSONObject(responseBody);

                success = responseJson.optBoolean(
                        "success",
                        false
                );

                message = responseJson.optString(
                        "message",
                        ""
                );

                token = extractToken(responseJson);

                if (TextUtils.isEmpty(message)) {

                    message = responseJson.optString(
                            "error",
                            ""
                    );
                }

                if (TextUtils.isEmpty(message)) {

                    JSONObject errors =
                            responseJson.optJSONObject("errors");

                    if (errors != null) {

                        message =
                                extractFirstErrorMessage(errors);
                    }
                }
            }

        } catch (JSONException ignored) {
            // Default messages will be used.
        }

        final boolean finalSuccess = success;
        final String finalMessage = message;
        final String finalToken = token;

        runOnUiThread(() -> {

            setLoadingState(false);

            boolean successfulStatus =
                    responseCode >= 200 &&
                            responseCode < 300;

            if (successfulStatus &&
                    (finalSuccess ||
                            !responseBodyContainsExplicitFailure(
                                    responseBody
                            ))) {

                saveLoginSession(
                        phoneNumber,
                        finalToken
                );

                Toast.makeText(
                        login.this,
                        TextUtils.isEmpty(finalMessage)
                                ? "Login successful"
                                : finalMessage,
                        Toast.LENGTH_SHORT
                ).show();

                openPermissionScreen();

            } else {

                String errorMessage =
                        getLoginErrorMessage(
                                responseCode,
                                finalMessage
                        );

                showErrorDialog(
                        "Login failed",
                        errorMessage
                );
            }
        });
    }

    private boolean responseBodyContainsExplicitFailure(
            String responseBody
    ) {

        if (TextUtils.isEmpty(responseBody)) {
            return false;
        }

        try {

            JSONObject responseJson =
                    new JSONObject(responseBody);

            return responseJson.has("success") &&
                    !responseJson.optBoolean(
                            "success",
                            false
                    );

        } catch (JSONException exception) {

            return false;
        }
    }

    private String extractToken(
            JSONObject responseJson
    ) {

        String token = responseJson.optString(
                "token",
                ""
        );

        if (TextUtils.isEmpty(token)) {

            token = responseJson.optString(
                    "accessToken",
                    ""
            );
        }

        if (TextUtils.isEmpty(token)) {

            token = responseJson.optString(
                    "access_token",
                    ""
            );
        }

        if (TextUtils.isEmpty(token)) {

            JSONObject data =
                    responseJson.optJSONObject("data");

            if (data != null) {

                token = data.optString(
                        "token",
                        ""
                );

                if (TextUtils.isEmpty(token)) {

                    token = data.optString(
                            "accessToken",
                            ""
                    );
                }

                if (TextUtils.isEmpty(token)) {

                    token = data.optString(
                            "access_token",
                            ""
                    );
                }
            }
        }

        return token;
    }

    private String extractFirstErrorMessage(
            JSONObject errors
    ) {

        java.util.Iterator<String> keys =
                errors.keys();

        while (keys.hasNext()) {

            String key = keys.next();
            Object value = errors.opt(key);

            if (value instanceof String) {
                return (String) value;
            }

            if (value instanceof org.json.JSONArray errorArray) {

                if (errorArray.length() > 0) {
                    return errorArray.optString(0);
                }
            }
        }

        return "";
    }

    private String getLoginErrorMessage(
            int responseCode,
            String backendMessage
    ) {

        if (!TextUtils.isEmpty(backendMessage)) {

            String message = backendMessage.trim();

            if (message.equalsIgnoreCase("Not Found") ||
                    message.equalsIgnoreCase("User not found") ||
                    message.equalsIgnoreCase("Account not found")) {

                return "No account was found with this mobile number. Please sign up first.";
            }

            if (message.equalsIgnoreCase("Invalid credentials") ||
                    message.equalsIgnoreCase("Invalid login credentials")) {

                return "The mobile number or password you entered is incorrect.";
            }

            if (message.equalsIgnoreCase("Invalid password") ||
                    message.equalsIgnoreCase("Incorrect password")) {

                return "The password you entered is incorrect. Please try again.";
            }

            if (message.equalsIgnoreCase("Unauthorized")) {

                return "Your login request could not be verified. Please try again.";
            }

            if (message.equalsIgnoreCase("Forbidden")) {

                return "Your account is currently unavailable. Please contact support.";
            }

            return message;
        }

        if (responseCode == 400) {
            return "Please check your mobile number and password.";
        }

        if (responseCode == 401) {
            return "The mobile number or password you entered is incorrect.";
        }

        if (responseCode == 403) {
            return "Your account is currently unavailable. Please contact support.";
        }

        if (responseCode == 404) {
            return "No account was found with this mobile number. Please sign up first.";
        }

        if (responseCode == 422) {
            return "Please enter valid login details.";
        }

        if (responseCode >= 500) {
            return "Something went wrong on our server. Please try again later.";
        }

        return "Unable to log in. Please try again.";
    }

    private void saveLoginSession(
            String phoneNumber,
            String token
    ) {

        SharedPreferences preferences =
                getSharedPreferences(
                        PREF_NAME,
                        MODE_PRIVATE
                );

        SharedPreferences.Editor editor =
                preferences.edit();

        editor.putBoolean(
                KEY_LOGGED_IN,
                true
        );

        editor.putString(
                KEY_PHONE,
                phoneNumber
        );

        if (!TextUtils.isEmpty(token)) {

            editor.putString(
                    KEY_AUTH_TOKEN,
                    token
            );

        } else {

            editor.remove(KEY_AUTH_TOKEN);
        }

        editor.apply();
    }

    private void setLoadingState(boolean loading) {

        isRequestInProgress = loading;

        runOnUiThread(() -> {

            loginButton.setEnabled(!loading);
            signUpText.setEnabled(!loading);
            forgotPasswordText.setEnabled(!loading);
            usernameEditText.setEnabled(!loading);
            passwordEditText.setEnabled(!loading);

            if (loading) {

                loginButton.setText(R.string.logging_in);

            } else {

                loginButton.setText(R.string.login);
            }
        });
    }

    private void showErrorDialog(
            String title,
            String message
    ) {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(
                login.this,
                R.style.AppMaterialDialogTheme
        )
                .setIcon(R.drawable.ic_error_outline)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(true)
                .setPositiveButton(
                        "Try Again",
                        (dialog, which) -> {
                            dialog.dismiss();

                            passwordEditText.requestFocus();

                            passwordEditText.postDelayed(
                                    () -> passwordEditText.setSelection(
                                            passwordEditText.length()
                                    ),
                                    150
                            );
                        }
                )
                .show();
    }

    private void openPermissionScreen() {

        Intent intent = new Intent(
                login.this,
                permissioncheck.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_NEW_TASK
        );

        startActivity(intent);
        finish();
    }

    private String getInputValue(
            TextInputEditText editText
    ) {

        if (editText.getText() == null) {
            return "";
        }

        return editText
                .getText()
                .toString()
                .trim();
    }

    private void clearInputError(
            TextInputLayout inputLayout
    ) {

        inputLayout.setError(null);
        inputLayout.setErrorEnabled(false);
    }

    private void clearErrors() {

        clearInputError(phoneInputLayout);
        clearInputError(passwordInputLayout);
    }

    @Override
    protected void onDestroy() {

        executorService.shutdownNow();
        super.onDestroy();
    }
}