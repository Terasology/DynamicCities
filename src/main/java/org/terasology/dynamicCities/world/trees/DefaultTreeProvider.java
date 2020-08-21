// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.world.trees;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import org.terasology.biomesAPI.Biome;
import org.terasology.core.world.CoreBiome;
import org.terasology.core.world.generator.facetProviders.PositionFilters;
import org.terasology.core.world.generator.facetProviders.SurfaceObjectProvider;
import org.terasology.core.world.generator.facets.BiomeFacet;
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.Vector3i;
import org.terasology.nui.properties.Range;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import java.util.List;

/**
 * Determines where trees can be placed.  Will put trees one block above the surface.
 */
@Produces(TreeFacet.class)
@Requires({
        @Facet(value = SeaLevelFacet.class, border = @FacetBorder(sides = 13)),
        @Facet(value = SurfaceHeightFacet.class, border = @FacetBorder(sides = 13 + 1)),
        @Facet(value = BiomeFacet.class, border = @FacetBorder(sides = 13))
})
public class DefaultTreeProvider extends SurfaceObjectProvider<Biome, TreeGenerator> implements ConfigurableFacetProvider {

    private Noise densityNoiseGen;
    private Configuration configuration = new Configuration();

    public DefaultTreeProvider() {
        register(CoreBiome.MOUNTAINS, Trees.oakTree(), 0.04f);
        register(CoreBiome.MOUNTAINS, Trees.pineTree(), 0.02f);

        register(CoreBiome.FOREST, Trees.oakTree(), 0.25f);
        register(CoreBiome.FOREST, Trees.pineTree(), 0.10f);
        register(CoreBiome.FOREST, Trees.birchTree(), 0.10f);
        register(CoreBiome.FOREST, Trees.oakVariationTree(), 0.25f);

        register(CoreBiome.SNOW, Trees.birchTree(), 0.02f);
        register(CoreBiome.SNOW, Trees.pineTree(), 0.10f);

        register(CoreBiome.PLAINS, Trees.redTree(), 0.01f);
        register(CoreBiome.PLAINS, Trees.birchTree(), 0.01f);
        register(CoreBiome.PLAINS, Trees.oakTree(), 0.02f);

        register(CoreBiome.DESERT, Trees.cactus(), 0.04f);
    }

    /**
     * @param configuration the default configuration to use
     */
    public DefaultTreeProvider(Configuration configuration) {
        this();
        this.configuration = configuration;
    }

    @Override
    public void setSeed(long seed) {
        super.setSeed(seed);

        densityNoiseGen = new WhiteNoise(seed);
    }

    @Override
    public void process(GeneratingRegion region) {
        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        BiomeFacet biome = region.getRegionFacet(BiomeFacet.class);

        List<Predicate<Vector3i>> filters = getFilters(region);

        // these value are derived from the maximum tree extents as
        // computed by the TreeTests class. Birch is the highest with 32
        // and Pine has 13 radius.
        // These values must be identical in the class annotations.
        int maxRad = 13;
        int maxHeight = 32;
        Border3D borderForTreeFacet = region.getBorderForFacet(TreeFacet.class);
        TreeFacet facet = new TreeFacet(region.getRegion(), borderForTreeFacet.extendBy(0, maxHeight, maxRad));

        populateFacet(facet, surface, biome, filters);

        region.setRegionFacet(TreeFacet.class, facet);
    }

    protected List<Predicate<Vector3i>> getFilters(GeneratingRegion region) {
        List<Predicate<Vector3i>> filters = Lists.newArrayList();

        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);
        filters.add(PositionFilters.minHeight(seaLevel.getSeaLevel()));

        filters.add(PositionFilters.probability(densityNoiseGen, configuration.density * 0.05f));

        SurfaceHeightFacet surface = region.getRegionFacet(SurfaceHeightFacet.class);
        filters.add(PositionFilters.flatness(surface, 1, 0));

        return filters;
    }

    @Override
    public String getConfigurationName() {
        return "Trees";
    }

    @Override
    public Component getConfiguration() {
        return configuration;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.configuration = (Configuration) configuration;
    }

    public static class Configuration implements Component {
        @Range(min = 0, max = 1.0f, increment = 0.05f, precision = 2, description = "Define the overall tree density")
        public float density = 0.2f;
    }
}
