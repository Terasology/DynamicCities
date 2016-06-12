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
import org.terasology.cities.model.roof.DomeRoof;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.math.geom.Rect2i;
import org.terasology.world.WorldProvider;

/**
 * Converts a {@link DomeRoof} into blocks
 */
public class DomeRoofRasterizer extends AbsDynBuildingRasterizer<DomeRoof> {

    /**
     * @param theme the block theme to use
     */
    public DomeRoofRasterizer(BlockTheme theme, WorldProvider worldProvider) {
        super(theme, DomeRoof.class, worldProvider);
    }

    @Override
    public void raster(RasterTarget target, DomeRoof roof, HeightMap hm) {
        final Rect2i area = roof.getArea();

        if (!area.overlaps(target.getAffectedArea())) {
            return;
        }

//        The surface of an ellipsoid is defined as the set of points(x, y, z) that ..
//
//         x^2     y^2     z^2
//        ----- + ----- + ----- = 1
//         a^2     b^2     c^2
//
//
//                 (      x^2     z^2  )
//       y^2 = b^2 ( 1 - ----- - ----- )
//                 (      a^2     c^2  )
//

        HeightMap topHm = new HeightMap() {

            @Override
            public int apply(int rx, int rz) {
                int height = roof.getHeight();
                float y = getY(area, height, rx + 0.5f, rz + 0.5f);        // measure at block center

                return roof.getBaseHeight() + (int) Math.max(1, y);
            }
        };

        HeightMap bottomHm = new HeightMap() {

            @Override
            public int apply(int rx, int rz) {
                int baseHeight = roof.getBaseHeight();

                if (rx == area.minX() || rz == area.minY()) {
                    return baseHeight;
                }

                if (rx == area.maxX() || rz == area.maxY()) {
                    return baseHeight;
                }

                int height = roof.getHeight();
                float y = getY(area, height, rx + 0.5f, rz + 0.5f);        // measure at block center

                return baseHeight + (int) Math.max(0, y - 2);
            }
        };

        Pen pen = Pens.fill(target, bottomHm, topHm, DefaultBlockType.ROOF_FLAT);
        RasterUtil.fillRect(pen, area);
    }

    private float getY(Rect2i area, int height, float rx, float rz) {

        float x = rx - area.minX() - area.width() * 0.5f;   // distance from the center
        float z = rz - area.minY() - area.height() * 0.5f;

        float a = area.width() * 0.5f;
        float b = height;
        float c = area.height() * 0.5f;

        float y2 = b * b * (1 - (x * x) / (a * a) - (z * z) / (c * c));

        return (float) Math.sqrt(y2);
    }
}
