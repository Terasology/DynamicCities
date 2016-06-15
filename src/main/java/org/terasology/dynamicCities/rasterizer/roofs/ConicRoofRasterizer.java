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
import org.terasology.cities.model.roof.ConicRoof;
import org.terasology.cities.raster.*;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Circle;
import org.terasology.math.geom.Vector2i;

import java.math.RoundingMode;

/**
 * Converts a {@link ConicRoof} into blocks
 */
public class ConicRoofRasterizer extends RoofRasterizer<ConicRoof> {

    /**
     * @param theme the block theme to use
     */
    public ConicRoofRasterizer(BlockTheme theme) {
        super(theme, ConicRoof.class);
    }

    @Override
    public void raster(RasterTarget target, ConicRoof roof, HeightMap hm) {
        final Circle area = roof.getArea();

        if (!area.intersects(target.getAffectedArea())) {
            return;
        }

        Vector2i center = new Vector2i(area.getCenter(), RoundingMode.HALF_UP);
        int radius = TeraMath.floorToInt(area.getRadius());

        HeightMap hmBottom = new HeightMap() {

            @Override
            public int apply(int x, int z) {
                int rx = x - center.getX();
                int rz = z - center.getY();

                // relative distance to border of the roof
                double dist = radius - Math.sqrt(rx * rx + rz * rz);

                int y = TeraMath.floorToInt(roof.getBaseHeight() + dist * roof.getPitch());
                return y;
            }
        };

        Pen pen = Pens.fill(target, hmBottom, HeightMaps.offset(hmBottom, 1), DefaultBlockType.ROOF_HIP);
        RasterUtil.fillCircle(new CheckedPen(pen), center.getX(), center.getY(), radius);
    }

}
