/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.dynamicCities.utilities;


import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;

public class Toolbox {

    public static Vector3f stringToVector3f(String string) {
        if (string.matches("\\(-?\\d+\\.\\d+,\\s-?\\d+\\.\\d+,\\s-?\\d+\\.\\d+\\)")) {
            String[] numbers = string.split("[,()]");
            float x = Float.parseFloat(numbers[1]);
            float y = Float.parseFloat(numbers[2]);
            float z = Float.parseFloat(numbers[3]);
            return new Vector3f(x, y, z);
        } else {
            throw new NullPointerException();
        }
    }

    public static Vector2i stringToVector2i(String string) {
        if (string.matches("\\(-?\\d+,\\s-?\\d+\\)")) {
            String[] numbers = string.split("[,()\\s]");
            int x = Integer.parseInt(numbers[1]);
            int y = Integer.parseInt(numbers[3]);
            return new Vector2i(x, y);
        } else {
            throw new NullPointerException();
        }
    }
}
