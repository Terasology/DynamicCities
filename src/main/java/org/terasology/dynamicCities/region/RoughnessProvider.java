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

import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.math.Region3i;
import org.terasology.math.geom.*;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Produces(RoughnessFacet.class)
@Requires(@Facet(SurfaceHeightFacet.class))
/**
 * This facet will be used to store information about the height variations in grid cells with the size a
 * It will be saved as a Component in the RegionEntity
 */
public class RoughnessProvider implements FacetProvider {

    private final int gridSize = 4;


    @Override
    public void setSeed(long seed) { }

    @Override
    public void process(GeneratingRegion region) {

        Border3D border = region.getBorderForFacet(RoughnessFacet.class);
        RoughnessFacet facet = new RoughnessFacet(region.getRegion(), border, gridSize);

        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        Rect2i processRegion = facet.getGridWorldRegion();

        for (BaseVector2i pos : processRegion.contents()) {
            facet.calcRoughness(new Vector2i(pos.x(), pos.y()), surfaceHeightFacet);
            //facet.setWorld(new Vector2i(pos.x(), pos.y()), 2);
        }

        region.setRegionFacet(RoughnessFacet.class, facet);
    }


}
