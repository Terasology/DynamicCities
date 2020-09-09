// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region;

import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Produces(ResourceFacet.class)
/**
 * This facet will be used to store information about the resources in its region
 * The actual data will be set through CompatibleRasterizer
 * It will be saved as a Component in the RegionEntity
 */
public class ResourceProvider implements FacetProvider {

    private final int gridSize = 32;


    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {

        Border3D border = region.getBorderForFacet(RoughnessFacet.class);
        ResourceFacet facet = new ResourceFacet(region.getRegion(), border, gridSize);

        region.setRegionFacet(ResourceFacet.class, facet);
    }


}
