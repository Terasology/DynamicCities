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

import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.resource.Resource;
import org.terasology.dynamicCities.resource.ResourceType;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.CoreRegistry;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.WorldRasterizer;

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
        stone = blockManager.getBlock("core:stone");
        hardStone = blockManager.getBlock("core:hardstone");
        cobbleStone = blockManager.getBlock("core:cobblestone");
        mantlestone = blockManager.getBlock("core:mantlestone");
        water = blockManager.getBlock("core:water");
        ice = blockManager.getBlock("core:Ice");
        sand = blockManager.getBlock("core:Sand");
        grass = blockManager.getBlock("core:Grass");
        snow = blockManager.getBlock("core:Snow");
        dirt = blockManager.getBlock("core:Dirt");
        oakTrunk = blockManager.getBlock("core:OakTrunk");
        pineTrunk = blockManager.getBlock("core:PineTrunk");
        birchTrunk = blockManager.getBlock("core:BirchTrunk");
        biomeRegistry = CoreRegistry.get(BiomeRegistry.class);
    }

    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
    }

    public void setBlock(CoreChunk chunk, Block block, BaseVector3i pos, ResourceFacet resourceFacet) {
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
