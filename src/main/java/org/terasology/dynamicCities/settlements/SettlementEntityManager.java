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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.commonworld.Orientation;
import org.terasology.dynamicCities.buildings.BuildingManager;
import org.terasology.dynamicCities.buildings.BuildingQueue;
import org.terasology.dynamicCities.construction.Construction;
import org.terasology.dynamicCities.districts.DistrictManager;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.population.Culture;
import org.terasology.dynamicCities.population.CultureManager;
import org.terasology.dynamicCities.population.Population;
import org.terasology.dynamicCities.region.RegionEntityManager;
import org.terasology.dynamicCities.region.components.RegionEntitiesComponent;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.UnassignedRegionComponent;
import org.terasology.dynamicCities.region.events.AssignRegionEvent;
import org.terasology.dynamicCities.settlements.components.ActiveSettlementComponent;
import org.terasology.dynamicCities.settlements.components.DistrictFacetComponent;
import org.terasology.dynamicCities.settlements.components.MarketComponent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.dynamicCities.sites.Site;
import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.economy.components.InfiniteStorageComponent;
import org.terasology.economy.components.MarketSubscriberComponent;
import org.terasology.economy.events.SubscriberRegistrationEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.logic.players.MinimapSystem;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Circle;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.generation.Border3D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @In
    private MinimapSystem minimapSystem;

    @In
    private BuildingManager buildingManager;

    @In
    private CultureManager cultureManager;

    @In
    private DistrictManager districtManager;

    @In
    private SettlementCachingSystem settlementCachingSystem;

    private int minDistance = 1000;
    private RegionEntitiesComponent regionEntitiesComponentStore;
    private int settlementMaxRadius = 256;
    private int counter = 50;
    private int timer = 0;
    private Noise randNumGen;

    private Logger logger = LoggerFactory.getLogger(SettlementEntityManager.class);
    @Override
    public void postBegin() {

        settlementEntities = settlementCachingSystem.getSettlementCacheEntity();
        regionEntitiesComponentStore = regionEntityManager.getRegionEntitiesComponent();
        randNumGen = new WhiteNoise(regionEntitiesComponentStore.hashCode() & 0x921233);

    }

    @Override
    public void update(float delta) {
        if (!settlementCachingSystem.isInitialised()) {
            return;
        } else if (settlementCachingSystem.isInitialised() && settlementEntities == null) {
            settlementEntities = settlementCachingSystem.getSettlementCacheEntity();
        }
        counter--;
        timer++;
        if (counter != 0) {
            return;
        }
        Iterable<EntityRef> uncheckedSiteRegions = entityManager.getEntitiesWith(Site.class);
        for (EntityRef siteRegion : uncheckedSiteRegions) {
            boolean checkDistance = checkMinDistance(siteRegion);
            boolean checkBuildArea = checkBuildArea(siteRegion);
            if (checkDistance && regionEntitiesComponentStore.checkSidesLoadedNear(siteRegion)
                    && checkBuildArea) {
                EntityRef newSettlement = createSettlement(siteRegion);
                newSettlement.send(new SettlementRegisterEvent());
                siteRegion.removeComponent(Site.class);
            } else if (!checkDistance || !checkBuildArea) {
                siteRegion.removeComponent(Site.class);
            }
        }
        Iterable<EntityRef> activeSettlements = entityManager.getEntitiesWith(BuildingQueue.class);
        for (EntityRef settlement : activeSettlements) {
            growSettlement(settlement, timer);
            build(settlement);
        }
        counter = 500;
    }


    public boolean checkMinDistance(EntityRef siteRegion) {
        Vector3f sitePos = siteRegion.getComponent(LocationComponent.class).getLocalPosition();
        Vector2i pos = new Vector2i(sitePos.x(), sitePos.z());
        SettlementsCacheComponent container = settlementEntities.getComponent(SettlementsCacheComponent.class);
        for (String vector2iString : container.settlementEntities.keySet()) {
            Vector2i activePosition = Toolbox.stringToVector2i(vector2iString);
            if (pos.distance(activePosition) < minDistance) {
                return false;
            }
        }
        return true;
    }

    public boolean checkMinDistanceCell(Vector2i pos) {
        if (!regionEntitiesComponentStore.cellIsLoaded(pos)) {
            return true;
        }

        SettlementsCacheComponent container = settlementEntities.getComponent(SettlementsCacheComponent.class);
        for (String vector2iString : container.settlementEntities.keySet()) {
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

    private EntityRef createSettlement(EntityRef siteRegion) {
        EntityRef settlementEntity = entityManager.create();
        Vector2i regionCenter = new Vector2i(siteRegion.getComponent(LocationComponent.class).getLocalPosition().x(),
                                             siteRegion.getComponent(LocationComponent.class).getLocalPosition().z());
        Site site = siteRegion.getComponent(Site.class);
        LocationComponent locationComponent = siteRegion.getComponent(LocationComponent.class);
        Population population = new Population(site.getPopulation());
        Culture culture = cultureManager.getRandomCulture();

        //add surrounding regions to settlement
        RegionEntitiesComponent regionEntitiesComponent = new RegionEntitiesComponent();
        getSurroundingRegions(regionCenter, regionEntitiesComponent);

        //Create the district facet and DistrictTypeMap
        Region3i region = Region3i.createFromCenterExtents(new Vector3i(locationComponent.getLocalPosition()), SettlementConstants.SETTLEMENT_RADIUS);
        Border3D border = new Border3D(0, 0, 0);
        DistrictFacetComponent districtGrid = new DistrictFacetComponent(region, border, SettlementConstants.DISTRICT_GRIDSIZE, site.hashCode(), districtManager, culture);
        if (districtGrid.districtMap.size() < 1) {
            logger.error("DistrictFacetComponent.districtMap not initialised!");
        }

        //Storage for parcels
        ParcelList parcels = new ParcelList(1);

        //Storage for incomplete parcels
        BuildingQueue buildingQueue = new BuildingQueue();

        //NameTagStuff
        NameTagComponent settlementName = new NameTagComponent();
        settlementName.text = "testcity regions: " + regionEntitiesComponent.regionEntities.size() + " " + population.populationSize;
        settlementName.textColor = Color.CYAN;
        settlementName.yOffset = 20;
        settlementName.scale = 20;

        //Marketstuff
        EntityRef market = entityManager.create(new InfiniteStorageComponent(1));
        MarketSubscriberComponent marketSubscriberComponent = new MarketSubscriberComponent(1);
        marketSubscriberComponent.consumptionStorage = market;
        marketSubscriberComponent.productStorage = settlementEntity;
        marketSubscriberComponent.productionInterval = 1;
        marketSubscriberComponent.production.put(population.popResourceType, Math.round(culture.growthRate));
        MarketComponent marketComponent = new MarketComponent(market);


        NetworkComponent networkComponent = new NetworkComponent();
        networkComponent.replicateMode = NetworkComponent.ReplicateMode.ALWAYS;
        settlementEntity.addComponent(locationComponent);
        settlementEntity.addComponent(districtGrid);
        settlementEntity.addComponent(culture);
        settlementEntity.addComponent(population);
        settlementEntity.addComponent(settlementName);
        settlementEntity.addComponent(regionEntitiesComponent);
        settlementEntity.addComponent(parcels);
        settlementEntity.addComponent(buildingQueue);
        settlementEntity.addComponent(marketComponent);
        settlementEntity.addComponent(marketSubscriberComponent);
        settlementEntity.addComponent(new ActiveSettlementComponent());
        settlementEntity.addComponent(networkComponent);

        settlementEntity.send(new SubscriberRegistrationEvent());
        settlementEntity.setAlwaysRelevant(true);
        return settlementEntity;
    }

    /**
     *
     * @param pos
     * @return
     */
    private void getSurroundingRegions(Vector2i pos, RegionEntitiesComponent assignedRegions) {
        Rect2i settlementRectArea = Rect2i.createFromMinAndMax(-3, -3, 3, 3);
        Circle settlementCircle = new Circle(pos.toVector2f(), settlementMaxRadius);
        Vector2i regionWorldPos = new Vector2i();

        for (BaseVector2i regionPos : settlementRectArea.contents()) {
            regionWorldPos.set(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            if (settlementCircle.contains(regionWorldPos)) {
                EntityRef region = regionEntitiesComponentStore.getNearest(regionWorldPos);
                if (regionEntitiesComponentStore.getNearest(pos) == null) {
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
                EntityRef region = regionEntitiesComponentStore.getNearest(regionWorldPos);
                if (region != null && region.hasComponent(RoughnessFacetComponent.class)) {
                    if (region.getComponent(RoughnessFacetComponent.class).meanDeviation > 99) {
                        unusableRegionsCount++;
                    }
                }
            }
        }

        return unusableRegionsCount < 15;
    }



    public void build(EntityRef settlement) {
        BuildingQueue buildingQueue = settlement.getComponent(BuildingQueue.class);
        Culture culture = settlement.getComponent(Culture.class);
        Set<DynParcel> removedParcels = new HashSet<>();
        Set<DynParcel> parcels = buildingQueue.buildingQueue;

        for (DynParcel dynParcel : parcels) {
            if (constructer.buildParcel(dynParcel, settlement, culture)) {
                removedParcels.add(dynParcel);
            }
        }
        parcels.removeAll(removedParcels);
        settlement.saveComponent(buildingQueue);
    }

    public void growSettlement(EntityRef settlement, int time) {
        DistrictFacetComponent districtFacetComponent = settlement.getComponent(DistrictFacetComponent.class);
        Population population = settlement.getComponent(Population.class);
        ParcelList parcels = settlement.getComponent(ParcelList.class);
        BuildingQueue buildingQueue = settlement.getComponent(BuildingQueue.class);
        LocationComponent locationComponent = settlement.getComponent(LocationComponent.class);
        NameTagComponent nameTagComponent = settlement.getComponent(NameTagComponent.class);
        Culture culture = settlement.getComponent(Culture.class);
        MarketSubscriberComponent marketSubscriberComponent = settlement.getComponent(MarketSubscriberComponent.class);

        Vector3i center = new Vector3i(locationComponent.getLocalPosition());
        Random rng = new FastRandom(locationComponent.getLocalPosition().hashCode() ^ 0x1496327 ^ time);
        float minRadius = parcels.minBuildRadius;
        float maxRadius = parcels.maxBuildRadius;
        int maxIterations = 40;
        int buildingSpawned = 0;
        List<String> zones = new ArrayList<>(buildingManager.getZones());
        Map<String, Vector2i> minMaxSizes = buildingManager.getMinMaxSizePerZone();

        if (population == null) {
            logger.error("No population found or was uninitialised!");
            return;
        }
        if (culture == null) {
            logger.error("No culture found or was uninitialised!");
            return;
        }
        if (parcels == null) {
            logger.error("No parcelList found or was uninitialised!");
            return;
        }
        if (buildingQueue == null) {
            logger.error("No buildingQueue found or was uninitialised!");
            return;
        }
        if (locationComponent == null) {
            logger.error("No locationComponent found or was uninitialised!");
            return;
        }
        if (nameTagComponent == null) {
            logger.error("No nameTagComponent found or was uninitialised!");
            return;
        }
        if (districtFacetComponent == null || districtFacetComponent.districtMap == null || districtFacetComponent.districtTypeMap == null) {
            logger.error("No DistrictFacetComponent found or was uninitialised!");
            return;
        }



        /**
         * TODO: Generate more random rectangles (currently only squares).
         */
        for (String zone : zones) {
            while (culture.getBuildingNeedsForZone(zone) * population.populationSize - parcels.areaPerZone.getOrDefault(zone, 0) > minMaxSizes.get(zone).x() && buildingSpawned < SettlementConstants.MAX_BUILDINGSPAWN) {
                int iter = 0;
                int size = Math.round(TeraMath.fastAbs(rng.nextInt((int) Math.ceil(Math.sqrt((double) minMaxSizes.get(zone).x())),
                        (int) Math.floor(Math.sqrt(minMaxSizes.get(zone).y())))));
                Rect2i shape;
                Orientation orientation = Orientation.NORTH.getRotated(90 * rng.nextInt(5));
                Vector2i rectPosition = new Vector2i();
                float radius;
                //Place parcel randomly until it hits the right district
                //For now: Just place is somewhere non intersecting
                do {
                    iter++;
                    float angle = rng.nextFloat(0, 360);
                    radius = rng.nextFloat(0, minRadius);
                    rectPosition.set((int) Math.round(radius * Math.sin((double) angle) + center.x()),
                            (int) Math.round(radius * Math.cos((double) angle)) + center.z());
                    shape = Rect2i.createFromMinAndSize(rectPosition.x(), rectPosition.y(), size, size);
                } while ((!parcels.isNotIntersecting(shape)
                        || !(districtFacetComponent.getDistrict(rectPosition.x(), rectPosition.y()).isValidType(zone)))
                        && iter != maxIterations);
                //Grow settlement radius if no valid area was found
                if (iter == maxIterations && minRadius < SettlementConstants.SETTLEMENT_RADIUS) {
                    minRadius += SettlementConstants.BUILD_RADIUS_INTERVALL;
                    break;
                } else if (iter == maxIterations) {
                    break;
                }
                if (radius > maxRadius) {
                    parcels.maxBuildRadius = radius;
                }

                DynParcel newParcel = new DynParcel(shape, orientation, zone, Math.round(locationComponent.getLocalPosition().y()));
                parcels.addParcel(newParcel);
                buildingQueue.buildingQueue.add(newParcel);
                buildingSpawned++;
            }
        }
        /**
         * grow population
         */
        for (String residentialZone : culture.residentialZones) {
            population.capacity += parcels.areaPerZone.getOrDefault(residentialZone, 0);
        }



        nameTagComponent.text =  Float.toString(population.populationSize);
        settlement.saveComponent(parcels);
        settlement.saveComponent(nameTagComponent);
        settlement.saveComponent(population);
        settlement.saveComponent(buildingQueue);
    }


}
