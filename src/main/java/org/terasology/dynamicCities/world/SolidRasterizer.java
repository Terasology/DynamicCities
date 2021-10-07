// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.world;

import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.rasterizer.CompatibleRasterizer;
import org.terasology.engine.world.chunks.Chunk;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.engine.world.generation.Region;
import org.terasology.engine.world.generation.ScalableWorldRasterizer;
import org.terasology.engine.world.generation.facets.DensityFacet;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfacesFacet;

import static com.google.common.base.Preconditions.checkNotNull;

public class SolidRasterizer extends CompatibleRasterizer implements ScalableWorldRasterizer {

    /**
     * This override is required since the one in {@link CompatibleRasterizer} overrides the one in {@link ScalableWorldRasterizer}.
     */
    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion) {
        generateChunk(chunk, chunkRegion, 1);
    }

    @Override
    public void generateChunk(Chunk chunk, Region chunkRegion, float scale) {
        DensityFacet solidityFacet = chunkRegion.getFacet(DensityFacet.class);
        SurfacesFacet surfaceFacet = chunkRegion.getFacet(SurfacesFacet.class);
        ResourceFacet resourceFacet = chunkRegion.getFacet(ResourceFacet.class);
        SeaLevelFacet seaLevelFacet = chunkRegion.getFacet(SeaLevelFacet.class);

        // FIXME: How should this handle a lack of sea level?
        //   Or should there be some facet dependency declared to make sure
        //   there always is a sea level facet?
        int seaLevel = (seaLevelFacet != null)
                ? seaLevelFacet.getSeaLevel()
                : Integer.MIN_VALUE;

        // FIXME: BiomeFacet does depend on SeaLevelFacet, but that doesn't help if
        //    there is no BiomeFacet!
        BiomeFacet biomeFacet = checkNotNull(chunkRegion.getFacet(BiomeFacet.class),
                "world must have a biome facet");

        Vector2i pos2d = new Vector2i();
        Vector3i worldPos = new Vector3i();
        for (Vector3ic pos : Chunks.CHUNK_REGION) {
            pos2d.set(pos.x(), pos.z());
            Biome biome = biomeFacet.get(pos2d);
            biomeRegistry.setBiome(biome, chunk, pos.x(), pos.y(), pos.z());

            float posY = (pos.y() + chunk.getChunkWorldOffsetY()) * scale;

            // FIXME: require DensityFacet
            float density = (solidityFacet != null)
                    ? solidityFacet.get(pos)
                    : 1f;
            chunk.chunkToWorldPosition(pos,  worldPos);

            if (surfaceFacet.get(pos)) {
                setBlock(chunk, biome.getSurfaceBlock(worldPos, seaLevel), pos, resourceFacet);
            } else if (density > 0) {
                setBlock(chunk, biome.getBelowSurfaceBlock(worldPos, density), pos, resourceFacet);
            } else if (posY == seaLevel && CoreBiome.SNOW == biome) {
                setBlock(chunk, ice, pos, resourceFacet);
            } else if (posY <= seaLevel) {         // either OCEAN or SNOW
                setBlock(chunk, water, pos, resourceFacet);
            }
        }
    }
}
