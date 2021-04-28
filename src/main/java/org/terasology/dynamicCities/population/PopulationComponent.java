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
package org.terasology.dynamicCities.population;


import org.terasology.engine.entitySystem.Component;

public class PopulationComponent implements Component {


    public float populationSize;
    public float health;
    public int capacity;
    public String popResourceType = "popUnit";
    public PopulationComponent(int size) {
        populationSize = size;
    }

    public PopulationComponent() { }

    public PopulationComponent(String arg) {
        if (arg.equals("test")) {
            capacity = 999;
        }
    }

    public void grow(float growthRate) {
        populationSize += growthRate /* * populationSize*/;
    }


}
