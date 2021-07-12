// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.population;


import org.terasology.gestalt.entitysystem.component.Component;

public class PopulationComponent implements Component<PopulationComponent> {


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


    @Override
    public void copy(PopulationComponent other) {
        this.populationSize = other.populationSize;
        this.health = other.health;
        this.capacity = other.capacity;
        this.popResourceType = other.popResourceType;
    }
}
