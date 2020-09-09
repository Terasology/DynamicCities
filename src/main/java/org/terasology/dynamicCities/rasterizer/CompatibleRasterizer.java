// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.rasterizer;

import org.terasology.biomesAPI.BiomeRegistry;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.resource.Resource;
import org.terasology.dynamicCities.resource.ResourceType;
import org.terasology.engine.registry.CoreRegistry;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.WorldRasterizer;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Vector2i;

/**
 *
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
