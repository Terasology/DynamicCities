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


import org.terasology.dynamicCities.population.Population;
import org.terasology.dynamicCities.region.RegionEntityManager;
import org.terasology.dynamicCities.region.components.RegionEntities;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.UnassignedRegionComponent;
import org.terasology.dynamicCities.region.events.AssignRegionEvent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.dynamicCities.sites.Site;
import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.math.geom.*;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Color;
/**
 * Current tasks: Rewrite site to settlement conversion: Check sides and remove sitecomponent if all 
 */

@RegisterSystem(RegisterMode.AUTHORITY)
public class SettlementEntityManager extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    private EntityRef settlementEntities;

    @In
    private RegionEntityManager regionEntityManager;

    private int minDistance = 500;
    private RegionEntities regionEntitiesStore;
    private int settlementMaxRadius = 96;
    private int counter = 50;

    @Override
    public void postBegin() {
        settlementEntities = entityManager.create(new SettlementEntities());
        regionEntitiesStore = regionEntityManager.getRegionEntities();
    }

    @Override
    public void update(float delta) {
        counter--;
        if (counter != 0) {
            return;
        }
        Iterable<EntityRef> uncheckedSiteRegions = entityManager.getEntitiesWith(Site.class);
        for (EntityRef siteRegion : uncheckedSiteRegions) {
            if (checkMinDistance(siteRegion) && regionEntitiesStore.checkSidesLoadedNear(siteRegion)
                    && checkBuildArea(siteRegion)) {
                createSettlement(siteRegion);
                siteRegion.send(new SettlementRegisterEvent());
                siteRegion.removeComponent(Site.class);
            } else if (!checkMinDistance(siteRegion)) {
                siteRegion.removeComponent(Site.class);
            }

        }
        counter = 50;
    }

    @ReceiveEvent(components = {Site.class})
    public void registerSettlement(SettlementRegisterEvent event, EntityRef settlement) {
        settlementEntities.getComponent(SettlementEntities.class).add(settlement);
        settlement.addComponent(new ActiveSettlementComponent());
    }


    private boolean checkMinDistance(EntityRef siteRegion) {
        Vector3f sitePos = siteRegion.getComponent(LocationComponent.class).getLocalPosition();
        Vector2i pos = new Vector2i(sitePos.x(), sitePos.z());

        for (String vector2iString : settlementEntities.getComponent(SettlementEntities.class).getMap().keySet()) {
            Vector2i activePosition = Toolbox.stringToVector2i(vector2iString);
            if (pos.distance(activePosition) < minDistance) {
                return false;
            }
        }
        return true;
    }

    private void createSettlement(EntityRef siteRegion) {

        Vector2i regionCenter = new Vector2i(siteRegion.getComponent(LocationComponent.class).getLocalPosition().x(),
                                             siteRegion.getComponent(LocationComponent.class).getLocalPosition().z());
        Site site = siteRegion.getComponent(Site.class);

        //add surrounding regions to settlement
        RegionEntities regionEntities = new RegionEntities();
        getSurroundingRegions(regionCenter, regionEntities);


        LocationComponent locationComponent = siteRegion.getComponent(LocationComponent.class);
        Population population = new Population(site.getPopulation());


        NameTagComponent settlementName = new NameTagComponent();
        settlementName.text = "testcity regions: " + regionEntities.regionEntities.size();
        if (regionEntities.regionEntities.size() >= 28) {
            settlementName.text += " #";
        }
        settlementName.textColor = Color.CYAN;
        settlementName.yOffset = 20;
        settlementName.scale = 20;


        EntityRef settlement = entityManager.create(locationComponent,
                population, settlementName, regionEntities);
    }

    /**
     *
     * @param pos
     * @return
     */
    private void getSurroundingRegions(Vector2i pos, RegionEntities assignedRegions) {
        Rect2i settlementRectArea = Rect2i.createFromMinAndMax(-3, -3, 3, 3);
        Circle settlementCircle = new Circle(pos.toVector2f(), settlementMaxRadius);
        Vector2i regionWorldPos = new Vector2i();

        for (BaseVector2i regionPos : settlementRectArea.contents()) {
            regionWorldPos.set(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            if (settlementCircle.contains(regionWorldPos)) {
                EntityRef region = regionEntitiesStore.getNearest(regionWorldPos);
                if (regionEntitiesStore.getNearest(pos) == null) {
                    throw new NullPointerException();
                }
                if (region != null && region.hasComponent(UnassignedRegionComponent.class)) {
                    assignedRegions.add(region);
                    region.send(new AssignRegionEvent());
                }
            }
        }

    }

    private boolean checkBuildArea(EntityRef siteRegion) {
        LocationComponent siteLocation = siteRegion.getComponent(LocationComponent.class);
        Vector2i pos = new Vector2i(siteLocation.getLocalPosition().x(), siteLocation.getLocalPosition().z());
        int unusableRegionsCount = 0;
        Rect2i settlementRectArea = Rect2i.createFromMinAndMax(-3, -3,
                3, 3);
        Circle settlementCircle = new Circle(pos.toVector2f(), settlementMaxRadius);

        for (BaseVector2i regionPos : settlementRectArea.contents()) {
            Vector2i regionWorldPos = new Vector2i(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            if (settlementCircle.contains(regionWorldPos)) {
                EntityRef region = regionEntitiesStore.getNearest(regionWorldPos);
                if (region != null && region.getComponent(RoughnessFacetComponent.class).meanDeviation > 0.3) {
                    unusableRegionsCount++;
                }
            }
        }

        return unusableRegionsCount < 10;
    }


}
