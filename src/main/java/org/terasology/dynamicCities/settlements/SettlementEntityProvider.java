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

import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.NetworkComponent;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.EntityBuffer;
import org.terasology.world.generation.EntityProvider;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

/**
 * Adds name tags for settlements.
 */
public class SettlementEntityProvider implements EntityProvider {

    private Noise nameNoiseGen = new WhiteNoise(6207934)



    @Override
    public void process(Region region, EntityBuffer buffer) {

        /**
        * Merged SettlementFacetProvider and such stuff with this
        * Use the RegionGrid to get a Hashcode for each spawned settlement for later use
        * Format for settlement entity id : "settlement[XCoordinate][ZCoordinate]" e.g. : "settlement3232"
         * Whereas the coordinates are the minvalue of the region 2D rectangle
        */

        SiteFacet siteFacet = region.getRegionFacet(SiteFacet.class);
        SurfaceHeightFacet surfaceHeightFacet = region.getFacet(SurfaceHeightFacet.class);

        for (Settlement settlement : settlementFacet.getSettlements()) {
            ImmutableVector2i pos2d = settlement.getSite().getPos();
            int x = pos2d.getX();
            int z = pos2d.getY();
            int y = TeraMath.floorToInt(heightFacet.getWorld(pos2d));
            if (region.getRegion().encompasses(x, y, z)) {

                EntityStore entityStore = new EntityStore();
                //Get NameTags into SettlementEntity
                /*
                NameTagComponent nameTagComponent = new NameTagComponent();
                nameTagComponent.text = settlement.getName();
                nameTagComponent.textColor = Color.WHITE;
                nameTagComponent.yOffset = 10;
                nameTagComponent.scale = 20;
                entityStore.addComponent(nameTagComponent);
                */
                Vector3f pos3d = new Vector3f(x, y, z);
                LocationComponent locationComponent = new LocationComponent(pos3d);
                entityStore.addComponent(locationComponent);

                entityStore.addComponent(new NetworkComponent());
                entityStore.addComponent(new SettlementComponent(settlement));
                buffer.enqueue(entityStore);
            }
        }
   }

}
