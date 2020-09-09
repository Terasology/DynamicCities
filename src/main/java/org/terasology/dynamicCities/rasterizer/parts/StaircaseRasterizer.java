// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.rasterizer.parts;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.StaircaseBuildingPart;
import org.terasology.cities.common.Edges;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.commonworld.Orientation;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.WorldProvider;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.RectIterable;
import org.terasology.math.geom.Vector2i;

import java.util.Collections;
import java.util.Iterator;

/**
 * Converts a {@link StaircaseBuildingPart} into blocks
 */
public class StaircaseRasterizer extends AbsDynBuildingRasterizer<StaircaseBuildingPart> {

    public StaircaseRasterizer(BlockTheme theme, WorldProvider worldProvider) {
        super(theme, StaircaseBuildingPart.class, worldProvider);
    }

    private static boolean isCorner(Rect2i rect, BaseVector2i v) {
        int x = v.getX();
        int y = v.getY();

        return (x == rect.maxX() || x == rect.minX()) && (y == rect.minY() || y == rect.maxY());
    }

    private static Side findSide(Rect2i rect, BaseVector2i v, boolean clockwise) {
        Side side = findSide(rect, v);
        return clockwise ? side : side.reverse();
    }

    private static Side findSide(Rect2i rect, BaseVector2i v) {
        // this method ignores corners
        if (v.getX() == rect.maxX()) {
            return Side.RIGHT;
        }
        if (v.getX() == rect.minX()) {
            return Side.LEFT;
        }
        if (v.getY() == rect.minY()) {
            return Side.FRONT;
        }
        if (v.getY() == rect.maxY()) {
            return Side.BACK;
        }
        throw new IllegalArgumentException("Not on the outline");
    }

    @Override
    protected void raster(RasterTarget target, StaircaseBuildingPart part, HeightMap heightMap) {
        Rect2i rc = part.getShape();

        if (!rc.overlaps(target.getAffectedArea())) {
            return;
        }

        if (part.getTopHeight() < target.getMinHeight()) {
            return;
        }

        boolean clockwise = false;
        int y = part.getBaseHeight();
        Rect2i stairsRect = rc.expand(-1, -1);
        Orientation o = part.getOrientation();
        Vector2i entry = Edges.getCorner(stairsRect, o.getRotated(-90));
        Iterator<BaseVector2i> it = new RectIterable(stairsRect, clockwise, entry).iterator();
        while (y < part.getTopHeight() && y <= target.getMaxHeight()) {
            BaseVector2i v = it.next();

            boolean isCorner = isCorner(stairsRect, v);

            // parts of the staircase could be outside
            if (target.getAffectedArea().contains(v.getX(), v.getY())) {
                if (isCorner && y - 1 >= target.getMinHeight()) {
                    target.setBlock(v.getX(), y - 1, v.getY(), DefaultBlockType.TOWER_STAIRS);
                } else if (y >= target.getMinHeight()) {
                    Side side = findSide(stairsRect, v, clockwise).yawClockwise(1);
                    target.setBlock(v.getX(), y, v.getY(), DefaultBlockType.TOWER_STAIRS, Collections.singleton(side));
                }
            }

            if (!isCorner) {
                y++;
            }
        }
    }
}

