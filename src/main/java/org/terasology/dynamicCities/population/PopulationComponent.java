// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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

    public PopulationComponent() {
    }

    public PopulationComponent(String arg) {
        if (arg.equals("test")) {
            capacity = 999;
        }
    }

    public void grow(float growthRate) {
        populationSize += growthRate /* * populationSize*/;
    }


}