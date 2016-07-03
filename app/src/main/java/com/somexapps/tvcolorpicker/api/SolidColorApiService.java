package com.somexapps.tvcolorpicker.api;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by something15525 on 7/3/2016.
 */
public interface SolidColorApiService {
    @POST("/solidcolor")
    Call<JsonObject> solidColor(@Query("red") int redValue,
                                @Query("green") int greenValue,
                                @Query("blue") int blueValue);
}
