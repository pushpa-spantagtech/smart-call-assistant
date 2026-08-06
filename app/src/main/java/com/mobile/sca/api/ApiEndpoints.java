package com.mobile.sca.api;

public final class ApiEndpoints {

    private ApiEndpoints() {
    }

    public static final String LOGIN =
            ApiConfig.BASE_URL + "/api/auth/login";

    public static final String SIGNUP =
            ApiConfig.BASE_URL + "/api/auth/signup";

    public static final String SEND_OTP =
            ApiConfig.BASE_URL + "/api/auth/forgot-password/send-otp";

    public static final String VERIFY_OTP =
            ApiConfig.BASE_URL + "/api/auth/forgot-password/verify-otp";

    public static final String RESET_PASSWORD =
            ApiConfig.BASE_URL + "/api/auth/forgot-password/reset";
}