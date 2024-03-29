// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.economy;


import org.terasology.dynamicCities.population.PopulationComponent;
import org.terasology.economy.handler.StorageComponentHandler;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashSet;
import java.util.Set;


public class PopulationStorageHandler implements StorageComponentHandler<PopulationComponent> {

    @Override
    public int store(PopulationComponent populationComponent, String resource, int amount) {
        if (resource.equals(populationComponent.popResourceType)) {
            if (populationComponent.populationSize + amount <= populationComponent.capacity) {
                populationComponent.populationSize += amount;
                return 0;
            } else {
                populationComponent.populationSize = populationComponent.capacity;
                return Math.round(amount - populationComponent.capacity - populationComponent.populationSize);
            }
        } else {
            return amount;
        }
    }

    @Override
    public int draw(PopulationComponent populationComponent, String resource, int amount) {
        if (resource.equals(populationComponent.popResourceType)) {
            if (populationComponent.populationSize >= amount) {
                populationComponent.populationSize -= amount;
                return 0;
            } else {
                int amountLeft = Math.round(amount - populationComponent.populationSize);
                populationComponent.populationSize = 0;
                return amountLeft;
            }
        } else {
            return amount;
        }
    }

    @Override
    public int availableResourceAmount(PopulationComponent populationComponent, String resource) {
        return Math.round(populationComponent.populationSize);
    }

    @Override
    public int availableResourceCapacity(PopulationComponent populationComponent, String resource) {
        return Math.round(populationComponent.capacity - populationComponent.populationSize);
    }

    @Override
    public Set<String> availableResourceTypes(PopulationComponent populationComponent) {
        Set<String> result = new HashSet<>();
        result.add(populationComponent.popResourceType);
        return result;
    }

    @Override
    public Class getStorageComponentClass() {
        return PopulationComponent.class;
    }

    @Override
    public Component getTestComponent() {
        return new PopulationComponent("test");
    }
    @Override
    public String getTestResource() {
        return new PopulationComponent().popResourceType;
    }
}
