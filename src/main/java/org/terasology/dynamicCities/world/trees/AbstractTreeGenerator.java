// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.world.trees;

import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.rasterizer.CompatibleRasterizer;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.ChunkConstants;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.math.geom.Vector3i;

/**
 * Object generators are used to generate objects like trees etc.
 */
public abstract class AbstractTreeGenerator extends CompatibleRasterizer implements TreeGenerator {

    protected void safelySetBlock(CoreChunk chunk, int x, int y, int z, Block block, ResourceFacet resourceFacet) {
        if (ChunkConstants.CHUNK_REGION.encompasses(x, y, z)) {
            setBlock(chunk, block, new Vector3i(x, y, z), resourceFacet);
        }
    }
}
