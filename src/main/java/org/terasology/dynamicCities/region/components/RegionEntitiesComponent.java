// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;


import org.joml.Vector2i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedContainer
public class RegionEntitiesComponent implements Component<RegionEntitiesComponent> {

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


