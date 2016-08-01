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


import org.terasology.economy.StorageComponentHandler;
import org.terasology.entitySystem.Component;


public class PopulationStorageHandler implements StorageComponentHandler<Population> {

    @Override
    public int store(Population population, String resource, int amount) {
        if (resource.equals(population.popResourceType)) {
            if (population.populationSize + amount <= population.capacity) {
                population.populationSize += amount;
                return 0;
            } else {
                population.populationSize = population.capacity;
                return Math.round(amount - population.capacity - population.populationSize);
            }
        } else {
            return amount;
        }
    }

    @Override
    public int draw(Population population, String resource, int amount) {
        if (resource.equals(population.popResourceType)) {
            if (population.populationSize >= amount) {
                population.populationSize -= amount;
                return 0;
            } else {
                int amountLeft = Math.round(amount - population.populationSize);
                population.populationSize = 0;
                return amountLeft;
            }
        } else {
            return amount;
        }
    }

    @Override
    public int availableResourceAmount(Population population, String resource) {
        return Math.round(population.populationSize);
    }

    @Override
    public int availableResourceCapacity(Population population, String resource) {
        return Math.round(population.capacity - population.populationSize);
    }

    @Override
    public Class getStorageComponentClass() {
        return Population.class;
    }

    @Override
    public Component getTestComponent() {
        return new Population("test");
    }
    @Override
    public String getTestResource() {
        return new Population().popResourceType;
    }
}
