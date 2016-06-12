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


import org.terasology.cities.bldg.Building;
import org.terasology.commonworld.Orientation;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.dynamicCities.buildings.BuildingQueue;
import org.terasology.dynamicCities.buildings.GenericBuilding;
import org.terasology.dynamicCities.construction.Construction;
import org.terasology.dynamicCities.gen.SimpleChurchGenerator;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.ParcelList;
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
import org.terasology.registry.Share;
import org.terasology.rendering.nui.Color;
/**
 * Current tasks: Rewrite site to settlement conversion: Check sides and remove sitecomponent if all 
 */

@Share(value = SettlementEntityManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class SettlementEntityManager extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    private EntityRef settlementEntities;

    @In
    private RegionEntityManager regionEntityManager;

    @In
    private Construction constructer;

    private int minDistance = 500;
    private RegionEntities regionEntitiesStore;
    private int settlementMaxRadius = 96;
    private int counter = 80;

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
            boolean checkDistance = checkMinDistance(siteRegion);
            boolean checkBuildArea = checkBuildArea(siteRegion);
            if (checkDistance && regionEntitiesStore.checkSidesLoadedNear(siteRegion)
                    && checkBuildArea) {
                createSettlement(siteRegion);
                siteRegion.send(new SettlementRegisterEvent());
                siteRegion.removeComponent(Site.class);
            } else if (!checkDistance || !checkBuildArea) {
                siteRegion.removeComponent(Site.class);
            }

        }
        Iterable<EntityRef> activeSettlements = entityManager.getEntitiesWith(BuildingQueue.class);
        for (EntityRef settlement : activeSettlements) {
            settlement.getComponent(BuildingQueue.class).build();
        }
        counter = 100;
    }

    @ReceiveEvent(components = {Site.class})
    public void registerSettlement(SettlementRegisterEvent event, EntityRef settlement) {
        settlementEntities.getComponent(SettlementEntities.class).add(settlement);
        settlement.addComponent(new ActiveSettlementComponent());
    }


    public boolean checkMinDistance(EntityRef siteRegion) {
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

    public boolean checkMinDistanceCell(Vector2i pos) {
        if (!regionEntitiesStore.cellIsLoaded(pos)) {
            return true;
        }
        for (String vector2iString : settlementEntities.getComponent(SettlementEntities.class).getMap().keySet()) {
            Vector2i activePosition = Toolbox.stringToVector2i(vector2iString);
            if (pos.distance(activePosition) < minDistance - settlementMaxRadius) {
                return false;
            }
        }
        return true;
    }

    public boolean checkMinDistanceCell(String posString) {
        return checkMinDistanceCell(Toolbox.stringToVector2i(posString));
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

        /**
         * some sample parcels for testing
         */
        Orientation or1 = Orientation.NORTH;
        Orientation or2 = Orientation.SOUTH;

        Rect2i shape1 = Rect2i.createFromMinAndSize(regionCenter.add(5, 5), new Vector2i(30, 30));
        Rect2i shape2 = Rect2i.createFromMinAndSize(regionCenter.sub(30, 30), new Vector2i(20, 20));

        DynParcel par1 = new DynParcel(shape1, or1, Math.round(locationComponent.getLocalPosition().y()));
        DynParcel par2 = new DynParcel(shape2, or2, Math.round(locationComponent.getLocalPosition().y()));

        SimpleChurchGenerator commercialBuildingGenerator = new SimpleChurchGenerator(1423243);
        Building testBldg = commercialBuildingGenerator.apply(par1, HeightMaps.constant(constructer.flatten(par1.shape, par1.height)));
        Building testBldg2 = commercialBuildingGenerator.apply(par1, HeightMaps.constant(constructer.flatten(par2.shape, par2.height)));
        GenericBuilding testGenBldg = new GenericBuilding(testBldg);
        GenericBuilding testGenBldg2 = new GenericBuilding(testBldg2);
        par1.addGenericBuilding(testGenBldg);
        par2.addGenericBuilding(testGenBldg2);

        ParcelList parcels = new ParcelList();
        parcels.parcels.add(par1);
        parcels.parcels.add(par2);

        BuildingQueue buildingQueue = new BuildingQueue(constructer);
        buildingQueue.buildingQueue.add(par1);
        buildingQueue.buildingQueue.add(par2);





        NameTagComponent settlementName = new NameTagComponent();
        settlementName.text = "testcity regions: " + regionEntities.regionEntities.size();
        if (regionEntities.regionEntities.size() >= 28) {
            settlementName.text += " #";
        }
        settlementName.textColor = Color.CYAN;
        settlementName.yOffset = 20;
        settlementName.scale = 20;


        EntityRef settlement = entityManager.create(locationComponent,
                population, settlementName, regionEntities, parcels, buildingQueue);
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
        Rect2i settlementRectArea = Rect2i.createFromMinAndMax(-3, -3, 3, 3);
        Circle settlementCircle = new Circle(pos.toVector2f(), settlementMaxRadius);

        for (BaseVector2i regionPos : settlementRectArea.contents()) {
            Vector2i regionWorldPos = new Vector2i(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            if (settlementCircle.contains(regionWorldPos)) {
                EntityRef region = regionEntitiesStore.getNearest(regionWorldPos);
                if (region != null && region.hasComponent(RoughnessFacetComponent.class)) {
                    if (region.getComponent(RoughnessFacetComponent.class).meanDeviation > 0.3) {
                        unusableRegionsCount++;
                    }
                }
            }
        }

        return unusableRegionsCount < 15;
    }
}
