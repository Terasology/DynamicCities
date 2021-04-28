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
