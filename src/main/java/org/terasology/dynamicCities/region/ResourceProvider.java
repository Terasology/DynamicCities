// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region;

import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.ScalableFacetProvider;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.math.TeraMath;

/**
 * This facet will be used to store information about the resources in its region.
 *
 * The actual data will be set through CompatibleRasterizer. It will be saved as a Component in the RegionEntity.
 */
@RegisterPlugin
@Produces(ResourceFacet.class)
public class ResourceProvider implements ScalableFacetProvider {

    private final int gridSize = 32;

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region, float scale) {
        Border3D border = region.getBorderForFacet(RoughnessFacet.class);
        ResourceFacet facet = new ResourceFacet(region.getRegion(), border, TeraMath.ceilToInt(gridSize / scale));

        region.setRegionFacet(ResourceFacet.class, facet);
    }
}
