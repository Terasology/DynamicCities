// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.settlements;


import org.terasology.dynamicCities.sites.SiteComponent;
import org.terasology.dynamicCities.sites.SiteFacet;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProvider;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.math.TeraMath;

/**
 * Generates the initial population for a settlement
 */
@Produces(SettlementFacet.class)
@Requires({@Facet(SiteFacet.class)})
public class SettlementFacetProvider implements FacetProvider {

    private WhiteNoise noiseGen;

    @Override
    public void setSeed(long seed) {
        this.noiseGen = new WhiteNoise(seed ^ 6207934);
    }

    @Override
    public void process(GeneratingRegion region) {
        SiteFacet siteFacet = region.getRegionFacet(SiteFacet.class);
        SiteComponent siteComponent = siteFacet.getSiteComponent();

        Border3D border = region.getBorderForFacet(SettlementFacet.class);
        SettlementFacet settlementFacet = new SettlementFacet(region.getRegion(), border);

        if (siteComponent != null) {
            float populationNoise = noiseGen.noise(siteComponent.getPos().x, siteComponent.getPos().y);
            float scaleFactor = SettlementConstants.MAX_POPULATIONSIZE - SettlementConstants.MIN_POPULATIONSIZE;
            int population =
                    TeraMath.fastAbs(Math.round(populationNoise * (scaleFactor))) + SettlementConstants.MIN_POPULATIONSIZE;

            SettlementComponent settlementComponent = new SettlementComponent(siteComponent, population);

            settlementFacet.setSettlement(settlementComponent);
        }

        region.setRegionFacet(SettlementFacet.class, settlementFacet);
    }
}
