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

package org.terasology.dynamicCities.settlements;

import org.terasology.dynamicCities.sites.SiteComponent;
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector2i;
import org.terasology.reflection.MappedContainer;

/**
 * Provides information on a settlement.
  */
@MappedContainer
public class SettlementComponent implements Component {

    private Vector2i coords = new Vector2i();
    private int population;
    private String name;

    public SettlementComponent() {
    }

    public SettlementComponent(SiteComponent siteComponent, String name) {
        this.coords = new Vector2i(siteComponent.getPos());
        this.name = name;
    }

    public SettlementComponent(SiteComponent siteComponent, String name, int population) {
        this.coords = new Vector2i(siteComponent.getPos());
        this.name = name;
        this.population = population;
    }

    /**
     * @return the site of the settlement
     */
    public Vector2i getCoords() {
        return coords;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     *  @return population of the settlement
     */
    public int getPopulation() {
        return population;
    }

    @Override
    public String toString() {
        return name + " (" + coords + ")";
    }

}
