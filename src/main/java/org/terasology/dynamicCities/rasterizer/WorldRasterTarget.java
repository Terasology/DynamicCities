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

import org.terasology.cities.BlockTheme;
import org.terasology.cities.BlockType;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.Set;

public class WorldRasterTarget implements RasterTarget {


    private final WorldProvider worldProvider;
    private final BlockTheme blockTheme;
    private final Rect2i affectedArea;
    private final Region3i affectedRegion;

    /**
     * @param worldProvider the chunk to work on
     * @param blockTheme a mapping String type to block
     */
    public WorldRasterTarget(WorldProvider worldProvider, BlockTheme blockTheme, Rect2i area) {
        this.blockTheme = blockTheme;
        this.worldProvider = worldProvider;
        this.affectedArea = area;
        affectedRegion = Region3i.createFromMinMax(new Vector3i(area.minX(), -255, area.minY()), new Vector3i(area.maxX(), 255, area.maxY()));
    }
    /**
     * @param x x in world coords
     * @param y y in world coords
     * @param z z in world coords
     * @param type the block type
     */
    public void setBlock(int x, int y, int z, BlockType type) {
        worldProvider.setBlock(new Vector3i(x, y, z), blockTheme.apply(type));
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
        worldProvider.setBlock(new Vector3i(x, y, z), block);
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
