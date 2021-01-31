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

import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.dynamicCities.region.components.ResourceFacetComponent;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.TreeFacetComponent;
import org.terasology.dynamicCities.region.components.UnregisteredRegionComponent;
import org.terasology.dynamicCities.settlements.SettlementFacet;
import org.terasology.dynamicCities.sites.SiteFacet;
import org.terasology.dynamicCities.world.trees.TreeFacet;
import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.network.NetworkComponent;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.EntityBuffer;
import org.terasology.world.generation.EntityProvider;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.ElevationFacet;
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;

/**
 * Add an entity for each region to serve as storage for relevant data
 * At worldgen create for each region one
 * Afterwards, if no settlement is adjacent clear them of unrelevant data
 * Only create an entity if it's a surface region
 */

public class RegionEntityProvider implements EntityProvider {

    @Override
    public void process(Region region, EntityBuffer buffer) {

        ElevationFacet elevationFacet = region.getFacet(ElevationFacet.class);
        BlockRegion worldRegion = region.getRegion();

        if (checkCorners(worldRegion, elevationFacet)) {
            RoughnessFacet roughnessFacet = region.getFacet(RoughnessFacet.class);
            ResourceFacet resourceFacet = region.getFacet(ResourceFacet.class);
            TreeFacet treeFacet = region.getFacet(TreeFacet.class);
            SiteFacet siteFacet = region.getFacet(SiteFacet.class);
            SettlementFacet settlementFacet = region.getFacet(SettlementFacet.class);

            EntityStore entityStore = new EntityStore();

            RoughnessFacetComponent roughnessFacetComponent = new RoughnessFacetComponent(roughnessFacet);
            ResourceFacetComponent resourceFacetComponent = new ResourceFacetComponent(resourceFacet);
            TreeFacetComponent treeFacetComponent = new TreeFacetComponent(treeFacet);
            entityStore.addComponent(roughnessFacetComponent);
            entityStore.addComponent(resourceFacetComponent);
            entityStore.addComponent(treeFacetComponent);

            LocationComponent locationComponent = new LocationComponent(worldRegion.center(new Vector3f()));
            entityStore.addComponent(locationComponent);

            if (siteFacet.getSiteComponent() != null) {
                entityStore.addComponent(siteFacet.getSiteComponent());
            }

            if (settlementFacet.getSettlement() != null) {
                entityStore.addComponent(settlementFacet.getSettlement());
            }

            //Region component is used as identifier for a region entity
            entityStore.addComponent(new UnregisteredRegionComponent());
            entityStore.addComponent(new NetworkComponent());
            buffer.enqueue(entityStore);

        }
   }

    //Checks if the region is on the surface
    protected boolean checkCorners(BlockRegion worldRegion, BaseFieldFacet2D facet) {
        Vector3i max = worldRegion.getMax(new Vector3i());
        Vector3i min = worldRegion.getMin(new Vector3i());
        int counter = 0;
        float[] corners = new float[5];
        Vector2i[] positions = new Vector2i[5];

        positions[0] = new Vector2i(max.x(), max.z());
        positions[1] = new Vector2i(min.x(), min.z());
        positions[2] = new Vector2i(min.x() + worldRegion.getSizeX(), min.z());
        positions[3] = new Vector2i(min.x(), min.z() + worldRegion.getSizeZ());
        positions[4] = new Vector2i(worldRegion.center(new Vector3f()).x, worldRegion.center(new Vector3f()).z,
            RoundingMode.FLOOR);

        for (int i = 0; i < corners.length; i++) {
            corners[i] = facet.getWorld(positions[i]);
            if (corners[i] > worldRegion.maxY() || corners[i] < worldRegion.minY()) {
                counter++;
            }
        }

        return (counter < 3);
    }

}
