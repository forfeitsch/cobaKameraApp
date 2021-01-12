package com.example.cobakamera;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface ApiService {
    @POST("localhost:8000/api/store")
    Call<Post> savePost(@Body String foto);
}
