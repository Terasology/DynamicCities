// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.rasterizer;


import org.terasology.cities.BlockTheme;
import org.terasology.cities.bldg.Building;
import org.terasology.cities.bldg.BuildingPart;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.engine.world.WorldProvider;

public abstract class AbsDynBuildingRasterizer<T> {
    protected final BlockTheme theme;
    protected final Class<T> targetClass;
    protected final WorldProvider worldProvider;

    protected AbsDynBuildingRasterizer(BlockTheme theme, Class<T> targetClass, WorldProvider worldProvider) {
        this.theme = theme;
        this.targetClass = targetClass;
        this.worldProvider = worldProvider;
    }

    public void raster(RasterTarget brush, Building bldg, HeightMap hm) {
        for (BuildingPart part : bldg.getParts()) {
            if (targetClass.isInstance(part)) {
                raster(brush, targetClass.cast(part), hm);
            }
        }
    }

    protected abstract void raster(RasterTarget brush, T part, HeightMap heightMap);
}
