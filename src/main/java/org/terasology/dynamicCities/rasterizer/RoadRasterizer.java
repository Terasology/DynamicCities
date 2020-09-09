// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.rasterizer;

import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;

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
        Region3i upper = Region3i.createFromMinAndSize(
                new Vector3i(rectMin.x(), heightMap.apply(rectMin) + 1, rectMin.y()),
                new Vector3i(roadSegment.rect.sizeX(), upperHeight, roadSegment.rect.sizeY())
        );

        for (Vector3i pos : upper) {
            rasterTarget.setBlock(pos, DefaultBlockType.AIR);
        }
    }
}
