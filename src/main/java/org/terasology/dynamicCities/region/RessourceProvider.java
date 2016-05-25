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
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.*;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

@RegisterPlugin
@Produces(ResourceFacet.class)
/**
 * This facet will be used to store information about the ressources in its region
 * It will be saved as a Component in the RegionEntity
 */
public class RessourceProvider implements FacetProvider {

    private final int gridSize = 4;


    @Override
    public void setSeed(long seed) { }

    @Override
    public void process(GeneratingRegion region) {

        Border3D border = region.getBorderForFacet(RoughnessFacet.class);
        Region3i roughnessRegion = region.getRegion().expand(-region.getRegion().sizeX() + Math.round(region.getRegion().sizeX() / gridSize));
        RoughnessFacet facet = new RoughnessFacet(roughnessRegion, border, gridSize);

        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        Rect2i processRegion = facet.getWorldRegion();

        for (BaseVector2i pos : processRegion.contents()) {
            facet.calcRoughness(new Vector2i(pos.x(), pos.y()), surfaceHeightFacet);
        }

        region.setRegionFacet(RoughnessFacet.class, facet);
    }


}
