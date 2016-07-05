package com.somexapps.ledcolorpicker;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonObject;
import com.somexapps.ledcolorpicker.api.SolidColorApiService;
import com.somexapps.ledcolorpicker.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Michael Limb on 7/1/2016.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class LedColorPickerActivity extends Activity {
    private static final String TAG = LedColorPickerActivity.class.getSimpleName();

    // View bindings
    @BindView(R.id.activity_main_red_color_text) EditText redColorEditText;
    @BindView(R.id.activity_main_blue_color_text) EditText blueColorEditText;
    @BindView(R.id.activity_main_green_color_text) EditText greenColorEditText;
    @BindView(R.id.activity_main_color_update_button) Button colorUpdateButton;

    // API object for making calls to solid color API
    private SolidColorApiService apiService;

    // Fields to hold the color values that were last set
    private int redColor = 0;
    private int blueColor = 0;
    private int greenColor = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Bind ButterKnife views.
        ButterKnife.bind(this);

        // Create api service
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(Constants.SOLID_COLOR_API_BASE_URL)
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
                    updateColor();
                } else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                    // Set LEDs to black
                    clearColor();
                }
            }
        }, intentFilter);

        // Register update color button click events
        colorUpdateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update values from edit texts
                redColor = Integer.parseInt(redColorEditText.getText().toString());
                greenColor = Integer.parseInt(greenColorEditText.getText().toString());
                blueColor = Integer.parseInt(blueColorEditText.getText().toString());

                // Update color with api
                updateColor();
            }
        });

        // Set initial colors, since screen should be on when activity is created.
        updateColor();
    }

    /**
     * Method used by the update color button and receiver to update colors accordingly.
     */
    private void updateColor() {
        // Turn on leds with saved values
        apiService
                .solidColor(redColor, greenColor, blueColor)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        // do nothing
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e(TAG, "Error calling solid color api:", t);
                    }
                });
    }

    private void clearColor() {
        // Turn off leds
        apiService
                .solidColor(0, 0, 0)
                .enqueue(new Callback<JsonObject>() {
                    @Override
                    public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                        // do nothing
                    }

                    @Override
                    public void onFailure(Call<JsonObject> call, Throwable t) {
                        Log.e(TAG, "Error calling solid color api:", t);
                    }
                });
    }
}
