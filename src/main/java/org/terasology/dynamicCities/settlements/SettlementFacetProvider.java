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

import org.terasology.cities.roads.Road;
import org.terasology.cities.roads.RoadFacet;
import org.terasology.cities.sites.Site;
import org.terasology.cities.sites.SiteFacet;
import org.terasology.namegenerator.town.DebugTownTheme;
import org.terasology.namegenerator.town.TownNameProvider;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;

/**
 *
 */
@Produces(SettlementFacet.class)
@Requires({@Facet(SiteFacet.class), @Facet(RoadFacet.class)})
public class SettlementFacetProvider implements FacetProvider {

    private WhiteNoise nameNoiseGen;

    @Override
    public void setSeed(long seed) {
        this.nameNoiseGen = new WhiteNoise(seed ^ 6207934);
    }

    @Override
    public void process(GeneratingRegion region) {

        SiteFacet siteFacet = region.getRegionFacet(SiteFacet.class);
        RoadFacet roadFacet = region.getRegionFacet(RoadFacet.class);

        Border3D border = region.getBorderForFacet(SettlementFacet.class);
        SettlementFacet settlementFacet = new SettlementFacet(region.getRegion(), border);

        for (Site site : siteFacet.getSettlements()) {
            long nameSeed = nameNoiseGen.intNoise(site.getPos().getX(), site.getPos().getY());

            // TODO: adapt NameProvider to provide a name for a given seed/pos
            TownNameProvider ng = new TownNameProvider(nameSeed, new DebugTownTheme());

            Settlement settlement = new Settlement(site, ng.generateName());

            for (Road road : roadFacet.getRoads()) {
                if (road.endsAt(site.getPos())) {
                    settlement.addRoad(road);
                }
            }

            settlementFacet.addSettlement(settlement);
        }

        region.setRegionFacet(SettlementFacet.class, settlementFacet);
    }
}
