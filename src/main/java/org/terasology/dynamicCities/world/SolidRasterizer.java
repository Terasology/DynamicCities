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
package org.terasology.dynamicCities.world;

import org.joml.Vector2i;
import org.joml.Vector3ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.rasterizer.CompatibleRasterizer;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.facets.DensityFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;

/**
 */
public class SolidRasterizer extends CompatibleRasterizer {


    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        DensityFacet solidityFacet = chunkRegion.getFacet(DensityFacet.class);
        SurfacesFacet surfaceFacet = chunkRegion.getFacet(SurfacesFacet.class);
        BiomeFacet biomeFacet = chunkRegion.getFacet(BiomeFacet.class);
        ResourceFacet resourceFacet = chunkRegion.getFacet(ResourceFacet.class);
        SeaLevelFacet seaLevelFacet = chunkRegion.getFacet(SeaLevelFacet.class);
        int seaLevel = seaLevelFacet.getSeaLevel();

        Vector2i pos2d = new Vector2i();
        for (Vector3ic pos : Chunks.CHUNK_REGION) {
            pos2d.set(pos.x(), pos.z());
            Biome biome = biomeFacet.get(pos2d);
            biomeRegistry.setBiome(biome, chunk, pos.x(), pos.y(), pos.z());

            int posY = pos.y() + chunk.getChunkWorldOffsetY();
            float density = solidityFacet.get(pos);

            if (surfaceFacet.get(pos)) {
                setBlock(chunk, biome.getSurfaceBlock(pos, seaLevel), pos, resourceFacet);
            } else if (density > 0) {
                setBlock(chunk, biome.getBelowSurfaceBlock(pos, density), pos, resourceFacet);
            } else if (posY == seaLevel && CoreBiome.SNOW == biome) {
                setBlock(chunk, ice, pos, resourceFacet);
            } else if (posY <= seaLevel) {         // either OCEAN or SNOW
                setBlock(chunk, water, pos, resourceFacet);
            }
        }
    }
}
