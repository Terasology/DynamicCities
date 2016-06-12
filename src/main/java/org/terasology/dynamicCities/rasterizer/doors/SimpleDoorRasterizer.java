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

package org.terasology.dynamicCities.rasterizer.doors;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.door.SimpleDoor;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.world.WorldProvider;

/**
 * Converts {@link SimpleDoor} into blocks (or air actually)
 */
public class SimpleDoorRasterizer extends AbsDynBuildingRasterizer<SimpleDoor> {

    /**
     * @param theme the block theme to use
     */
    public SimpleDoorRasterizer(BlockTheme theme, WorldProvider worldProvider) {
        super(theme, SimpleDoor.class, worldProvider);
    }

    @Override
    public void raster(RasterTarget target, SimpleDoor door, HeightMap hm) {
        if (target.getAffectedArea().contains(door.getPos())) {
            Pen pen = Pens.fill(target, door.getBaseHeight(), door.getTopHeight(), DefaultBlockType.SIMPLE_DOOR);
            pen.draw(door.getPos());
        }
    }
}
