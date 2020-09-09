// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.rasterizer;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.BlockType;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.dynamicCities.construction.BlockBufferSystem;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.math.Side;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3i;

import java.util.Set;

public class BufferRasterTarget implements RasterTarget {


    private final BlockBufferSystem blockBufferSystem;
    private final BlockTheme blockTheme;
    private final Rect2i affectedArea;
    private final Region3i affectedRegion;

    /**
     * @param blockBufferSystem the chunk to work on
     * @param blockTheme a mapping String type to block
     */
    public BufferRasterTarget(BlockBufferSystem blockBufferSystem, BlockTheme blockTheme, Rect2i area) {
        this.blockTheme = blockTheme;
        this.blockBufferSystem = blockBufferSystem;
        this.affectedArea = area;
        affectedRegion = Region3i.createFromMinMax(new Vector3i(area.minX(), -255, area.minY()),
                new Vector3i(area.maxX(), 255, area.maxY()));
    }

    /**
     * @param x x in world coords
     * @param y y in world coords
     * @param z z in world coords
     * @param type the block type
     */
    public void setBlock(int x, int y, int z, BlockType type) {
        blockBufferSystem.saveBlock(new Vector3i(x, y, z), blockTheme.apply(type));
    }

    @Override
    public void setBlock(int x, int y, int z, BlockType type, Set<Side> side) {
        setBlock(x, y, z, blockTheme.apply(type, side));
    }

    /**
     * @param x x in world coords
     * @param y y in world coords
     * @param z z in world coords
     * @param block the actual block
     */
    public void setBlock(int x, int y, int z, Block block) {
        blockBufferSystem.saveBlock(new Vector3i(x, y, z), block);
    }

    /**
     * @return the XZ area that is drawn by this raster target
     */
    public Rect2i getAffectedArea() {
        return affectedArea;
    }

    /**
     * @return the region that is drawn by this raster target
     */
    public Region3i getAffectedRegion() {
        return affectedRegion;
    }
}
