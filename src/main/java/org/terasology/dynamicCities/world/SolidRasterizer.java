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

import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.rasterizer.CompatibleRasterizer;
import org.terasology.math.JomlUtil;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.world.block.Block;
import org.terasology.world.chunks.ChunkConstants;
import org.terasology.world.chunks.CoreChunk;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfacesFacet;

/**
 */
public class SolidRasterizer extends CompatibleRasterizer {


    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        DensityFacet solidityFacet = chunkRegion.getFacet(DensityFacet.class);
        SurfacesFacet surfaceFacet = chunkRegion.getFacet(SurfacesFacet.class);
        BiomeFacet biomeFacet = chunkRegion.getFacet(BiomeFacet.class);
        ResourceFacet resourceFacet = chunkRegion.getFacet(ResourceFacet.class);
        SeaLevelFacet seaLevelFacet = chunkRegion.getFacet(SeaLevelFacet.class);
        int seaLevel = seaLevelFacet.getSeaLevel();

        Vector2i pos2d = new Vector2i();
        for (Vector3i pos : ChunkConstants.CHUNK_REGION) {
            pos2d.set(pos.x, pos.z);
            Biome biome = biomeFacet.get(pos2d);
            biomeRegistry.setBiome(biome, chunk, pos.x, pos.y, pos.z);

            int posY = pos.y + chunk.getChunkWorldOffsetY();
            float density = solidityFacet.get(JomlUtil.from(pos));

            if (surfaceFacet.get(JomlUtil.from(pos))) {
                setBlock(chunk, getSurfaceBlock(biome, posY - seaLevel), pos, resourceFacet);
            } else if (density > 0) {
                setBlock(chunk, getBelowSurfaceBlock(density, biome), pos, resourceFacet);
            } else if (posY == seaLevel && CoreBiome.SNOW == biome) {
                setBlock(chunk, ice, pos, resourceFacet);
            } else if (posY <= seaLevel) {         // either OCEAN or SNOW
                setBlock(chunk, water, pos, resourceFacet);
            }
        }
    }

    private Block getSurfaceBlock(Biome type, int heightAboveSea) {
        if (type instanceof CoreBiome) {
            switch ((CoreBiome) type) {
                case FOREST:
                case PLAINS:
                case MOUNTAINS:
                    if (heightAboveSea > 96) {
                        return snow;
                    } else if (heightAboveSea > 0) {
                        return grass;
                    } else {
                        return dirt;
                    }
                case SNOW:
                    if (heightAboveSea > 0) {
                        return snow;
                    } else {
                        return dirt;
                    }
                case DESERT:
                case OCEAN:
                case BEACH:
                    return sand;
            }
        }
        return dirt;
    }

    private Block getBelowSurfaceBlock(float density, Biome type) {
        if (type instanceof CoreBiome) {
            switch ((CoreBiome) type) {
                case DESERT:
                    if (density > 8) {
                        return stone;
                    } else {
                        return sand;
                    }
                case BEACH:
                    if (density > 2) {
                        return stone;
                    } else {
                        return sand;
                    }
                case OCEAN:
                    return stone;
            }
        }
        if (density > 32) {
            return stone;
        } else {
            return dirt;
        }
    }
}
