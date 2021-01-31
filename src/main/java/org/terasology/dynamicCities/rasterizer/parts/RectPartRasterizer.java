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

package org.terasology.dynamicCities.rasterizer.parts;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.RectBuildingPart;
import org.terasology.cities.raster.BuildingPens;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockAreac;

/**
 * Converts a {@link RectBuildingPart} into blocks
 */
public class RectPartRasterizer extends AbsDynBuildingRasterizer<RectBuildingPart> {

    public RectPartRasterizer(BlockTheme theme, WorldProvider worldProvider) {
        super(theme, RectBuildingPart.class, worldProvider);
    }

    @Override
    protected void raster(RasterTarget brush, RectBuildingPart part, HeightMap heightMap) {
        BlockAreac rc = part.getShape();

        if (!rc.intersectsBlockArea(brush.getAffectedArea())) {
            return;
        }

        int baseHeight = part.getBaseHeight();
        int wallHeight = part.getWallHeight();

        Pen floorPen = BuildingPens.floorPen(brush, heightMap, baseHeight, DefaultBlockType.BUILDING_FLOOR);
        RasterUtil.fillRect(floorPen, rc);

        // create walls
        Pen wallPen = Pens.fill(brush, baseHeight, baseHeight + wallHeight, DefaultBlockType.BUILDING_WALL);
        RasterUtil.drawRect(wallPen, rc);
    }
}

