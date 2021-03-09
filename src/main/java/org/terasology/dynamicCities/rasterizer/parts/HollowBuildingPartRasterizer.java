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

import com.google.common.math.DoubleMath;
import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.HollowBuildingPart;
import org.terasology.cities.common.Edges;
import org.terasology.cities.raster.BuildingPens;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.BlockAreac;

import java.math.RoundingMode;

/**
 * Converts a {@link HollowBuildingPart} into blocks
 */
public class HollowBuildingPartRasterizer extends AbsDynBuildingRasterizer<HollowBuildingPart> {

    public HollowBuildingPartRasterizer(BlockTheme theme, WorldProvider worldProvider) {
        super(theme, HollowBuildingPart.class, worldProvider);
    }

    @Override
    protected void raster(RasterTarget brush, HollowBuildingPart part, HeightMap heightMap) {
        BlockAreac rc = part.getShape();

        if (!rc.intersectsBlockArea(brush.getAffectedArea())) {
            return;
        }

//      TODO: check y overlap

        int baseHeight = part.getBaseHeight();
        int wallHeight = part.getWallHeight();
        int arcRadius = part.getArcRadius();

        Pen floorPen = BuildingPens.floorPen(brush, heightMap, baseHeight, DefaultBlockType.BUILDING_FLOOR);
        RasterUtil.fillRect(floorPen, rc);

        HeightMap hmTop = HeightMaps.constant(baseHeight + wallHeight);
        HeightMap hmBottom = new HeightMap() {

            @Override
            public int apply(int x, int z) {

                int distToCornerSq = Edges.getDistanceToCornerSq(rc, x, z);
                if (distToCornerSq == 0) {
                    return baseHeight;
                }

                int top = hmTop.apply(x, z);
                int arcSq = arcRadius * arcRadius;
                int dxSq = Math.max(0, arcSq - distToCornerSq);
                int dy = DoubleMath.roundToInt(Math.sqrt(arcSq - dxSq), RoundingMode.HALF_UP);
                return top - arcRadius + dy;
            }

        };

        // create walls
        Pen wallPen = Pens.fill(brush, hmBottom, hmTop, DefaultBlockType.BUILDING_WALL);
        RasterUtil.drawRect(wallPen, rc);
    }
}

