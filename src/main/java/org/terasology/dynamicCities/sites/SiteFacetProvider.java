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

package org.terasology.dynamicCities.sites;

import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.dynamicCities.resource.ResourceType;
import org.terasology.dynamicCities.settlements.SettlementConstants;
import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.rendering.nui.properties.Range;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.ConfigurableFacetProvider;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 *
 */
@Produces(SiteFacet.class)
@Requires({ @Facet(RoughnessFacet.class),
            @Facet(SeaLevelFacet.class),
            @Facet(SurfaceHeightFacet.class)})
public class SiteFacetProvider implements ConfigurableFacetProvider {

    private SiteConfiguration config = new SiteConfiguration();


    private Noise sizeNoiseGen;
    @Override
    public void setSeed(long seed) {
        this.sizeNoiseGen = new WhiteNoise(seed ^ 0x2347928);
    }

    @Override
    public void process(GeneratingRegion region) {


        RoughnessFacet roughnessFacet = region.getRegionFacet(RoughnessFacet.class);
        ResourceFacet resourceFacet = region.getRegionFacet(ResourceFacet.class);



        Border3D border = region.getBorderForFacet(SiteFacet.class);
        Region3i coreReg = region.getRegion();
        SiteFacet siteFacet = new SiteFacet(coreReg, border);

        if (roughnessFacet.getMeanDeviation() < 0.3f && roughnessFacet.getMeanDeviation() > 0
                && resourceFacet.getResourceSum(ResourceType.GRASS.toString()) > 750) {
            BaseVector2i minPos = new Vector2i();
            float minDev = 10;
            for (BaseVector2i pos : roughnessFacet.getGridWorldRegion().contents()) {
                float currentDev = roughnessFacet.getWorld(pos);
                if (currentDev < minDev && currentDev > 0) {
                    minDev = currentDev;
                    minPos = pos;
                }
            }

            int population = TeraMath.fastAbs(Math.round(sizeNoiseGen.noise(minPos.getX(), minPos.getY())
                    * (SettlementConstants.MAX_POPULATIONSIZE - SettlementConstants.MIN_POPULATIONSIZE))) + SettlementConstants.MIN_POPULATIONSIZE;
            Site site = new Site(minPos.getX(), minPos.getY(), population);
            siteFacet.setSite(site);
        }


        region.setRegionFacet(SiteFacet.class, siteFacet);
    }

    @Override
    public String getConfigurationName() {
        return "settlements";
    }

    @Override
    public Component getConfiguration() {
        return config;
    }

    @Override
    public void setConfiguration(Component configuration) {
        this.config = (SiteConfiguration) configuration;
    }


    private static class SiteConfiguration implements Component {

        @Range(label = "Minimal town size", description = "Minimal town size in blocks", min = 1, max = 150, increment = 10, precision = 1)
        private int minRadius = 50;

        @Range(label = "Maximum town population", description = "Maximum town population", min = 10, max = 350, increment = 10, precision = 1)
        private int maxPopulation = 100;

        @Range(label = "Minimum distance between towns", min = 10, max = 1000, increment = 10, precision = 1)
        private int minDistance = 128;
    }
}
