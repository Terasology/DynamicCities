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

import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.block.BlockRegion;

/**
 * A default implementation of a rasterizer for roads. Creates simple dirt roads.
 */
public class RoadRasterizer {

    public RoadRasterizer() {
    }

    public void raster(RasterTarget rasterTarget, RoadSegment roadSegment, HeightMap heightMap) {
        int upperHeight = 255;  // Height to which the region above the segment would be cleared

        for (BaseVector2i pos : roadSegment.rect.contents()) {
            rasterTarget.setBlock(new Vector3i(pos.x(), heightMap.apply(pos), pos.y()), DefaultBlockType.ROAD_SURFACE);
        }

        // Clean the region above the rect
        Vector2i rectMin = roadSegment.rect.min();
        BlockRegion upper = new BlockRegion(rectMin.x(), heightMap.apply(rectMin) + 1, rectMin.y(),
                roadSegment.rect.sizeX(), upperHeight, roadSegment.rect.sizeY());

        for (Vector3ic pos : upper) {
            rasterTarget.setBlock(pos, DefaultBlockType.AIR);
        }
    }
}
