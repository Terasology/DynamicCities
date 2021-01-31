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

import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.model.roof.ConicRoof;
import org.terasology.cities.raster.CheckedPen;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.commonworld.geom.CircleUtility;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.joml.geom.Circlef;
import org.terasology.joml.geom.Rectanglef;
import org.terasology.math.TeraMath;


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
        final Circlef area = roof.getArea();

        if (!CircleUtility.intersect(area, target.getAffectedArea().getBounds(new Rectanglef()))) {
            return;
        }

        Vector2i center = new Vector2i(area.x, area.y, RoundingMode.HALF_UP);
        int radius = TeraMath.floorToInt(area.r);

        HeightMap hmBottom = new HeightMap() {

            @Override
            public int apply(int x, int z) {
                int rx = x - center.x();
                int rz = z - center.y();

                // relative distance to border of the roof
                double dist = radius - Math.sqrt(rx * rx + rz * rz);

                int y = TeraMath.floorToInt(roof.getBaseHeight() + dist * roof.getPitch());
                return y;
            }
        };

        Pen pen = Pens.fill(target, hmBottom, HeightMaps.offset(hmBottom, 1), DefaultBlockType.ROOF_HIP);
        RasterUtil.fillCircle(new CheckedPen(pen), center.x(), center.y(), radius);
    }
}
