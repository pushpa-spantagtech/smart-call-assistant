package com.mobile.sca.base;

import com.mobile.sca.AppAnalytics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class signup extends AppCompatActivity {
    private static final String SIGNUP_URL =
            ApiEndpoints.SIGNUP;
    private TextInputLayout nameInputLayout;
    private TextInputLayout phoneInputLayout;
    private TextInputLayout emailInputLayout;
    private TextInputLayout passwordInputLayout;
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputEditText nameEditText;
    private TextInputEditText phoneEditText;
    private TextInputEditText emailEditText;
    private TextInputEditText passwordEditText;
    private TextInputEditText confirmPasswordEditText;

    private Button registerButton;
    private TextView loginText;

    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isRequestInProgress = false;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onResume() {
        super.onResume();
        AppAnalytics.screen(this, AppAnalytics.SIGNUP, getClass().getSimpleName());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );

        setContentView(R.layout.activity_signup);

        initializeViews();
        setupPasswordToggles();
        setupListeners();
        setupErrorClearListeners();
    }

    private void initializeViews() {

        nameInputLayout = findViewById(R.id.nameInputLayout);
        phoneInputLayout = findViewById(R.id.signupPhoneInputLayout);
        emailInputLayout = findViewById(R.id.signupEmailInputLayout);
        passwordInputLayout = findViewById(R.id.signupPasswordInputLayout);
        confirmPasswordInputLayout =
                findViewById(R.id.confirmPasswordInputLayout);

        nameEditText = findViewById(R.id.nameEditText);
        phoneEditText = findViewById(R.id.signupPhoneEditText);
        emailEditText = findViewById(R.id.signupEmailEditText);
        passwordEditText = findViewById(R.id.signupPasswordEditText);
        confirmPasswordEditText =
                findViewById(R.id.confirmPasswordEditText);

        registerButton = findViewById(R.id.registerButton);
        loginText = findViewById(R.id.loginText);

        passwordEditText.setTransformationMethod(
                PasswordTransformationMethod.getInstance()
        );

        confirmPasswordEditText.setTransformationMethod(
                PasswordTransformationMethod.getInstance()
        );

        passwordInputLayout.setEndIconDrawable(R.drawable.ic_eye_off);
        confirmPasswordInputLayout.setEndIconDrawable(R.drawable.ic_eye_off);

        passwordInputLayout.setEndIconContentDescription("Show password");
        confirmPasswordInputLayout.setEndIconContentDescription(
                "Show confirm password"
        );
    }

    private void setupPasswordToggles() {

        passwordInputLayout.setEndIconOnClickListener(
                view -> togglePasswordVisibility()
        );

        confirmPasswordInputLayout.setEndIconOnClickListener(
                view -> toggleConfirmPasswordVisibility()
        );
    }

    private void setupListeners() {

        registerButton.setOnClickListener(
                view -> validateRegistration()
        );

        loginText.setOnClickListener(
                view -> {
                    if (!isRequestInProgress) {
                        openLoginScreen();
                    }
                }
        );
    }

    private void setupErrorClearListeners() {

        addErrorClearWatcher(
                nameEditText,
                nameInputLayout
        );

        addErrorClearWatcher(
                phoneEditText,
                phoneInputLayout
        );

        addErrorClearWatcher(
                emailEditText,
                emailInputLayout
        );

        addErrorClearWatcher(
                passwordEditText,
                passwordInputLayout
        );

        addErrorClearWatcher(
                confirmPasswordEditText,
                confirmPasswordInputLayout
        );

        passwordEditText.addTextChangedListener(new TextWatcher() {

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

                if (confirmPasswordInputLayout.getError() != null) {
                    clearInputError(confirmPasswordInputLayout);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // No action required.
            }
        });
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

    private void clearInputError(TextInputLayout inputLayout) {
        inputLayout.setError(null);
        inputLayout.setErrorEnabled(false);
    }

    private void togglePasswordVisibility() {

        int cursorPosition = passwordEditText.getSelectionStart();

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

        restoreCursorPosition(
                passwordEditText,
                cursorPosition
        );
    }

    private void toggleConfirmPasswordVisibility() {

        int cursorPosition =
                confirmPasswordEditText.getSelectionStart();

        if (isConfirmPasswordVisible) {

            confirmPasswordEditText.setTransformationMethod(
                    PasswordTransformationMethod.getInstance()
            );

            confirmPasswordInputLayout.setEndIconDrawable(
                    R.drawable.ic_eye_off
            );

            confirmPasswordInputLayout.setEndIconContentDescription(
                    "Show confirm password"
            );

            isConfirmPasswordVisible = false;

        } else {

            confirmPasswordEditText.setTransformationMethod(
                    HideReturnsTransformationMethod.getInstance()
            );

            confirmPasswordInputLayout.setEndIconDrawable(
                    R.drawable.ic_eye
            );

            confirmPasswordInputLayout.setEndIconContentDescription(
                    "Hide confirm password"
            );

            isConfirmPasswordVisible = true;
        }

        restoreCursorPosition(
                confirmPasswordEditText,
                cursorPosition
        );
    }

    private void restoreCursorPosition(
            TextInputEditText editText,
            int cursorPosition
    ) {

        if (cursorPosition >= 0) {

            editText.setSelection(
                    Math.min(
                            cursorPosition,
                            editText.length()
                    )
            );
        }
    }

    private void validateRegistration() {

        if (isRequestInProgress) {
            return;
        }

        clearErrors();

        String fullName = getValue(nameEditText);
        String phone = getValue(phoneEditText);

        String email = getValue(emailEditText)
                .toLowerCase(Locale.ROOT);

        emailEditText.setText(email);
        emailEditText.setSelection(email.length());

        String password = getValue(passwordEditText);
        String confirmPassword = getValue(confirmPasswordEditText);

        if (TextUtils.isEmpty(fullName)) {

            nameInputLayout.setErrorEnabled(true);
            nameInputLayout.setError("Enter your full name");
            nameEditText.requestFocus();
            return;
        }

        if (fullName.length() < 2) {

            nameInputLayout.setErrorEnabled(true);
            nameInputLayout.setError("Enter a valid full name");
            nameEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(phone)) {

            phoneInputLayout.setErrorEnabled(true);
            phoneInputLayout.setError("Enter your phone number");
            phoneEditText.requestFocus();
            return;
        }

        if (!phone.matches("[0-9]{10}")) {

            phoneInputLayout.setErrorEnabled(true);
            phoneInputLayout.setError(
                    "Enter a valid 10-digit phone number"
            );

            phoneEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {

            emailInputLayout.setErrorEnabled(true);
            emailInputLayout.setError("Enter your email address");
            emailEditText.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            emailInputLayout.setErrorEnabled(true);
            emailInputLayout.setError("Enter a valid email address");
            emailEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {

            passwordInputLayout.setErrorEnabled(true);
            passwordInputLayout.setError("Enter your password");
            passwordEditText.requestFocus();
            return;
        }

        String passwordError =
                getPasswordValidationError(password);

        if (passwordError != null) {

            passwordInputLayout.setErrorEnabled(true);
            passwordInputLayout.setError(passwordError);
            passwordEditText.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(confirmPassword)) {

            confirmPasswordInputLayout.setErrorEnabled(true);
            confirmPasswordInputLayout.setError(
                    "Confirm your password"
            );

            confirmPasswordEditText.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {

            confirmPasswordInputLayout.setErrorEnabled(true);
            confirmPasswordInputLayout.setError(
                    "Passwords do not match"
            );

            confirmPasswordEditText.requestFocus();
            return;
        }

        hideKeyboard(confirmPasswordEditText);

        showEmailConfirmationDialog(
                fullName,
                phone,
                email,
                password,
                confirmPassword
        );
    }

    private void showEmailConfirmationDialog(
            String fullName,
            String phone,
            String email,
            String password,
            String confirmPassword
    ) {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(
                signup.this,
                R.style.AppMaterialDialogTheme
        )
                .setIcon(R.drawable.ic_email)
                .setTitle("Confirm Email Address")
                .setMessage(
                        "Password reset OTP will be sent to:\n\n" +
                                email +
                                "\n\nPlease make sure this email address is correct."
                )
                .setCancelable(true)
                .setNegativeButton(
                        "Edit Email",
                        (dialog, which) -> {

                            dialog.dismiss();

                            emailEditText.requestFocus();

                            emailEditText.postDelayed(
                                    () -> {

                                        emailEditText.setSelection(
                                                emailEditText.length()
                                        );

                                        showKeyboard(emailEditText);
                                    },
                                    150
                            );
                        }
                )
                .setPositiveButton(
                        "Confirm & Register",
                        (dialog, which) -> {

                            dialog.dismiss();

                            callSignupApi(
                                    fullName,
                                    phone,
                                    email,
                                    password,
                                    confirmPassword
                            );
                        }
                )
                .show();
    }

    private void hideKeyboard(View view) {

        if (view == null) {
            return;
        }

        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        if (inputMethodManager != null) {

            inputMethodManager.hideSoftInputFromWindow(
                    view.getWindowToken(),
                    0
            );
        }
    }

    private void showKeyboard(View view) {

        if (view == null) {
            return;
        }

        InputMethodManager inputMethodManager =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        if (inputMethodManager != null) {

            inputMethodManager.showSoftInput(
                    view,
                    InputMethodManager.SHOW_IMPLICIT
            );
        }
    }

    private String getPasswordValidationError(String password) {

        if (password.length() < 8) {
            return "Password must contain at least 8 characters";
        }

        if (!password.matches(".*[A-Z].*")) {
            return "Password must contain an uppercase letter";
        }

        if (!password.matches(".*[a-z].*")) {
            return "Password must contain a lowercase letter";
        }

        if (!password.matches(".*[0-9].*")) {
            return "Password must contain a number";
        }

        if (!password.matches(
                ".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*"
        )) {
            return "Password must contain a special character";
        }

        return null;
    }

    private void callSignupApi(
            String fullName,
            String phone,
            String email,
            String password,
            String confirmPassword
    ) {

        setLoadingState(true);

        executorService.execute(() -> {

            HttpURLConnection connection = null;

            try {

                URL url = new URL(SIGNUP_URL);

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

                requestBody.put("fullName", fullName);
                requestBody.put("phone", phone);
                requestBody.put("email", email);
                requestBody.put("password", password);
                requestBody.put(
                        "confirmPassword",
                        confirmPassword
                );

                byte[] requestBytes =
                        requestBody
                                .toString()
                                .getBytes(StandardCharsets.UTF_8);

                connection.setFixedLengthStreamingMode(
                        requestBytes.length
                );

                try (OutputStream outputStream =
                             connection.getOutputStream()) {

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

                handleSignupResponse(
                        responseCode,
                        responseBody
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
                            "Unable to create the signup request."
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

    private void handleSignupResponse(
            int responseCode,
            String responseBody
    ) {

        boolean success = false;
        String message = "";

        try {

            if (!TextUtils.isEmpty(responseBody)) {

                JSONObject responseJson =
                        new JSONObject(responseBody);

                success =
                        responseJson.optBoolean(
                                "success",
                                false
                        );

                message =
                        responseJson.optString(
                                "message",
                                ""
                        );

                if (TextUtils.isEmpty(message)) {

                    JSONObject errors =
                            responseJson.optJSONObject("errors");

                    if (errors != null) {
                        message =
                                extractFirstErrorMessage(errors);
                    }
                }

                if (TextUtils.isEmpty(message)) {

                    message =
                            responseJson.optString(
                                    "error",
                                    ""
                            );
                }
            }

        } catch (JSONException ignored) {
            // Default message is shown below.
        }

        final boolean finalSuccess = success;
        final String finalMessage = message;

        runOnUiThread(() -> {

            setLoadingState(false);

            if (responseCode >= 200 &&
                    responseCode < 300 &&
                    finalSuccess) {

                AppAnalytics.authSuccess(this, true);

                showRegistrationSuccessDialog(
                        TextUtils.isEmpty(finalMessage)
                                ? "Registration successful"
                                : finalMessage
                );

            } else {

                if (isAlreadyRegisteredError(
                        responseCode,
                        finalMessage
                )) {

                    showAlreadyRegisteredDialog();

                } else {

                    String errorMessage =
                            getSignupErrorMessage(
                                    responseCode,
                                    finalMessage
                            );

                    showErrorDialog(
                            "Unable to register",
                            errorMessage
                    );
                }
            }
        });
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

    private boolean isAlreadyRegisteredError(
            int responseCode,
            String backendMessage
    ) {

        if (responseCode == 409) {
            return true;
        }

        if (TextUtils.isEmpty(backendMessage)) {
            return false;
        }

        String message =
                backendMessage
                        .trim()
                        .toLowerCase();

        return message.contains("already") ||
                message.contains("exists") ||
                message.contains("registered") ||
                message.contains("duplicate");
    }

    private String getSignupErrorMessage(
            int responseCode,
            String backendMessage
    ) {

        if (!TextUtils.isEmpty(backendMessage)) {

            String message = backendMessage.trim();
            String lowerMessage = message.toLowerCase();

            if (lowerMessage.contains("invalid phone") ||
                    lowerMessage.contains("invalid mobile")) {

                return "Please enter a valid 10-digit mobile number.";
            }

            if (lowerMessage.contains("password")) {
                return "Please check your password and try again.";
            }

            if (lowerMessage.contains("validation") ||
                    lowerMessage.contains("invalid data") ||
                    lowerMessage.contains("bad request")) {

                return "Some details are invalid. Please check all fields and try again.";
            }

            if (lowerMessage.equals("not found") ||
                    lowerMessage.contains("route not found")) {

                return "The registration service is temporarily unavailable. Please try again later.";
            }

            if (lowerMessage.contains("unauthorized") ||
                    lowerMessage.contains("forbidden")) {

                return "Registration is temporarily unavailable. Please try again later.";
            }

            return message;
        }

        if (responseCode == 400) {
            return "Some details are invalid. Please check all fields and try again.";
        }

        if (responseCode == 401 || responseCode == 403) {
            return "Registration is temporarily unavailable. Please try again later.";
        }

        if (responseCode == 404) {
            return "The registration service is temporarily unavailable. Please try again later.";
        }

        if (responseCode == 422) {
            return "Please enter valid registration details.";
        }

        if (responseCode >= 500) {
            return "The server is currently unavailable. Please try again shortly.";
        }

        return "We could not create your account. Please check your details and try again.";
    }

    private void setLoadingState(boolean loading) {

        isRequestInProgress = loading;

        runOnUiThread(() -> {

            registerButton.setEnabled(!loading);
            loginText.setEnabled(!loading);

            if (loading) {

                registerButton.setText(R.string.registering);

            } else {

                registerButton.setText(R.string.register);
            }
        });
    }

    private void showRegistrationSuccessDialog(
            String message
    ) {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        final androidx.appcompat.app.AlertDialog dialog =
                new MaterialAlertDialogBuilder(
                        signup.this,
                        R.style.AppMaterialDialogTheme
                )
                        .setIcon(R.drawable.ic_check_circle)
                        .setTitle("Registration Successful")
                        .setMessage("Your account has been created successfully.")
                        .setCancelable(false)
                        .create();

        dialog.show();

        new android.os.Handler(android.os.Looper.getMainLooper())
                .postDelayed(() -> {

                    if (!isFinishing()
                            && !isDestroyed()
                            && dialog.isShowing()) {

                        dialog.dismiss();
                    }

                    openLoginScreen();

                }, 2000); // 2 seconds
    }

    private void showAlreadyRegisteredDialog() {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(
                signup.this,
                R.style.AppMaterialDialogTheme
        )
                .setIcon(R.drawable.ic_error_outline)
                .setTitle("Account already exists")
                .setMessage(
                        "This mobile number or email address is already registered. " +
                                "Please log in to continue."
                )
                .setCancelable(true)
                .setPositiveButton(
                        "Go to Login",
                        (dialog, which) -> {
                            dialog.dismiss();
                            openLoginScreen();
                        }
                )
                .show();
    }

    private void showErrorDialog(
            String title,
            String message
    ) {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(
                signup.this,
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

    private void openLoginScreen() {

        Intent intent =
                new Intent(
                        signup.this,
                        login.class
                );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }

    private String getValue(
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

    private void clearErrors() {

        clearInputError(nameInputLayout);
        clearInputError(phoneInputLayout);
        clearInputError(emailInputLayout);
        clearInputError(passwordInputLayout);
        clearInputError(confirmPasswordInputLayout);
    }

    @Override
    protected void onDestroy() {

        executorService.shutdownNow();
        super.onDestroy();
    }
}