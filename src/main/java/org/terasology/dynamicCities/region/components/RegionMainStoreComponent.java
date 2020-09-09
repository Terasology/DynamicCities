// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;


import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedContainer
public class RegionMainStoreComponent implements Component {

    public Map<String, EntityRef> regionEntities;
    public int gridSize;
    //This stores information about the loaded state of several regions packed into a cell
    public Map<String, Integer> cellGrid;
    public List<String> processed;
    public int cellSize;

    public RegionMainStoreComponent() {
        regionEntities = new HashMap<>();
        cellGrid = new HashMap<>();
        processed = new ArrayList<>();
    }

    public RegionMainStoreComponent(int gridSize) {
        regionEntities = new HashMap<>();
        cellGrid = new HashMap<>();
        processed = new ArrayList<>();
        this.gridSize = gridSize;
        cellSize = gridSize * gridSize / (32 * 32);
    }


}


