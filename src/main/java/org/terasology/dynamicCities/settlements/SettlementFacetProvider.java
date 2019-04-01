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

package org.terasology.dynamicCities.settlements;


import org.terasology.dynamicCities.sites.SiteComponent;
import org.terasology.dynamicCities.sites.SiteFacet;
import org.terasology.math.TeraMath;
import org.terasology.namegenerator.town.TownNameProvider;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

/**
 * Generates the name and initial population for a settlement
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
            //Generate a name
            long nameSeed = noiseGen.intNoise(siteComponent.getPos().x, siteComponent.getPos().y);
            TownNameProvider nameProvider = new TownNameProvider(nameSeed);

            float populationNoise = noiseGen.noise(siteComponent.getPos().x, siteComponent.getPos().y);
            float scaleFactor = SettlementConstants.MAX_POPULATIONSIZE - SettlementConstants.MIN_POPULATIONSIZE;
            int population = TeraMath.fastAbs(Math.round(populationNoise * (scaleFactor))) + SettlementConstants.MIN_POPULATIONSIZE;

            SettlementComponent settlementComponent = new SettlementComponent(siteComponent, nameProvider.generateName(), population);

            settlementFacet.setSettlement(settlementComponent);
        }

        region.setRegionFacet(SettlementFacet.class, settlementFacet);
    }
}
