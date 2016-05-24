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
package org.terasology.dynamicCities.facets;

import org.terasology.dynamicCities.ressource.Ressource;
import org.terasology.dynamicCities.ressource.RessourceType;
import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.Border3D;

import java.util.Map;

public class RessourceFacet extends Grid2DFacet implements Component {

    private Region3i region;
    private Map<RessourceType, Ressource>[] ressources;

    public RessourceFacet(Region3i targetRegion, Border3D border, int gridSize) {
        super(targetRegion, border, gridSize);
        ressources = new Map<RessourceType, Ressource>[gridWorldRegion.area()];
    }

    //Modify that to get ressources per grid cell!
    public void addRessource(RessourceType type, Vector2i pos) {
        if (ressources.containsKey(type)) {
            ressources.get(type).amount += amount;
        } else {
            ressources.put( type, new Ressource(type));
            ressources.get(type).amount += amount;
        }
    }

}


