package com.somexapps.ledcolorpicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.pavelsikun.vintagechroma.ChromaDialog;
import com.pavelsikun.vintagechroma.ChromaUtil;
import com.pavelsikun.vintagechroma.IndicatorMode;
import com.pavelsikun.vintagechroma.colormode.ColorMode;
import com.somexapps.ledcolorpicker.shared.api.SolidColorApiService;
import com.somexapps.ledcolorpicker.shared.utils.ApiConstants;
import com.somexapps.ledcolorpicker.utils.ColorParser;
import com.somexapps.ledcolorpicker.utils.Constants;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LedColorPickerActivity extends AppCompatActivity {
    private static final String TAG = LedColorPickerActivity.class.getSimpleName();

    // Used to store the current color that is set
    private int currentColor;

    // Shared prefs for holding the saved color value
    private SharedPreferences preferences;

    // Current color text view
    private TextView currentColorTextView;

    // Current color view
    private View currentColorView;

    // Used to identify the color picker dialog
    private static final String DIALOG_COLOR_PICKER = "dialog_color_picker";

    // API object for making calls to solid color API
    private SolidColorApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up main view
        setContentView(R.layout.activity_led_color_picker);

        // Create api service
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(ApiConstants.SOLID_COLOR_API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
        apiService = retrofit.create(SolidColorApiService.class);

        // Grab views and set click listeners
        currentColorTextView =
                (TextView) findViewById(R.id.activity_led_color_picker_current_color_text);
        Button updateColorButton =
                (Button) findViewById(R.id.activity_led_color_picker_new_color_button);

        preferences = getPreferences(Context.MODE_PRIVATE);

        // Grab color view to set current color
        currentColorView = findViewById(R.id.activity_led_color_picker_current_color_view);

        // Update the current color with saved color, if it exists
        int savedColor = preferences.getInt(
                Constants.PREF_SAVED_COLOR_VALUE,
                Constants.DEFAULT_SAVED_COLOR_VALUE
        );

        // Update color on the tv
        updateColor(savedColor);

        // Set up button click
        updateColorButton.setOnClickListener(view -> new ChromaDialog.Builder()
                .initialColor(savedColor)
                .colorMode(ColorMode.RGB) // RGB, ARGB, HVS, CMYK, CMYK255, HSL
                .indicatorMode(IndicatorMode.DECIMAL) //HEX or DECIMAL; Note that (HSV || HSL || CMYK) && IndicatorMode.HEX is a bad idea
                // Set color text view when selected
                .onColorSelected(this::updateColor)
                .create()
                .show(getSupportFragmentManager(), DIALOG_COLOR_PICKER));
    }

    private void updateColor(int newColor) {
        // Update color
        currentColor = newColor;

        // Get human readable hex string from integer.
        String hexString = ChromaUtil.getFormattedColorString(currentColor, false);

        // Update text view with new color
        currentColorTextView.setText(
                getString(
                        R.string.activity_led_color_picker_current_color_header,
                        hexString
                )
        );

        // Update color view
        currentColorView.setBackgroundColor(newColor);

        // Save color in shared preferences
        preferences
                .edit()
                .putInt(Constants.PREF_SAVED_COLOR_VALUE, newColor)
                .apply();

        // Grab individual RGB values to send to server.
        int[] rgbArray = ColorParser.hexToRgb(hexString);

        // Make sure we have a saved color
        if (rgbArray != null &&
                rgbArray.length == 3) {
            // Turn on leds with saved values
            apiService
                    .solidColor(rgbArray[0], rgbArray[1], rgbArray[2])
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
}
