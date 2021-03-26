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

import org.joml.Vector2i;
import org.joml.Vector3ic;
import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.resource.Resource;
import org.terasology.dynamicCities.resource.ResourceType;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;

/**
 */
public abstract class CompatibleRasterizer implements WorldRasterizer {

    protected Block water;
    protected Block ice;
    protected Block stone;
    protected Block cobbleStone;
    protected Block hardStone;
    protected Block sand;
    protected Block grass;
    protected Block snow;
    protected Block dirt;
    protected Block mantlestone;
    protected Block oakTrunk;
    protected Block pineTrunk;
    protected Block birchTrunk;
    protected BiomeRegistry biomeRegistry;

    @Override
    public void initialize() {
        BlockManager blockManager = CoreRegistry.get(BlockManager.class);
        stone = blockManager.getBlock("CoreAssets:stone");
        hardStone = blockManager.getBlock("CoreAssets:hardstone");
        cobbleStone = blockManager.getBlock("CoreAssets:cobblestone");
        mantlestone = blockManager.getBlock("CoreAssets:mantlestone");
        water = blockManager.getBlock("CoreAssets:water");
        ice = blockManager.getBlock("CoreAssets:Ice");
        sand = blockManager.getBlock("CoreAssets:Sand");
        grass = blockManager.getBlock("CoreAssets:Grass");
        snow = blockManager.getBlock("CoreAssets:Snow");
        dirt = blockManager.getBlock("CoreAssets:Dirt");
        oakTrunk = blockManager.getBlock("CoreAssets:OakTrunk");
        pineTrunk = blockManager.getBlock("CoreAssets:PineTrunk");
        birchTrunk = blockManager.getBlock("CoreAssets:BirchTrunk");
        biomeRegistry = CoreRegistry.get(BiomeRegistry.class);
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
    }

    public void setBlock(Chunk chunk, Block block, Vector3ic pos, ResourceFacet resourceFacet) {
        chunk.setBlock(pos, block);
        Resource resource = getResourceType(block);
        if (resource.getType() != ResourceType.NULL) {
            resourceFacet.addResource(resource, new Vector2i(pos.x(), pos.y()));
        }
    }

    public Resource getResourceType(Block block) {
        if (block == stone || block == mantlestone || block == hardStone) {
            return new Resource(ResourceType.STONE);
        }
        if (block == water) {
            return new Resource(ResourceType.WATER);
        }
        if (block == grass) {
            return new Resource(ResourceType.GRASS);
        }
        if (block == oakTrunk || block == pineTrunk || block == birchTrunk) {
            return new Resource(ResourceType.WOOD);
        }

        return new Resource(ResourceType.NULL);
    }
}
