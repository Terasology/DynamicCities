// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region;

import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generation.facets.SeaLevelFacet;
import org.terasology.engine.world.generation.facets.SurfaceHeightFacet;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;

@RegisterPlugin
@Produces(RoughnessFacet.class)
@Requires({@Facet(value = SurfaceHeightFacet.class),
        @Facet(value = SeaLevelFacet.class)})
/**
 * This facet will be used to store information about the height variations in grid cells with the size a
 * It will be saved as a Component in the RegionEntity
 */
public class RoughnessProvider implements FacetProvider {

    private final int gridSize = 4;


    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {

        Border3D border = region.getBorderForFacet(RoughnessFacet.class);
        RoughnessFacet facet = new RoughnessFacet(region.getRegion(), border, gridSize);

        SeaLevelFacet seaLevelFacet = region.getRegionFacet(SeaLevelFacet.class);
        SurfaceHeightFacet surfaceHeightFacet = region.getRegionFacet(SurfaceHeightFacet.class);
        Rect2i processRegion = facet.getGridWorldRegion();

        for (BaseVector2i pos : processRegion.contents()) {

            if (surfaceHeightFacet.getWorld(pos) > seaLevelFacet.getSeaLevel()) {
                facet.calcRoughness(new Vector2i(pos.x(), pos.y()), surfaceHeightFacet);
            } else {
                facet.setWorld(pos, -1000);
            }
        }

        region.setRegionFacet(RoughnessFacet.class, facet);
    }


}
