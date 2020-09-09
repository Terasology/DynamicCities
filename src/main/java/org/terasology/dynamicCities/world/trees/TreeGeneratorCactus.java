// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.world.trees;

import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.chunks.CoreChunk;

/**
 * Cactus generator.
 */
public class TreeGeneratorCactus extends AbstractTreeGenerator {

    private BlockUri cactusType;

    @Override
    public void generate(BlockManager blockManager, CoreChunk view, Random rand, int posX, int posY, int posZ,
                         ResourceFacet resourceFacet) {
        for (int y = posY; y < posY + 3; y++) {
            safelySetBlock(view, posX, y, posZ, blockManager.getBlock(cactusType), resourceFacet);
        }
    }

    public TreeGenerator setTrunkType(BlockUri b) {
        cactusType = b;
        return this;
    }

    public BlockUri getCactusType() {
        return cactusType;
    }

}
