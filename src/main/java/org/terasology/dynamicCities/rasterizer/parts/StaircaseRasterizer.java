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

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.StaircaseBuildingPart;
import org.terasology.cities.common.Edges;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.Orientation;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.commonworld.geom.OutlineIterator;
import org.terasology.math.Side;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

import java.util.Collections;
import java.util.Iterator;

/**
 * Converts a {@link StaircaseBuildingPart} into blocks
 */
public class StaircaseRasterizer extends AbsDynBuildingRasterizer<StaircaseBuildingPart> {

    public StaircaseRasterizer(BlockTheme theme, WorldProvider worldProvider) {
        super(theme, StaircaseBuildingPart.class, worldProvider);
    }

    @Override
    protected void raster(RasterTarget target, StaircaseBuildingPart part, HeightMap heightMap) {
        BlockAreac rc = part.getShape();

        if (!rc.intersectsBlockArea(target.getAffectedArea())) {
            return;
        }

        if (part.getTopHeight() < target.getMinHeight()) {
            return;
        }

        boolean clockwise = false;
        int y = part.getBaseHeight();
        BlockArea stairsRect = rc.expand(-1, -1, new BlockArea(BlockArea.INVALID));
        Orientation o = part.getOrientation();
        Vector2i entry = Edges.getCorner(stairsRect, o.getRotated(-90));

        Iterator<Vector2ic> it = new OutlineIterator(stairsRect, clockwise, entry).iterator();
        while (y < part.getTopHeight() && y <= target.getMaxHeight()) {
            Vector2ic v = it.next();

            boolean isCorner = isCorner(stairsRect, v);

            // parts of the staircase could be outside
            if (target.getAffectedArea().contains(v.x(), v.y())) {
                if (isCorner && y - 1 >= target.getMinHeight()) {
                    target.setBlock(v.x(), y - 1, v.y(), DefaultBlockType.TOWER_STAIRS);
                } else if (y >= target.getMinHeight()) {
                    Side side = findSide(stairsRect, v, clockwise).yawClockwise(1);
                    target.setBlock(v.x(), y, v.y(), DefaultBlockType.TOWER_STAIRS, Collections.singleton(side));
                }
            }

            if (!isCorner) {
                y++;
            }
        }
    }

    private static boolean isCorner(BlockAreac rect, Vector2ic v) {
        int x = v.x();
        int y = v.y();

        return (x == rect.maxX() || x == rect.minX()) && (y == rect.minY() || y == rect.maxY());
    }

    private static Side findSide(BlockAreac rect, Vector2ic v, boolean clockwise) {
        Side side = findSide(rect, v);
        return clockwise ? side : side.reverse();
    }

    private static Side findSide(BlockAreac rect, Vector2ic v) {
        // this method ignores corners
        if (v.x() == rect.maxX()) {
            return Side.RIGHT;
        }
        if (v.x() == rect.minX()) {
            return Side.LEFT;
        }
        if (v.y() == rect.minY()) {
            return Side.FRONT;
        }
        if (v.y() == rect.maxY()) {
            return Side.BACK;
        }
        throw new IllegalArgumentException("Not on the outline");
    }
}

