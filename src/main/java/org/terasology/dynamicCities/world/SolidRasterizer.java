// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.world;

import org.terasology.biomesAPI.Biome;
import org.terasology.coreworlds.CoreBiome;
import org.terasology.coreworlds.generator.facets.BiomeFacet;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.rasterizer.CompatibleRasterizer;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.ChunkConstants;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.facets.DensityFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;

/**
 *
 */
public class SolidRasterizer extends CompatibleRasterizer {


    @Override
    public void generateChunk(CoreChunk chunk, Region chunkRegion) {
        DensityFacet solidityFacet = chunkRegion.getFacet(DensityFacet.class);
        SurfaceHeightFacet surfaceFacet = chunkRegion.getFacet(SurfaceHeightFacet.class);
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
            float density = solidityFacet.get(pos);

            if (density >= 32) {
                setBlock(chunk, stone, pos, resourceFacet);
            } else if (density >= 0) {
                int depth = TeraMath.floorToInt(surfaceFacet.get(pos2d)) - posY;
                Block block = getSurfaceBlock(depth, posY, biome, seaLevel);
                setBlock(chunk, block, pos, resourceFacet);
            } else {
                // fill up terrain up to sealevel height with water or ice
                if (posY == seaLevel && CoreBiome.SNOW == biome) {
                    setBlock(chunk, ice, pos, resourceFacet);
                } else if (posY <= seaLevel) {         // either OCEAN or SNOW
                    setBlock(chunk, water, pos, resourceFacet);
                }
            }
        }
    }

    private Block getSurfaceBlock(int depth, int height, Biome type, int seaLevel) {
        if (type instanceof CoreBiome) {
            switch ((CoreBiome) type) {
                case FOREST:
                case PLAINS:
                case MOUNTAINS:
                    // Beach
                    if (depth == 0 && height > seaLevel && height < seaLevel + 96) {
                        return grass;
                    } else if (depth == 0 && height >= seaLevel + 96) {
                        return snow;
                    } else if (depth > 32) {
                        return stone;
                    } else {
                        return dirt;
                    }
                case SNOW:
                    if (depth == 0 && height > seaLevel) {
                        // Snow on top
                        return snow;
                    } else if (depth > 32) {
                        // Stone
                        return stone;
                    } else {
                        // Dirt
                        return dirt;
                    }
                case DESERT:
                    if (depth > 8) {
                        // Stone
                        return stone;
                    } else {
                        return sand;
                    }
                case OCEAN:
                    if (depth == 0) {
                        return sand;
                    } else {
                        return stone;
                    }
                case BEACH:
                    if (depth < 3) {
                        return sand;
                    } else {
                        return stone;
                    }
            }
        }
        return dirt;
    }
}
