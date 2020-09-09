// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.rasterizer.parts;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.RectBuildingPart;
import org.terasology.cities.raster.BuildingPens;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.engine.world.WorldProvider;
import org.terasology.math.geom.Rect2i;

/**
 * Converts a {@link RectBuildingPart} into blocks
 */
public class RectPartRasterizer extends AbsDynBuildingRasterizer<RectBuildingPart> {

    public RectPartRasterizer(BlockTheme theme, WorldProvider worldProvider) {
        super(theme, RectBuildingPart.class, worldProvider);
    }

    @Override
    protected void raster(RasterTarget brush, RectBuildingPart part, HeightMap heightMap) {
        Rect2i rc = part.getShape();

        if (!rc.overlaps(brush.getAffectedArea())) {
            return;
        }

//        int topHeight = part.getBaseHeight() + part.getWallHeight() + part.getRoof().getHeight;
//        Region3i bbox = Region3i(rc.minX(), part.getBaseHeight(), rc.minY(), rc.maxX(), topHeight, rc.maxY());

//        if (chunk.getRegion().overlaps(bbox)) {

        int baseHeight = part.getBaseHeight();
        int wallHeight = part.getWallHeight();

        Pen floorPen = BuildingPens.floorPen(brush, heightMap, baseHeight, DefaultBlockType.BUILDING_FLOOR);
        RasterUtil.fillRect(floorPen, rc);

        // create walls
        Pen wallPen = Pens.fill(brush, baseHeight, baseHeight + wallHeight, DefaultBlockType.BUILDING_WALL);
        RasterUtil.drawRect(wallPen, rc);
    }
}

