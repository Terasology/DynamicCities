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

import org.terasology.ShatteredPlanes.ShatteredPlanesBiome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.dynamicCities.region.RegionEntityProvider;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.biomes.Biome;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.liquid.LiquidData;
import org.terasology.world.liquid.LiquidType;

/**
 */
public abstract class CompatibleRasterizer implements WorldRasterizer {

    private Block water;
    private Block ice;
    private Block stone;
    private Block sand;
    private Block grass;
    private Block snow;
    private Block dirt;
    private Block mantlestone;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("core:stone");
        mantlestone = blockManager.getBlock("core:mantlestone");
        water = blockManager.getBlock("core:water");
        ice = blockManager.getBlock("core:Ice");
        sand = blockManager.getBlock("core:Sand");
        grass = blockManager.getBlock("core:Grass");
        snow = blockManager.getBlock("core:Snow");
        dirt = blockManager.getBlock("core:Dirt");
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
    }

    public void setBlock(CoreChunk chunk, Block block, BaseVector3i pos) {
        chunk.setBlock(pos, block);

    }

}
