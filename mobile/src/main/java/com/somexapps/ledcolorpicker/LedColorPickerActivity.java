package com.somexapps.ledcolorpicker;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
    private int currentColor;
    private SharedPreferences preferences;
    private TextView currentColorTextView;
    private View currentColorView;
    private static final String DIALOG_COLOR_PICKER_IDENTIFIER = "dialog_color_picker";
    private SolidColorApiService colorApiService;
    private AlertDialog errorUpdatingColorDialog;

    private TextInputEditText redColorEditText;
    private TextInputEditText greenColorEditText;
    private TextInputEditText blueColorEditText;
    private Button updateButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up main view
        setContentView(R.layout.activity_led_color_picker);

        errorUpdatingColorDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.error_sending_color_to_service_text)
                .setNeutralButton(android.R.string.ok, null)
                .create();

        // Create api service
        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(ApiConstants.SOLID_COLOR_API_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
        colorApiService = retrofit.create(SolidColorApiService.class);

        // Grab views and set click listeners
        currentColorTextView =
                (TextView) findViewById(R.id.activity_led_color_picker_current_color_text);
        Button pickNewColorButton =
                (Button) findViewById(R.id.activity_led_color_picker_new_color_button);

        redColorEditText = (TextInputEditText) findViewById(R.id.red_color_value_et);
        greenColorEditText = (TextInputEditText) findViewById(R.id.green_color_value_et);
        blueColorEditText = (TextInputEditText) findViewById(R.id.blue_color_value_et);
        updateButton = (Button) findViewById(R.id.update_button_bt);

        preferences = getPreferences(Context.MODE_PRIVATE);

        // Grab color view to set current color
        currentColorView = findViewById(R.id.activity_led_color_picker_current_color_view);

        // Update the current color with saved color, if it exists
        currentColor = preferences.getInt(
                Constants.PREF_SAVED_COLOR_VALUE,
                Constants.DEFAULT_SAVED_COLOR_VALUE
        );

        // Update color on the tv
        sendColorToService(currentColor);

        updateButton.setOnClickListener(view -> {
            String hexFromRgb = ColorParser.rgbToHex(
                    Integer.valueOf(redColorEditText.getText().toString()),
                    Integer.valueOf(greenColorEditText.getText().toString()),
                    Integer.valueOf(blueColorEditText.getText().toString())
            );
            sendColorToService(Color.parseColor(hexFromRgb));
        });

        // Set up button click
        pickNewColorButton.setOnClickListener(view -> new ChromaDialog.Builder()
                .initialColor(currentColor)
                .colorMode(ColorMode.RGB) // RGB, ARGB, HVS, CMYK, CMYK255, HSL
                .indicatorMode(IndicatorMode.DECIMAL) //HEX or DECIMAL; Note that (HSV || HSL || CMYK) && IndicatorMode.HEX is a bad idea
                // Set color text view when selected
                .onColorSelected(this::sendColorToService)
                .create()
                .show(getSupportFragmentManager(), DIALOG_COLOR_PICKER_IDENTIFIER));
    }

    private void sendColorToService(int newColor) {
        // Get human readable hex string from integer.
        final String hexString = ChromaUtil.getFormattedColorString(newColor, false);

        // Grab individual RGB values to send to server.
        int[] rgbArray = ColorParser.hexToRgb(hexString);

        // Make sure we have a saved color
        if (rgbArray != null &&
                rgbArray.length == 3) {
            // Turn on leds with saved values
            colorApiService
                    .solidColor(rgbArray[0], rgbArray[1], rgbArray[2])
                    .enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            // do nothing
                            if (response.isSuccessful()) {
                                updateColorUi(newColor, hexString, rgbArray);
                            } else {
                                errorUpdatingColorDialog.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            errorUpdatingColorDialog.show();
                        }
                    });
        }
    }

    private void updateColorUi(int newColor, String hexString, int[] rgbArray) {
        // Update color
        currentColor = newColor;

        // Update text view with new color
        currentColorTextView.setText(
                getString(
                        R.string.current_color_header,
                        hexString
                )
        );

        redColorEditText.setText(String.valueOf(rgbArray[0]));
        greenColorEditText.setText(String.valueOf(rgbArray[1]));
        blueColorEditText.setText(String.valueOf(rgbArray[2]));

        // Update color view
        currentColorView.setBackgroundColor(currentColor);

        // Save color in shared preferences
        preferences
                .edit()
                .putInt(Constants.PREF_SAVED_COLOR_VALUE, currentColor)
                .apply();
    }
}
