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
package org.terasology.dynamicCities.rasterizer;

import org.joml.Vector3i;
import org.terasology.cities.BlockTheme;
import org.terasology.cities.BlockType;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.dynamicCities.construction.BlockBufferSystem;
import org.terasology.math.Side;
import org.terasology.math.geom.Rect2i;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;
import org.terasology.world.block.BlockRegion;

import java.util.Set;

public class BufferRasterTarget implements RasterTarget {


    private final BlockBufferSystem blockBufferSystem;
    private final BlockTheme blockTheme;
    private final BlockAreac affectedArea;
    private final BlockRegion affectedRegion;

    /**
     * @param blockBufferSystem the chunk to work on
     * @param blockTheme a mapping String type to block
     */
    public BufferRasterTarget(BlockBufferSystem blockBufferSystem, BlockTheme blockTheme, BlockAreac area) {
        this.blockTheme = blockTheme;
        this.blockBufferSystem = blockBufferSystem;
        this.affectedArea = new BlockArea(area);
        affectedRegion = new BlockRegion(area.minX(), -255, area.minY(), area.maxX(), 255, area.maxY());
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
     * @param x     x in world coords
     * @param y     y in world coords
     * @param z     z in world coords
     * @param block the actual block
     */
    public void setBlock(int x, int y, int z, Block block) {
        blockBufferSystem.saveBlock(new Vector3i(x, y, z), block);
    }

    /**
     * @return the XZ area that is drawn by this raster target
     */
    public BlockAreac getAffectedArea() {
        return affectedArea;
    }

    /**
     * @return the region that is drawn by this raster target
     */
    public BlockRegion getAffectedRegion() {
        return affectedRegion;
    }
}
