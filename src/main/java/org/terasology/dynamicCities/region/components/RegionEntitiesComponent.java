// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector2i;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedContainer
public class RegionEntitiesComponent implements Component<RegionEntitiesComponent> {

    public Map<Vector2i, EntityRef> regionEntities = new HashMap<>();
    public int gridSize;
    //This stores information about the loaded state of several regions packed into a cell
    public Map<Vector2i, Integer> cellGrid = Maps.newHashMap();
    public List<String> processed = Lists.newArrayList();
    public int cellSize;

    public RegionEntitiesComponent() {
    }

    public RegionEntitiesComponent(int gridSize) {
        this.gridSize = gridSize;
        this.cellSize = gridSize * gridSize / (32 * 32);
    }


    @Override
    public void copyFrom(RegionEntitiesComponent other) {
        this.regionEntities = Maps.newHashMap(other.regionEntities);
        this.gridSize = other.gridSize;
        this.cellGrid = Maps.newHashMap(other.cellGrid);
        this.processed = Lists.newArrayList(other.processed);
        this.cellSize = other.cellSize;
    }
}


