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
import org.terasology.math.geom.Vector3i;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

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

    public static Vector3i stringToVector3i(String string) {
        if (string.matches("\\(-?\\d+,\\s-?\\d+,\\s-?\\d+\\)")) {
            String[] numbers = string.split("[,()\\s]");
            int x = Integer.parseInt(numbers[1]);
            int y = Integer.parseInt(numbers[3]);
            int z = Integer.parseInt(numbers[5]);
            return new Vector3i(x, y, z);
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

    public static float distance(float[] a, float[] b) {
        if (a.length != b.length) {
            System.out.print("Error in Function distance: Arrays have different Dimension");
            return 0;
        }
        float sum = 0;
        for (int i = 0; i < a.length; i++) {
            sum += Math.pow(a[i] - b[i], 2);
        }
        sum = (float) Math.sqrt(sum);
        return sum;
    }

    public static float distance(int[] a, int[] b) {
        return (float) Math.sqrt(Math.pow(a[0] - b[0], 2) + Math.pow(a[1] - b[1], 2));
    }

    public static float abs(float[] a) {
        float abs = 0;
        for (int i = 0; i < a.length; i++) {
            abs += Math.pow(a[i], 2);
        }
        abs = (float) Math.sqrt(abs);
        return abs;
    }

    public static int[][] arrayToMatrix(int[] array, int rows, int columns) {
        int[][] matrix = new int[rows][columns];
        for (int i = 0; i < rows; i++) {
            System.arraycopy(array, 0 + i * columns, matrix[i], 0, columns);
        }
        return matrix;
    }
    public static void stringsToLowerCase(List strings) {
        ListIterator<String> iterator = strings.listIterator();
        while (iterator.hasNext())
        {
            iterator.set(iterator.next().toLowerCase());
        }
    }
    public static Map<String, Integer> stringsToLowerCase(Map<String, Integer> map) {
        Set<String> keySet = Collections.unmodifiableSet(map.keySet());
        Map<String, Integer> newMap = new HashMap<>();
        for (String key : keySet) {
            newMap.put(key.toLowerCase(), map.get(key));
        }
        return newMap;
    }
}
