// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.world.testbench;

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facetProviders.BiomeProvider;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;

// TODO: Make BiomeProvider an interface so we don't have to extend the array-backed implementation
@Produces(BiomeFacet.class)
public class ConstantBiomeProvider extends BiomeProvider {
    private final CoreBiome biome;

    public ConstantBiomeProvider(CoreBiome biome) {
        this.biome = biome;
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        BiomeFacet biomeFacet = new ConstantBiomeFacet(region.getRegion(), region.getBorderForFacet(BiomeFacet.class), biome);
        region.setRegionFacet(BiomeFacet.class, biomeFacet);
    }
}
