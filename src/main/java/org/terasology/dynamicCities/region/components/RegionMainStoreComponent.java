// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;


import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

@MappedContainer
public class RegionMainStoreComponent implements Component<RegionMainStoreComponent> {
    public int gridSize;
    public int cellSize;

    public RegionMainStoreComponent() {
    }

    public RegionMainStoreComponent(int gridSize) {
        this.gridSize = gridSize;
        this.cellSize = gridSize * gridSize / (32 * 32);
    }


    @Override
    public void copyFrom(RegionMainStoreComponent other) {
        this.gridSize = other.gridSize;
        this.cellSize = other.cellSize;
    }
}


