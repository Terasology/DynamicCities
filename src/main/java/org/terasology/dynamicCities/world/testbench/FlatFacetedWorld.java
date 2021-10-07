// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.world.testbench;

import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facetProviders.SeaLevelProvider;
import org.terasology.dynamicCities.region.RegionEntityProvider;
import org.terasology.dynamicCities.region.ResourceProvider;
import org.terasology.dynamicCities.region.RoughnessProvider;
import org.terasology.dynamicCities.settlements.SettlementFacetProvider;
import org.terasology.dynamicCities.sites.SiteFacetProvider;
import org.terasology.dynamicCities.world.SolidRasterizer;
import org.terasology.engine.core.SimpleUri;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.generation.BaseFacetedWorldGenerator;
import org.terasology.engine.world.generation.WorldBuilder;
import org.terasology.engine.world.generator.RegisterWorldGenerator;
import org.terasology.engine.world.generator.plugin.WorldGeneratorPluginLibrary;
import org.terasology.moduletestingenvironment.fixtures.FlatSurfaceHeightProvider;

@RegisterWorldGenerator(id = "FlatFaceted", displayName = "Faceted world with one uniform flat surface.")
public class FlatFacetedWorld extends BaseFacetedWorldGenerator {
    public static final int SURFACE_HEIGHT = 40;
    public static final int SEA_LEVEL = 15;
    public static final CoreBiome BIOME = CoreBiome.PLAINS;

    @In
    private WorldGeneratorPluginLibrary worldGeneratorPluginLibrary;

    public FlatFacetedWorld(SimpleUri uri) {
        super(uri);
    }

    @Override
    protected WorldBuilder createWorld() {
        return new WorldBuilder(worldGeneratorPluginLibrary)
                .addProvider(new FlatSurfaceHeightProvider(SURFACE_HEIGHT))
                .addProvider(new SeaLevelProvider(SEA_LEVEL))
                .addProvider(new ConstantBiomeProvider(BIOME))
                .addProvider(new RoughnessProvider())
                .addProvider(new SiteFacetProvider())
                .addProvider(new SettlementFacetProvider())
                .addProvider(new ResourceProvider())
                .addEntities(new RegionEntityProvider())
                .addRasterizer(new SolidRasterizer())
                .addPlugins();
    }
}
