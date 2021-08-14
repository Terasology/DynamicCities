// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.sites;

import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.ConfigurableFacetProvider;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.nui.properties.Range;

/**
 * Marks sites suitable to build settlements on
 */
@Produces(SiteFacet.class)
@Requires(@Facet(RoughnessFacet.class))
public class SiteFacetProvider implements ConfigurableFacetProvider {

    private SiteConfiguration config = new SiteConfiguration();

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {

        RoughnessFacet roughnessFacet = region.getRegionFacet(RoughnessFacet.class);

        Border3D border = region.getBorderForFacet(SiteFacet.class);
        BlockRegion coreReg = region.getRegion();
        SiteFacet siteFacet = new SiteFacet(coreReg, border);

        if (roughnessFacet.getMeanDeviation() < 0.3f && roughnessFacet.getMeanDeviation() > 0) {
            Vector2i minPos = new Vector2i();
            float minDev = 10;
            for (Vector2ic pos : roughnessFacet.getGridWorldRegion()) {
                float currentDev = roughnessFacet.getWorld(pos);
                if (currentDev < minDev && currentDev > 0) {
                    minDev = currentDev;
                    minPos.set(pos);
                }
            }

            // Removes sites that are too close to spawn. Spawn is assumed to be at (0, 0, 0).
            if (minPos.length() > config.minSpawnGap) {
                SiteComponent siteComponent = new SiteComponent(minPos.x(), minPos.y());
                siteFacet.setSiteComponent(siteComponent);
            }
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


    private static class SiteConfiguration implements Component<SiteConfiguration> {

        @Range(label = "Minimal town size", description = "Minimal town size in blocks", min = 1, max = 150, increment = 10, precision = 1)
        private int minRadius = 50;

        @Range(label = "Maximum town population", description = "Maximum town population", min = 10, max = 350, increment = 10, precision = 1)
        private int maxPopulation = 100;

        @Range(label = "Minimum distance between towns", min = 10, max = 1000, increment = 10, precision = 1)
        private int minDistance = 128;

        // Spawn is assumed to be at (0, 0, 0) for this setting.
        @Range(label = "Minimum distance from spawn", min = 0, max = 1000, increment = 10, precision = 1)
        private int minSpawnGap = 200;

        @Override
        public void copyFrom(SiteConfiguration other) {
            this.minRadius = other.minRadius;
            this.maxPopulation = other.maxPopulation;
            this.minDistance = other.minDistance;
            this.minSpawnGap = other.minSpawnGap;
        }
    }
}
