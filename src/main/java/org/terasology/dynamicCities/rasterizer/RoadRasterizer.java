/*
 * Copyright 2019 MovingBlocks
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;

public class RoadRasterizer {
    private Logger logger = LoggerFactory.getLogger(RoadRasterizer.class);

    public RoadRasterizer() {
    }

    public void raster(RasterTarget rasterTarget, RoadSegment roadSegment, HeightMap heightMap) {
        int upperHeight = 255;  // Height to which the region above the segment would be cleared

        for (BaseVector2i pos : roadSegment.rect.contents()) {
            logger.info("Drawing dirt block at {}...", pos);
            rasterTarget.setBlock(new Vector3i(pos.x(), heightMap.apply(pos), pos.y()), DefaultBlockType.ROAD_FILL);
        }

        // Clean the region above the rect
        Vector2i rectMin = roadSegment.rect.min();
        Region3i upper = Region3i.createFromMinAndSize(
                new Vector3i(rectMin.x(), heightMap.apply(rectMin) + 1, rectMin.y()),
                new Vector3i(roadSegment.rect.sizeX(), upperHeight, roadSegment.rect.sizeY())
        );

        for (Vector3i pos : upper) {
            rasterTarget.setBlock(pos, DefaultBlockType.AIR);
        }
    }
}
