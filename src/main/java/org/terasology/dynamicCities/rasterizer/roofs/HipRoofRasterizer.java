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

package org.terasology.dynamicCities.rasterizer.roofs;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.common.Edges;
import org.terasology.cities.model.roof.HipRoof;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Rect2i;
import org.terasology.world.WorldProvider;

/**
 * Converts a {@link HipRoof} into blocks
 */
public class HipRoofRasterizer extends AbsDynBuildingRasterizer<HipRoof> {

    /**
     * @param theme the block theme to use
     */
    public HipRoofRasterizer(BlockTheme theme, WorldProvider worldProvider) {
        super(theme, HipRoof.class, worldProvider);
    }

    @Override
    public void raster(RasterTarget target, HipRoof roof, HeightMap hm) {
        Rect2i area = roof.getArea();

        if (!area.overlaps(target.getAffectedArea())) {
            return;
        }

        // this is the ground truth
        // maxHeight = baseHeight + Math.min(cur.width, cur.height) * pitch / 2;

        HeightMap hmBottom = new HeightMap() {

            @Override
            public int apply(int x, int z) {
                int dist = Edges.getDistanceToBorder(area, x, z);
                int y = TeraMath.floorToInt(roof.getBaseHeight() + dist * roof.getPitch());
                return Math.min(y, roof.getMaxHeight());
            }
        };

        HeightMap hmTop = HeightMaps.offset(hmBottom, TeraMath.ceilToInt(roof.getPitch()));
        Pen pen = Pens.fill(target, hmBottom, hmTop, DefaultBlockType.ROOF_HIP);
        RasterUtil.fillRect(pen, area);
    }

}
