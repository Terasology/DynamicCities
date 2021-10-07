// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.world.testbench;

import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;

public class ConstantBiomeFacet extends BiomeFacet {
    private final CoreBiome biome;

    public ConstantBiomeFacet(BlockRegion region, Border3D border, CoreBiome biome) {
        super(region, border);
        this.biome = biome;
    }

    @Override
    public Biome get(int x, int y) {
        return biome;
    }

    @Override
    public Biome getWorld(int x, int y) {
        return biome;
    }
}
