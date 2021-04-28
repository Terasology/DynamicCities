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

package org.terasology.dynamicCities.world.trees;

import org.joml.Vector3i;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.rasterizer.CompatibleRasterizer;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;

/**
 * Object generators are used to generate objects like trees etc.
 *
 */
public abstract class AbstractTreeGenerator extends CompatibleRasterizer implements TreeGenerator {

    protected void safelySetBlock(Chunk chunk, int x, int y, int z, Block block, ResourceFacet resourceFacet) {
        if (Chunks.CHUNK_REGION.contains(x, y, z)) {
            setBlock(chunk, block, new Vector3i(x, y, z), resourceFacet);
        }
    }
}
