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

import com.google.common.math.DoubleMath;
import org.joml.Vector2ic;
import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.model.roof.PentRoof;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.math.TeraMath;
import org.terasology.world.block.BlockAreac;

import java.math.RoundingMode;

/**
 * Converts a {@link PentRoof} into blocks
 */
public class PentRoofRasterizer extends RoofRasterizer<PentRoof> {

    /**
     * @param theme the block theme to use
     */
    public PentRoofRasterizer(BlockTheme theme) {
        super(theme, PentRoof.class);
    }

    @Override
    public void raster(RasterTarget target, PentRoof roof, HeightMap hm) {
        BlockAreac area = roof.getArea();

        if (!area.intersectsBlockArea(target.getAffectedArea())) {
            return;
        }

        final HeightMap hmBottom = new HeightMap() {

            @Override
            public int apply(int x, int z) {
                int rx = x - area.minX();
                int rz = z - area.minY();

                Vector2ic dir = roof.getOrientation().direction();

                if (dir.x() < 0) {
                    rx -= area.getSizeX() - 1;  // maxX
                }

                if (dir.y() < 0) {
                    rz -= area.getSizeY() - 1; // maxY
                }

                int hx = rx * dir.x();
                int hz = rz * dir.y();

                int h = DoubleMath.roundToInt(Math.max(hx, hz) * roof.getPitch(), RoundingMode.HALF_UP);

                return roof.getBaseHeight() + h;
            }
        };

        int thickness = TeraMath.ceilToInt(roof.getPitch());
        HeightMap hmTop = HeightMaps.offset(hmBottom, thickness);
        Pen pen = Pens.fill(target, hmBottom, hmTop, DefaultBlockType.ROOF_HIP);
        RasterUtil.fillRect(pen, area);

        final BlockAreac wallRect = roof.getBaseArea();

        HeightMap hmGableBottom = new HeightMap() {

            @Override
            public int apply(int x, int z) {
                int h0 = roof.getBaseHeight();

                boolean onZ = (x == wallRect.minX() || x == wallRect.maxX());
                boolean zOk = (z >= wallRect.minY() && z <= wallRect.maxY());

                if (onZ && zOk) {
                    return h0;
                }

                boolean onX = (z == wallRect.minY() || z == wallRect.maxY());
                boolean xOk = (x >= wallRect.minX() && x <= wallRect.maxX());

                if (onX && xOk) {
                    return h0;
                }

                return hmBottom.apply(x, z); // return top-height to get a no-op
            }
        };

        pen = Pens.fill(target, hmGableBottom, hmBottom, DefaultBlockType.ROOF_GABLE);
        RasterUtil.fillRect(pen, area);
    }
}
