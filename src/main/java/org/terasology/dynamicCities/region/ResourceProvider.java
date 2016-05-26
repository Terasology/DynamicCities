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
package org.terasology.dynamicCities.region;

import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Produces(ResourceFacet.class)
/**
 * This facet will be used to store information about the resources in its region
 * The actual data will be set through CompatibleRasterizer
 * It will be saved as a Component in the RegionEntity
 */
public class ResourceProvider implements FacetProvider {

    private final int gridSize = 4;


    @Override
    public void setSeed(long seed) { }

    @Override
    public void process(GeneratingRegion region) {

        Border3D border = region.getBorderForFacet(RoughnessFacet.class);
        ResourceFacet facet = new ResourceFacet(region.getRegion(), border, gridSize);

        region.setRegionFacet(ResourceFacet.class, facet);
    }


}
