package com.somexapps.ledcolorpicker.utils;

/**
 * Created by Michael Limb on 7/12/2016.
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
public class ColorParser {
    public static int[] hexToRgb(String hexString) {
        // Make sure the hex string is the correct length.
        if (hexString.length() == 7) {
            // Lob off the # on the front
            String parsedHexString = hexString.substring(1);

            int[] rgbArray = new int[3];

            // Save RGB values in respective positions.
            rgbArray[0] = Integer.parseInt(parsedHexString.substring(0, 2), 16);
            rgbArray[1] = Integer.parseInt(parsedHexString.substring(2, 4), 16);
            rgbArray[2] = Integer.parseInt(parsedHexString.substring(4, 6), 16);

            // Return result
            return rgbArray;
        } else {
            // Return nothing
            return null;
        }
    }

    public static String rgbToHex(int red, int green, int blue) {
        return String.format("#%02x%02x%02x", red, green, blue);
    }
}
