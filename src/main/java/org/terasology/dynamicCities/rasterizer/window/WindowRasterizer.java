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

package org.terasology.dynamicCities.rasterizer.window;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.window.Window;
import org.terasology.commonworld.heightmap.HeightMap;

/**
 * @param <T> the target class
 */
public abstract class WindowRasterizer<T extends Window> {

    private final BlockTheme theme;
    private final Class<T> targetClass;

    /**
     * @param theme the block theme that is used to map type to blocks
     * @param targetClass the target class that is rasterized
     */
    protected WindowRasterizer(BlockTheme theme, Class<T> targetClass) {
        this.theme = theme;
        this.targetClass = targetClass;
    }

    public void tryRaster(RasterTarget brush, Window window, HeightMap heightMap) {
        if (targetClass.isInstance(window)) {
            raster(brush, targetClass.cast(window), heightMap);
        }
    }

    protected abstract void raster(RasterTarget brush, T part, HeightMap heightMap);
}

