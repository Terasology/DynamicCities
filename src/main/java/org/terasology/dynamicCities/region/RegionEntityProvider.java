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
import org.terasology.dynamicCities.region.components.ResourceFacetComponent;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.UnregisteredRegionComponent;
import org.terasology.dynamicCities.sites.SiteFacet;
import org.terasology.entitySystem.entity.EntityStore;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetworkComponent;
import org.terasology.rendering.nui.Color;
import org.terasology.world.generation.EntityBuffer;
import org.terasology.world.generation.EntityProvider;
import org.terasology.world.generation.Region;
import org.terasology.world.generation.facets.SurfaceHeightFacet;
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

        SurfaceHeightFacet surfaceHeightFacet = region.getFacet(SurfaceHeightFacet.class);
        RoughnessFacet roughnessFacet = region.getFacet(RoughnessFacet.class);
        ResourceFacet resourceFacet = region.getFacet(ResourceFacet.class);
        SiteFacet siteFacet = region.getFacet(SiteFacet.class);
        Region3i worldRegion = region.getRegion();

        if (checkCorners(worldRegion, surfaceHeightFacet)) {
            EntityStore entityStore = new EntityStore();

            RoughnessFacetComponent roughnessFacetComponent = new RoughnessFacetComponent(roughnessFacet);
            ResourceFacetComponent resourceFacetComponent = new ResourceFacetComponent(resourceFacet);
            entityStore.addComponent(roughnessFacetComponent);
            entityStore.addComponent(resourceFacetComponent);

            LocationComponent locationComponent = new LocationComponent(worldRegion.center());
            entityStore.addComponent(locationComponent);

            NameTagComponent nameTagComponent = new NameTagComponent();
            nameTagComponent.text = "Roughness: "
                    + roughnessFacetComponent.meanDeviation + " Grass: " + resourceFacetComponent.getResourceSum("Grass")
                    + locationComponent.getWorldPosition().toString();
            nameTagComponent.textColor = Color.WHITE;
            nameTagComponent.yOffset = 10;
            nameTagComponent.scale = 10;

            if (siteFacet.getSite() != null) {
                entityStore.addComponent(siteFacet.getSite());
                nameTagComponent.text += " Site";
            }
            entityStore.addComponent(nameTagComponent);

            //Region component is used as identifier for a region entity
            entityStore.addComponent(new UnregisteredRegionComponent());
            entityStore.addComponent(new NetworkComponent());
            buffer.enqueue(entityStore);

        }
   }

    //Checks if the region is on the surface
    protected boolean checkCorners(Region3i worldRegion, BaseFieldFacet2D facet) {
        Vector3i max = worldRegion.max();
        Vector3i min = worldRegion.min();
        int counter = 0;
        float[] corners = new float[5];
        Vector2i[] positions = new Vector2i[5];
        
        positions[0] = new Vector2i(max.x(), max.z());
        positions[1] = new Vector2i(min.x(), min.z());
        positions[2] = new Vector2i(min.x() + worldRegion.sizeX(), min.z());
        positions[3] = new Vector2i(min.x(), min.z() + worldRegion.sizeZ());
        positions[4] = new Vector2i(worldRegion.center().x, worldRegion.center().z);

        for (int i = 0; i < corners.length; i++) {
            corners[i] = facet.getWorld(positions[i]);
            if (corners[i] > worldRegion.maxY() || corners[i] < worldRegion.minY()) {
                counter++;
            }
        }

        return (counter < 3);
    }

}
