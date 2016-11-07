package com.somexapps.ledcolorpicker;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.gson.JsonObject;
import com.somexapps.ledcolorpicker.shared.api.SolidColorApiService;
import com.somexapps.ledcolorpicker.shared.utils.ApiConstants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Michael Limb on 11/6/2016.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class LedColorPickerService extends Service {
    private static final String TAG = LedColorPickerService.class.getSimpleName();
    private SolidColorApiService apiService;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // Create api service
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(ApiConstants.SOLID_COLOR_API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
        apiService = retrofit.create(SolidColorApiService.class);

        // Create receiver to listen to screen on/off events
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
                    // Update LEDs to currently stored color
                    showColors(true);
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    // Set LEDs to black
                    showColors(false);
                }
            }
        }, intentFilter);

        // Set initial colors, since screen should be on when activity is created.
        showColors(true);
    }

    private void showColors(boolean shouldShow) {
        // Call colors endpoint to show/hide colors
        apiService
                .color(shouldShow)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        // do nothing
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e(TAG, "Error calling color api:", t);
                    }
                });
    }
}
