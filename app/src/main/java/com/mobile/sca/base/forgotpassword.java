package com.mobile.sca.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.mobile.sca.R;
import com.mobile.sca.api.ApiEndpoints;

import org.json.JSONArray;
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
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class forgotpassword extends AppCompatActivity {

    private static final String SEND_OTP_URL =
            ApiEndpoints.SEND_OTP;
    private static final String VERIFY_OTP_URL =
            ApiEndpoints.VERIFY_OTP;
    private static final String RESET_PASSWORD_URL =
            ApiEndpoints.RESET_PASSWORD;

    // Step sections
    private LinearLayout mobileNumberSection;
    private LinearLayout otpSection;
    private LinearLayout newPasswordSection;
    private LinearLayout confirmPasswordSection;

    // Step indicators
    private TextView stepOneIndicator;
    private TextView stepTwoIndicator;
    private TextView stepThreeIndicator;
    private TextView stepFourIndicator;

    // Email views
    private TextInputLayout emailInputLayout;
    private TextInputEditText emailEditText;
    private Button sendOtpButton;

    // OTP views
    private TextInputLayout otpInputLayout;
    private TextInputEditText otpEditText;
    private Button verifyOtpButton;
    private TextView otpSentMessage;
    private TextView resendOtpText;
    private TextView timerText;

    // New-password views
    private TextInputLayout newPasswordInputLayout;
    private TextInputEditText newPasswordEditText;
    private Button continuePasswordButton;

    // Confirm-password views
    private TextInputLayout confirmPasswordInputLayout;
    private TextInputEditText confirmPasswordEditText;
    private Button resetPasswordButton;

    private TextView backToLoginText;

    private String enteredEmailAddress = "";
    private String enteredOtp = "";
    private String enteredNewPassword = "";

    private boolean isRequestInProgress = false;

    private CountDownTimer countDownTimer;

    private final ExecutorService executorService =
            Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        initializeViews();
        setupClickListeners();
        setupErrorClearListeners();
        setupKeyboardBehavior();
        setupBackButton();

        showEmailStep();
    }

    private void initializeViews() {

        mobileNumberSection = findViewById(R.id.mobileNumberSection);
        otpSection = findViewById(R.id.otpSection);
        newPasswordSection = findViewById(R.id.newPasswordSection);
        confirmPasswordSection = findViewById(R.id.confirmPasswordSection);

        stepOneIndicator = findViewById(R.id.stepOneIndicator);
        stepTwoIndicator = findViewById(R.id.stepTwoIndicator);
        stepThreeIndicator = findViewById(R.id.stepThreeIndicator);
        stepFourIndicator = findViewById(R.id.stepFourIndicator);

        emailInputLayout = findViewById(R.id.mobileInputLayout);
        emailEditText = findViewById(R.id.mobileEditText);
        sendOtpButton = findViewById(R.id.sendOtpButton);

        otpInputLayout = findViewById(R.id.otpInputLayout);
        otpEditText = findViewById(R.id.otpEditText);
        verifyOtpButton = findViewById(R.id.verifyOtpButton);
        otpSentMessage = findViewById(R.id.otpSentMessage);
        resendOtpText = findViewById(R.id.resendOtpText);
        timerText = findViewById(R.id.timerText);

        newPasswordInputLayout =
                findViewById(R.id.newPasswordInputLayout);

        newPasswordEditText =
                findViewById(R.id.newPasswordEditText);

        continuePasswordButton =
                findViewById(R.id.continuePasswordButton);

        confirmPasswordInputLayout =
                findViewById(R.id.confirmPasswordInputLayout);

        confirmPasswordEditText =
                findViewById(R.id.confirmPasswordEditText);

        resetPasswordButton =
                findViewById(R.id.resetPasswordButton);

        backToLoginText = findViewById(R.id.backToLoginText);
    }

    private void setupClickListeners() {

        sendOtpButton.setOnClickListener(view -> {
            if (!isRequestInProgress) {
                validateAndSendOtp();
            }
        });

        verifyOtpButton.setOnClickListener(view -> {
            if (!isRequestInProgress) {
                validateAndVerifyOtp();
            }
        });

        continuePasswordButton.setOnClickListener(view -> {
            if (!isRequestInProgress) {
                validateNewPassword();
            }
        });

        resetPasswordButton.setOnClickListener(view -> {
            if (!isRequestInProgress) {
                validateAndResetPassword();
            }
        });

        resendOtpText.setOnClickListener(view -> {
            if (!isRequestInProgress &&
                    resendOtpText.isEnabled()) {

                resendOtp();
            }
        });

        backToLoginText.setOnClickListener(view -> {
            if (!isRequestInProgress) {
                openLoginScreen();
            }
        });
    }

    private void setupErrorClearListeners() {

        addErrorClearWatcher(
                emailEditText,
                emailInputLayout
        );

        addErrorClearWatcher(
                otpEditText,
                otpInputLayout
        );

        addErrorClearWatcher(
                newPasswordEditText,
                newPasswordInputLayout
        );

        addErrorClearWatcher(
                confirmPasswordEditText,
                confirmPasswordInputLayout
        );

        newPasswordEditText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence text,
                            int start,
                            int count,
                            int after
                    ) {
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
                    }
                }
        );
    }

    private void addErrorClearWatcher(
            TextInputEditText editText,
            TextInputLayout inputLayout
    ) {

        editText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence text,
                            int start,
                            int count,
                            int after
                    ) {
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
                    }
                }
        );
    }

    private void setupKeyboardBehavior() {

        emailEditText.setOnEditorActionListener(
                (view, actionId, event) -> {

                    hideKeyboard(emailEditText);
                    emailEditText.clearFocus();

                    return false;
                }
        );

        otpEditText.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence text,
                            int start,
                            int count,
                            int after
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence text,
                            int start,
                            int before,
                            int count
                    ) {

                        if (text != null &&
                                text.length() == 6) {

                            otpEditText.postDelayed(
                                    () -> hideKeyboard(otpEditText),
                                    100
                            );
                        }
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable
                    ) {
                    }
                }
        );

        newPasswordEditText.setOnEditorActionListener(
                (view, actionId, event) -> {

                    hideKeyboard(newPasswordEditText);
                    return false;
                }
        );

        confirmPasswordEditText.setOnEditorActionListener(
                (view, actionId, event) -> {

                    hideKeyboard(confirmPasswordEditText);
                    confirmPasswordEditText.clearFocus();

                    return false;
                }
        );
    }

    private void useStableOtpKeyboardMode() {

        /*
         * Keep the activity size unchanged and pan only enough to keep
         * the focused OTP field above the keyboard. This avoids the
         * resize shake while keeping the input visible.
         */
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        );
    }

    private void useNormalKeyboardMode() {

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        );
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

    private void setupBackButton() {

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {

                    @Override
                    public void handleOnBackPressed() {

                        if (isRequestInProgress) {
                            return;
                        }

                        if (confirmPasswordSection.getVisibility()
                                == View.VISIBLE) {

                            showNewPasswordStep();
                            return;
                        }

                        if (newPasswordSection.getVisibility()
                                == View.VISIBLE) {

                            showOtpStep();
                            return;
                        }

                        if (otpSection.getVisibility()
                                == View.VISIBLE) {

                            stopOtpTimer();
                            showEmailStep();
                            return;
                        }

                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
        );
    }

    private void validateAndSendOtp() {

        clearInputError(emailInputLayout);

        String emailAddress = getText(emailEditText);

        if (TextUtils.isEmpty(emailAddress)) {

            showInputError(
                    emailInputLayout,
                    emailEditText,
                    "Email address is required"
            );

            return;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(emailAddress)
                .matches()) {

            showInputError(
                    emailInputLayout,
                    emailEditText,
                    "Enter a valid email address"
            );

            return;
        }

        enteredEmailAddress = emailAddress;

        hideKeyboard(emailEditText);
        emailEditText.clearFocus();

        callSendOtpApi(false);
    }

    private void validateAndVerifyOtp() {

        clearInputError(otpInputLayout);

        String otp = getText(otpEditText);

        if (TextUtils.isEmpty(otp)) {

            showInputError(
                    otpInputLayout,
                    otpEditText,
                    "OTP is required"
            );

            return;
        }

        if (!otp.matches("[0-9]{6}")) {

            showInputError(
                    otpInputLayout,
                    otpEditText,
                    "Enter the valid 6-digit OTP"
            );

            return;
        }

        enteredOtp = otp;

        hideKeyboard(otpEditText);

        callVerifyOtpApi();
    }

    private void validateNewPassword() {

        clearInputError(newPasswordInputLayout);

        String password = getText(newPasswordEditText);

        if (TextUtils.isEmpty(password)) {

            showInputError(
                    newPasswordInputLayout,
                    newPasswordEditText,
                    "New password is required"
            );

            return;
        }

        String passwordError =
                getPasswordValidationError(password);

        if (passwordError != null) {

            showInputError(
                    newPasswordInputLayout,
                    newPasswordEditText,
                    passwordError
            );

            return;
        }

        enteredNewPassword = password;

        hideKeyboard(newPasswordEditText);
        newPasswordEditText.clearFocus();

        showConfirmPasswordStep();
    }

    private void validateAndResetPassword() {

        clearInputError(confirmPasswordInputLayout);

        String confirmPassword =
                getText(confirmPasswordEditText);

        if (TextUtils.isEmpty(confirmPassword)) {

            showInputError(
                    confirmPasswordInputLayout,
                    confirmPasswordEditText,
                    "Confirm password is required"
            );

            return;
        }

        if (!enteredNewPassword.equals(confirmPassword)) {

            showInputError(
                    confirmPasswordInputLayout,
                    confirmPasswordEditText,
                    "Passwords do not match"
            );

            return;
        }

        hideKeyboard(confirmPasswordEditText);
        confirmPasswordEditText.clearFocus();

        callResetPasswordApi(confirmPassword);
    }

    private void callSendOtpApi(boolean resendRequest) {

        setLoadingState(
                true,
                resendRequest
                        ? "Resending OTP..."
                        : "Sending OTP..."
        );

        executorService.execute(() -> {

            try {

                JSONObject requestBody = new JSONObject();
                requestBody.put(
                        "email",
                        enteredEmailAddress
                );

                ApiResult result = executePostRequest(
                        SEND_OTP_URL,
                        requestBody
                );

                runOnUiThread(() -> {

                    setLoadingState(false, "");

                    if (isSuccessfulResponse(result)) {

                        showOtpStep();
                        startOtpTimer();

                        showInformationDialog(
                                resendRequest
                                        ? "OTP resent"
                                        : "OTP sent",
                                TextUtils.isEmpty(result.message)
                                        ? "OTP has been sent to your email address."
                                        : result.message
                        );

                    } else {

                        showErrorDialog(
                                resendRequest
                                        ? "Unable to resend OTP"
                                        : "Unable to send OTP",
                                getApiErrorMessage(result)
                        );
                    }
                });

            } catch (JSONException exception) {

                showRequestCreationError();
            }
        });
    }

    private void callVerifyOtpApi() {

        setLoadingState(
                true,
                "Verifying OTP..."
        );

        executorService.execute(() -> {

            try {

                JSONObject requestBody = new JSONObject();

                requestBody.put(
                        "email",
                        enteredEmailAddress
                );

                requestBody.put(
                        "otp",
                        enteredOtp
                );

                ApiResult result = executePostRequest(
                        VERIFY_OTP_URL,
                        requestBody
                );

                runOnUiThread(() -> {

                    setLoadingState(false, "");

                    if (isSuccessfulResponse(result)) {

                        stopOtpTimer();
                        showNewPasswordStep();

                        showInformationDialog(
                                "OTP verified",
                                TextUtils.isEmpty(result.message)
                                        ? "Your email address was verified successfully."
                                        : result.message
                        );

                    } else {

                        showInputError(
                                otpInputLayout,
                                otpEditText,
                                getApiErrorMessage(result)
                        );
                    }
                });

            } catch (JSONException exception) {

                showRequestCreationError();
            }
        });
    }

    private void callResetPasswordApi(
            String confirmPassword
    ) {

        setLoadingState(
                true,
                "Resetting password..."
        );

        executorService.execute(() -> {

            try {

                JSONObject requestBody = new JSONObject();

                requestBody.put(
                        "email",
                        enteredEmailAddress
                );

                requestBody.put(
                        "newPassword",
                        enteredNewPassword
                );

                requestBody.put(
                        "confirmPassword",
                        confirmPassword
                );

                ApiResult result = executePostRequest(
                        RESET_PASSWORD_URL,
                        requestBody
                );

                runOnUiThread(() -> {

                    setLoadingState(false, "");

                    if (isSuccessfulResponse(result)) {

                        showPasswordResetSuccessDialog(
                                TextUtils.isEmpty(result.message)
                                        ? "Your password has been reset successfully."
                                        : result.message
                        );

                    } else {

                        showErrorDialog(
                                "Password reset failed",
                                getApiErrorMessage(result)
                        );
                    }
                });

            } catch (JSONException exception) {

                showRequestCreationError();
            }
        });
    }

    private ApiResult executePostRequest(
            String endpoint,
            JSONObject requestBody
    ) {

        HttpURLConnection connection = null;

        try {

            URL url = new URL(endpoint);

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

            return parseApiResult(
                    responseCode,
                    responseBody
            );

        } catch (IOException exception) {

            return new ApiResult(
                    0,
                    false,
                    "Unable to connect to the server. " +
                            "Check your internet connection and try again.",
                    ""
            );

        } finally {

            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private ApiResult parseApiResult(
            int responseCode,
            String responseBody
    ) {

        boolean success = false;
        String message = "";

        try {

            if (!TextUtils.isEmpty(responseBody)) {

                JSONObject responseJson =
                        new JSONObject(responseBody);

                success = responseJson.optBoolean(
                        "success",
                        responseCode >= 200 &&
                                responseCode < 300
                );

                message = responseJson.optString(
                        "message",
                        ""
                );

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
            success = responseCode >= 200 &&
                    responseCode < 300;
        }

        return new ApiResult(
                responseCode,
                success,
                message,
                responseBody
        );
    }

    private boolean isSuccessfulResponse(ApiResult result) {

        return result.responseCode >= 200 &&
                result.responseCode < 300 &&
                result.success;
    }

    private String getApiErrorMessage(ApiResult result) {

        if (!TextUtils.isEmpty(result.message)) {
            return result.message;
        }

        if (result.responseCode == 0) {
            return "Unable to connect to the server.";
        }

        if (result.responseCode == 400) {
            return "Invalid request information.";
        }

        if (result.responseCode == 401) {
            return "The request is not authorized.";
        }

        if (result.responseCode == 403) {
            return "The OTP request could not be completed. Please check the email address and try again.";
        }

        if (result.responseCode == 404) {
            return "No account was found for this email address.";
        }

        if (result.responseCode == 409) {
            return "The request could not be completed.";
        }

        if (result.responseCode == 422) {
            return "Please check the entered information.";
        }

        if (result.responseCode >= 500) {
            return "Server error. Please try again later.";
        }

        return "The request could not be completed. Please try again.";
    }

    private String extractFirstErrorMessage(
            JSONObject errors
    ) {

        Iterator<String> keys = errors.keys();

        while (keys.hasNext()) {

            String key = keys.next();
            Object value = errors.opt(key);

            if (value instanceof String) {
                return (String) value;
            }

            if (value instanceof JSONArray errorArray) {

                if (errorArray.length() > 0) {
                    return errorArray.optString(0);
                }
            }
        }

        return "";
    }

    private String readResponse(
            InputStream inputStream
    ) throws IOException {

        if (inputStream == null) {
            return "";
        }

        StringBuilder builder =
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
                builder.append(line);
            }
        }

        return builder.toString();
    }

    private void resendOtp() {

        if (TextUtils.isEmpty(enteredEmailAddress)) {

            showEmailStep();
            return;
        }

        clearInputError(otpInputLayout);
        otpEditText.setText("");
        enteredOtp = "";

        callSendOtpApi(true);
    }

    private String getPasswordValidationError(
            String password
    ) {

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

    private void showEmailStep() {

        useNormalKeyboardMode();

        mobileNumberSection.setVisibility(View.VISIBLE);
        otpSection.setVisibility(View.GONE);
        newPasswordSection.setVisibility(View.GONE);
        confirmPasswordSection.setVisibility(View.GONE);

        updateStepIndicators(1);
    }

    private void showOtpStep() {

        useStableOtpKeyboardMode();

        mobileNumberSection.setVisibility(View.GONE);
        otpSection.setVisibility(View.VISIBLE);
        newPasswordSection.setVisibility(View.GONE);
        confirmPasswordSection.setVisibility(View.GONE);

        String maskedEmail =
                maskEmailAddress(enteredEmailAddress);

        otpSentMessage.setText(
                getString(
                        R.string.otp_sent_message,
                        maskedEmail
                )
        );

        updateStepIndicators(2);
    }

    private void showNewPasswordStep() {

        useNormalKeyboardMode();

        mobileNumberSection.setVisibility(View.GONE);
        otpSection.setVisibility(View.GONE);
        newPasswordSection.setVisibility(View.VISIBLE);
        confirmPasswordSection.setVisibility(View.GONE);

        updateStepIndicators(3);
    }

    private void showConfirmPasswordStep() {

        useNormalKeyboardMode();

        mobileNumberSection.setVisibility(View.GONE);
        otpSection.setVisibility(View.GONE);
        newPasswordSection.setVisibility(View.GONE);
        confirmPasswordSection.setVisibility(View.VISIBLE);

        updateStepIndicators(4);
    }

    private void updateStepIndicators(int currentStep) {

        stepOneIndicator.setSelected(currentStep >= 1);
        stepTwoIndicator.setSelected(currentStep >= 2);
        stepThreeIndicator.setSelected(currentStep >= 3);
        stepFourIndicator.setSelected(currentStep >= 4);

        stepOneIndicator.setAlpha(
                currentStep >= 1 ? 1.0f : 0.4f
        );

        stepTwoIndicator.setAlpha(
                currentStep >= 2 ? 1.0f : 0.4f
        );

        stepThreeIndicator.setAlpha(
                currentStep >= 3 ? 1.0f : 0.4f
        );

        stepFourIndicator.setAlpha(
                currentStep >= 4 ? 1.0f : 0.4f
        );
    }

    private void startOtpTimer() {

        stopOtpTimer();

        resendOtpText.setEnabled(false);
        resendOtpText.setAlpha(0.5f);
        timerText.setVisibility(View.VISIBLE);

        countDownTimer =
                new CountDownTimer(60000, 1000) {

                    @Override
                    public void onTick(
                            long millisecondsUntilFinished
                    ) {

                        long seconds =
                                millisecondsUntilFinished / 1000;

                        timerText.setText(
                                getString(
                                        R.string.resend_timer,
                                        seconds
                                )
                        );
                    }

                    @Override
                    public void onFinish() {

                        timerText.setVisibility(View.GONE);
                        resendOtpText.setEnabled(true);
                        resendOtpText.setAlpha(1.0f);
                        countDownTimer = null;
                    }
                }.start();
    }

    private void stopOtpTimer() {

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void setLoadingState(
            boolean loading,
            String actionText
    ) {

        isRequestInProgress = loading;

        runOnUiThread(() -> {

            sendOtpButton.setEnabled(!loading);
            verifyOtpButton.setEnabled(!loading);
            continuePasswordButton.setEnabled(!loading);
            resetPasswordButton.setEnabled(!loading);
            resendOtpText.setEnabled(!loading);
            backToLoginText.setEnabled(!loading);

            emailEditText.setEnabled(!loading);
            otpEditText.setEnabled(!loading);
            newPasswordEditText.setEnabled(!loading);
            confirmPasswordEditText.setEnabled(!loading);

            if (loading) {

                if (mobileNumberSection.getVisibility() == View.VISIBLE) {
                    sendOtpButton.setText("Sending OTP...");
                }

                if (otpSection.getVisibility() == View.VISIBLE) {
                    verifyOtpButton.setText("Verifying OTP...");
                }

                if (confirmPasswordSection.getVisibility() == View.VISIBLE) {
                    resetPasswordButton.setText("Resetting...");
                }

            } else {

                sendOtpButton.setText("Send OTP");
                verifyOtpButton.setText("Verify OTP");
                resetPasswordButton.setText("Reset Password");
            }
        });
    }

    private void showRequestCreationError() {

        runOnUiThread(() -> {

            setLoadingState(false, "");

            showErrorDialog(
                    "Request error",
                    "Unable to create the request."
            );
        });
    }

    private void showInformationDialog(
            String title,
            String message
    ) {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        AlertDialog dialog =
                new MaterialAlertDialogBuilder(
                        forgotpassword.this,
                        R.style.AppMaterialDialogTheme
                )
                        .setIcon(R.drawable.ic_check_circle)
                        .setTitle(title)
                        .setMessage(getUserFriendlyMessage(message))
                        .setCancelable(false)
                        .create();

        dialog.show();

        new android.os.Handler(
                android.os.Looper.getMainLooper()
        ).postDelayed(() -> {

            if (!isFinishing()
                    && !isDestroyed()
                    && dialog.isShowing()) {

                dialog.dismiss();
            }

        }, 1000);
    }

    private void showErrorDialog(
            String title,
            String message
    ) {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(
                forgotpassword.this,
                R.style.AppMaterialDialogTheme
        )
                .setIcon(R.drawable.ic_error_outline)
                .setTitle(title)
                .setMessage(getUserFriendlyMessage(message))
                .setCancelable(true)
                .setPositiveButton(
                        "Try Again",
                        (dialog, which) -> {

                            dialog.dismiss();
                            focusCurrentInput();
                        }
                )
                .show();
    }

    private void showPasswordResetSuccessDialog(
            String message
    ) {

        if (isFinishing() || isDestroyed()) {
            return;
        }

        new MaterialAlertDialogBuilder(
                forgotpassword.this,
                R.style.AppMaterialDialogTheme
        )
                .setIcon(R.drawable.ic_check_circle)
                .setTitle("Password Reset Successful")
                .setMessage(getUserFriendlyMessage(message))
                .setCancelable(false)
                .setPositiveButton(
                        "Go to Login",
                        (dialog, which) -> {

                            dialog.dismiss();
                            openLoginScreen();
                        }
                )
                .show();
    }

    private String getUserFriendlyMessage(
            String message
    ) {

        if (TextUtils.isEmpty(message)) {
            return "The request could not be completed. Please try again.";
        }

        String trimmedMessage = message.trim();

        if (trimmedMessage.equalsIgnoreCase(
                "Internal Server Error"
        )) {

            return "Something went wrong on the server. Please try again shortly.";
        }

        if (trimmedMessage.equalsIgnoreCase(
                "Email address is not registered"
        ) || trimmedMessage.equalsIgnoreCase(
                "Email is not registered"
        )) {

            return "This email address is not registered. Please check the email or create an account first.";
        }

        if (trimmedMessage.equalsIgnoreCase(
                "Invalid OTP"
        )) {

            return "The OTP you entered is incorrect. Please check it and try again.";
        }

        if (trimmedMessage.toLowerCase().contains(
                "otp expired"
        )) {

            return "This OTP has expired. Please request a new OTP.";
        }

        return trimmedMessage;
    }

    private void focusCurrentInput() {

        if (mobileNumberSection.getVisibility()
                == View.VISIBLE) {

            requestInputFocus(emailEditText);
            return;
        }

        if (otpSection.getVisibility()
                == View.VISIBLE) {

            requestInputFocus(otpEditText);
            return;
        }

        if (newPasswordSection.getVisibility()
                == View.VISIBLE) {

            requestInputFocus(newPasswordEditText);
            return;
        }

        if (confirmPasswordSection.getVisibility()
                == View.VISIBLE) {

            requestInputFocus(confirmPasswordEditText);
        }
    }

    private void requestInputFocus(
            TextInputEditText editText
    ) {

        if (editText == null) {
            return;
        }

        editText.requestFocus();

        editText.postDelayed(
                () -> {

                    if (editText.getText() != null) {

                        editText.setSelection(
                                editText.length()
                        );
                    }
                },
                150
        );
    }

    private void showInputError(
            TextInputLayout inputLayout,
            TextInputEditText editText,
            String message
    ) {

        inputLayout.setErrorEnabled(true);
        inputLayout.setError(message);
        editText.requestFocus();
    }

    private void clearInputError(
            TextInputLayout inputLayout
    ) {

        inputLayout.setError(null);
    }

    private String maskEmailAddress(
            String emailAddress
    ) {

        if (emailAddress == null ||
                emailAddress.trim().isEmpty()) {

            return "";
        }

        int atIndex = emailAddress.indexOf("@");

        if (atIndex <= 1) {
            return emailAddress;
        }

        String localPart =
                emailAddress.substring(0, atIndex);

        String domainPart =
                emailAddress.substring(atIndex);

        int visibleCharacters =
                Math.min(2, localPart.length());

        StringBuilder maskedEmail =
                new StringBuilder(
                        localPart.substring(
                                0,
                                visibleCharacters
                        )
                );

        for (int index = visibleCharacters;
             index < localPart.length();
             index++) {

            maskedEmail.append("*");
        }

        return maskedEmail + domainPart;
    }

    private String getText(
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

    private void openLoginScreen() {

        Intent intent = new Intent(
                forgotpassword.this,
                login.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {

        stopOtpTimer();
        executorService.shutdownNow();

        super.onDestroy();
    }

    private static class ApiResult {

        private final int responseCode;
        private final boolean success;
        private final String message;
        private final String responseBody;

        private ApiResult(
                int responseCode,
                boolean success,
                String message,
                String responseBody
        ) {

            this.responseCode = responseCode;
            this.success = success;
            this.message = message;
            this.responseBody = responseBody;
        }
    }
}