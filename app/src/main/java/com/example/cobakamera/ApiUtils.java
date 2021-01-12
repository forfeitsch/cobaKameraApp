package com.example.cobakamera;

public class ApiUtils {
    private ApiUtils() {}

    public static final String BASE_URL = "http://36.74.4.117/32/";

    public static ApiService getAPIService() {
        return ApiClient.getClient(BASE_URL).create(ApiService.class);
    }
}
