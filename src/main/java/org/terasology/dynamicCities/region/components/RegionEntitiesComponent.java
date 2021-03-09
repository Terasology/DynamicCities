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
package org.terasology.dynamicCities.region.components;


import org.joml.Vector2i;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedContainer
public class RegionEntitiesComponent implements Component {

    public Map<Vector2i, EntityRef> regionEntities;
    public int gridSize;
    //This stores information about the loaded state of several regions packed into a cell
    public Map<Vector2i, Integer> cellGrid;
    public List<String> processed;
    public int cellSize;

    public RegionEntitiesComponent() {
        regionEntities = new HashMap<>();
        cellGrid = new HashMap<>();
        processed = new ArrayList<>();
    }

    public RegionEntitiesComponent(int gridSize) {
        regionEntities = new HashMap<>();
        cellGrid = new HashMap<>();
        processed = new ArrayList<>();
        this.gridSize = gridSize;
        cellSize = gridSize * gridSize / (32 * 32);
    }


}


