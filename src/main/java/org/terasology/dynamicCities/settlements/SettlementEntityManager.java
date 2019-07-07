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
import org.terasology.dynamicCities.construction.BlockBufferSystem;
import org.terasology.dynamicCities.construction.Construction;
import org.terasology.dynamicCities.construction.TreeRemovalSystem;
import org.terasology.dynamicCities.districts.DistrictManager;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.population.CultureManager;
import org.terasology.dynamicCities.population.PopulationComponent;
import org.terasology.dynamicCities.population.ThemeManager;
import org.terasology.dynamicCities.region.RegionEntityManager;
import org.terasology.dynamicCities.region.components.RegionEntitiesComponent;
import org.terasology.dynamicCities.region.components.ResourceFacetComponent;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.UnassignedRegionComponent;
import org.terasology.dynamicCities.region.events.AssignRegionEvent;
import org.terasology.dynamicCities.resource.ResourceType;
import org.terasology.dynamicCities.roads.RoadQueue;
import org.terasology.dynamicCities.settlements.components.ActiveSettlementComponent;
import org.terasology.dynamicCities.settlements.components.DistrictFacetComponent;
import org.terasology.dynamicCities.settlements.events.CheckBuildingSpawnPreconditionsEvent;
import org.terasology.dynamicCities.settlements.events.SettlementGrowthEvent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.dynamicCities.sites.SiteComponent;
import org.terasology.dynamicCities.utilities.Toolbox;
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
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Circle;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.namegenerator.town.TownNameProvider;
import org.terasology.network.NetworkComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.generation.Border3D;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


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
    private ThemeManager themeManager;

    @In
    private DistrictManager districtManager;

    @In
    private SettlementCachingSystem settlementCachingSystem;

    @In
    private TreeRemovalSystem treeRemovalSystem;

    @In
    private BlockBufferSystem blockBufferSystem;

    private int minDistance = 200;
    private int settlementMaxRadius = 100;
    private int counter = 50;
    private int timer = 0;
    private Random rng;

    private Logger logger = LoggerFactory.getLogger(SettlementEntityManager.class);

    @Override
    public void postBegin() {

        settlementEntities = settlementCachingSystem.getSettlementCacheEntity();
        long seed = regionEntityManager.hashCode() & 0x921233;
        rng = new FastRandom(seed);

    }

    @Override
    public void update(float delta) {
        if (!settlementCachingSystem.isInitialised()) {
            return;
        } else if (settlementEntities == null) {
            settlementEntities = settlementCachingSystem.getSettlementCacheEntity();
        }

        counter--;
        timer++;
        if (counter != 0) {
            return;
        }
        Iterable<EntityRef> uncheckedSiteRegions = entityManager.getEntitiesWith(SiteComponent.class);

        Set<Vector3f> uncheckedLocations = new HashSet<>();
        for (EntityRef location : uncheckedSiteRegions) {
            LocationComponent locationComponent = location.getComponent(LocationComponent.class);
            uncheckedLocations.add(locationComponent.getWorldPosition());
        }

        logger.info("\n\n UNCHECKED SITE REGIONS ARE: {} \n\n", uncheckedLocations);
        for (EntityRef siteRegion : uncheckedSiteRegions) {
            boolean checkDistance = checkMinDistance(siteRegion);
            boolean checkBuildArea = checkBuildArea(siteRegion);
            if (checkDistance && regionEntityManager.checkSidesLoadedNear(siteRegion)
                    && checkBuildArea) {
                EntityRef newSettlement = createSettlement(siteRegion);
                newSettlement.send(new SettlementRegisterEvent());
                siteRegion.removeComponent(SiteComponent.class);
            } else if (!checkDistance || !checkBuildArea) {
                siteRegion.removeComponent(SiteComponent.class);
            }
        }
        Iterable<EntityRef> activeSettlements = entityManager.getEntitiesWith(BuildingQueue.class);
        for (EntityRef settlement : activeSettlements) {
            growSettlement(settlement);
            build(settlement);

            logger.info("Now building roads from settlement {}...", settlement.getId());
            buildRoads(settlement);
        }
        counter = 250;
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
        if (!regionEntityManager.cellIsLoaded(pos)) {
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

        SiteComponent siteComponent = siteRegion.getComponent(SiteComponent.class);
        LocationComponent locationComponent = siteRegion.getComponent(LocationComponent.class);
        CultureComponent cultureComponent = cultureManager.getRandomCulture();

        SettlementComponent settlementComponent = siteRegion.getComponent(SettlementComponent.class);

        // Generate name
        TownNameProvider nameProvider = new TownNameProvider(rng.nextLong(), themeManager.getTownTheme(cultureComponent.theme));
        settlementComponent.name = nameProvider.generateName();

        PopulationComponent populationComponent = new PopulationComponent(settlementComponent.population);

        //add surrounding regions to settlement
        RegionEntitiesComponent regionEntitiesComponent = new RegionEntitiesComponent();

        //Create the district facet and DistrictTypeMap
        Region3i region = Region3i.createFromCenterExtents(new Vector3i(locationComponent.getLocalPosition()), SettlementConstants.SETTLEMENT_RADIUS);
        Border3D border = new Border3D(0, 0, 0);
        DistrictFacetComponent districtGrid = new DistrictFacetComponent(region, border, SettlementConstants.DISTRICT_GRIDSIZE, siteComponent.hashCode(), districtManager, cultureComponent);
        if (districtGrid.districtMap.size() < 1) {
            logger.error("DistrictFacetComponent.districtMap not initialised!");
        }

        //Storage for parcels
        ParcelList parcels = new ParcelList(1);

        //Storage for incomplete parcels
        BuildingQueue buildingQueue = new BuildingQueue();
        RoadQueue roadQueue = new RoadQueue();

        //Add the name tag
        NameTagComponent nameTagComponent = new NameTagComponent();
        nameTagComponent.text = settlementComponent.name;
        nameTagComponent.textColor = Color.CYAN;
        nameTagComponent.yOffset = 20;
        nameTagComponent.scale = 20;

        //population growth
        MarketSubscriberComponent populationSubscriberComponent = new MarketSubscriberComponent(1);
        populationSubscriberComponent.productStorage = settlementEntity;
        populationSubscriberComponent.consumptionStorage = settlementEntity;
        populationSubscriberComponent.productionInterval = 500;
        populationSubscriberComponent.production.put(populationComponent.popResourceType, Math.round(cultureComponent.growthRate));

        NetworkComponent networkComponent = new NetworkComponent();
        networkComponent.replicateMode = NetworkComponent.ReplicateMode.ALWAYS;
        settlementEntity.addComponent(locationComponent);
        settlementEntity.addComponent(settlementComponent);
        settlementEntity.addComponent(districtGrid);
        settlementEntity.addComponent(cultureComponent);
        settlementEntity.addComponent(populationComponent);
        settlementEntity.addComponent(nameTagComponent);
        settlementEntity.addComponent(regionEntitiesComponent);
        settlementEntity.addComponent(parcels);
        settlementEntity.addComponent(buildingQueue);
        settlementEntity.addComponent(roadQueue);
        settlementEntity.addComponent(new ActiveSettlementComponent());
        settlementEntity.addComponent(networkComponent);
        settlementEntity.addComponent(populationSubscriberComponent);
        settlementEntity.setAlwaysRelevant(true);

        //add region entities
        getSurroundingRegions(settlementEntity);

        //Hook into the economy module's MarketUpdaterSystem
        settlementEntity.send(new SubscriberRegistrationEvent());

        return settlementEntity;
    }

    /**
     * Adds the region entities within city reach to the RegionEntitiesComponent of a settlement
     */
    private void getSurroundingRegions(EntityRef settlement) {
        RegionEntitiesComponent regionEntitiesComponent = settlement.getComponent(RegionEntitiesComponent.class);
        ParcelList parcelList = settlement.getComponent(ParcelList.class);
        LocationComponent locationComponent = settlement.getComponent(LocationComponent.class);
        Vector2i pos = new Vector2i(locationComponent.getLocalPosition().x(),
                locationComponent.getLocalPosition().z());
        float radius = parcelList.cityRadius;
        int size = (Math.round(radius / 32) >= 1) ? Math.round(radius / 32) : 1;
        Rect2i settlementRectArea = Rect2i.createFromMinAndMax(-size, -size, size, size);
        Circle settlementCircle = new Circle(pos.toVector2f(), radius);
        Vector2i regionWorldPos = new Vector2i();

        for (BaseVector2i regionPos : settlementRectArea.contents()) {
            regionWorldPos.set(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            if (settlementCircle.contains(regionWorldPos)) {
                EntityRef region = regionEntityManager.getNearest(regionWorldPos);
                if (region != null && region.hasComponent(UnassignedRegionComponent.class)) {
                    LocationComponent location = region.getComponent(LocationComponent.class);
                    Vector2i position = new Vector2i(location.getWorldPosition().x(), location.getWorldPosition().z());
                    regionEntitiesComponent.regionEntities.put(position.toString(), region);
                    region.send(new AssignRegionEvent());
                }
            }
        }
        settlement.saveComponent(regionEntitiesComponent);
    }

    /**
     * Currently only checks whether the area has enough flat spots.
     */
    private boolean checkBuildArea(EntityRef siteRegion) {
        LocationComponent siteLocation = siteRegion.getComponent(LocationComponent.class);
        Vector2i pos = new Vector2i(siteLocation.getLocalPosition().x(), siteLocation.getLocalPosition().z());
        int unusableRegionsCount = 0;
        Rect2i settlementRectArea = Rect2i.createFromMinAndMax(-3, -3, 3, 3);
        Circle settlementCircle = new Circle(pos.toVector2f(), settlementMaxRadius);

        for (BaseVector2i regionPos : settlementRectArea.contents()) {
            Vector2i regionWorldPos = new Vector2i(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            if (settlementCircle.contains(regionWorldPos)) {
                EntityRef region = regionEntityManager.getNearest(regionWorldPos);
                if (region != null && region.hasComponent(RoughnessFacetComponent.class)) {
                    if (region.getComponent(RoughnessFacetComponent.class).meanDeviation > SettlementConstants.MAX_BUILDABLE_ROUGHNESS) {
                        unusableRegionsCount++;
                    }
                }
            }
        }

        return unusableRegionsCount < SettlementConstants.NEEDED_USABLE_REGIONS_FOR_CITY_SPAWN;
    }


    public void build(EntityRef settlement) {
        BuildingQueue buildingQueue = settlement.getComponent(BuildingQueue.class);
        CultureComponent cultureComponent = settlement.getComponent(CultureComponent.class);
        ParcelList parcelList = settlement.getComponent(ParcelList.class);
        Set<DynParcel> removedParcels = new HashSet<>();
        Set<DynParcel> parcelsInQueue = buildingQueue.buildingQueue;


        for (DynParcel dynParcel : parcelsInQueue) {
            Rect2i expandedParcel = dynParcel.shape.expand(SettlementConstants.MAX_TREE_RADIUS, SettlementConstants.MAX_TREE_RADIUS);
            if (!treeRemovalSystem.removeTreesInRegions(expandedParcel)) {
                continue;
            }

            if (constructer.buildParcel(dynParcel, settlement, cultureComponent)) {
                removedParcels.add(dynParcel);
            }
        }
        for (DynParcel dynParcel : removedParcels) {
            parcelList.addParcel(dynParcel);
        }
        parcelsInQueue.removeAll(removedParcels);

        settlement.saveComponent(buildingQueue);
        settlement.saveComponent(parcelList);
    }

    public void growSettlement(EntityRef settlement) {
        if (blockBufferSystem.getBlockBufferSize() > SettlementConstants.BLOCKBUFFER_SIZE) {
            return;
        }
        DistrictFacetComponent districtFacetComponent = settlement.getComponent(DistrictFacetComponent.class);
        PopulationComponent populationComponent = settlement.getComponent(PopulationComponent.class);
        ParcelList parcels = settlement.getComponent(ParcelList.class);
        BuildingQueue buildingQueue = settlement.getComponent(BuildingQueue.class);
        RoadQueue roadQueue = settlement.getComponent(RoadQueue.class);
        LocationComponent locationComponent = settlement.getComponent(LocationComponent.class);
        NameTagComponent nameTagComponent = settlement.getComponent(NameTagComponent.class);
        CultureComponent cultureComponent = settlement.getComponent(CultureComponent.class);

        int maxIterations = 500;
        int buildingSpawned = 0;
        List<String> zones = new ArrayList<>(buildingManager.getZones());
        Map<String, List<Vector2i>> minMaxSizes = buildingManager.getMinMaxSizePerZone();

        if (populationComponent == null) {
            logger.error("No population found or was uninitialised!");
            return;
        }
        if (cultureComponent == null) {
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

        Vector3i center = new Vector3i(locationComponent.getLocalPosition());

        for (String zone : zones) {
            //Checks if the demand for a building of that zone is enough
            CheckBuildingSpawnPreconditionsEvent preconditionsEvent = new CheckBuildingSpawnPreconditionsEvent(zone);
            settlement.send(preconditionsEvent);
            if (!preconditionsEvent.isHandled) {
                preconditionsEvent.check = true;
            }
            while (cultureComponent.getBuildingNeedsForZone(zone) * populationComponent.populationSize - parcels.areaPerZone.getOrDefault(zone, 0) > minMaxSizes.get(zone).get(0).x * minMaxSizes.get(zone).get(0).y
                    && buildingSpawned < SettlementConstants.MAX_BUILDINGSPAWN && preconditionsEvent.check) {
                Optional<DynParcel> parcelOptional = placeParcel(center, zone, parcels, buildingQueue, districtFacetComponent, maxIterations);
                //Grow settlement radius if no valid area was found
                if (!parcelOptional.isPresent() && parcels.cityRadius < SettlementConstants.SETTLEMENT_RADIUS) {
                    parcels.cityRadius += SettlementConstants.BUILD_RADIUS_INTERVALL;
                    //Add region entities of the now bigger zone
                    getSurroundingRegions(settlement);
                    break;
                } else if (!parcelOptional.isPresent()) {
                    break;
                }
                //TODO Maybe send an event here?
                DynParcel parcel = parcelOptional.get();
                buildingQueue.buildingQueue.add(parcel);

                buildingSpawned++;
            }
            buildingSpawned = 0;
        }

        //Grow population
        for (String residentialZone : cultureComponent.residentialZones) {
            populationComponent.capacity += parcels.areaPerZone.getOrDefault(residentialZone, 0);
        }


        // TODO: Add more roads here
        Vector3f road = locationComponent.getWorldPosition();
        road = road.add(new Vector3f(settlementMaxRadius, 0, settlementMaxRadius));
        Rect2i roadRect = Rect2i.createFromMinAndSize(new Vector2i(road.x, road.z), new Vector2i(10, 5));
        Set<Rect2i> roadSet = new HashSet<>();
        roadSet.add(roadRect);
        RoadParcel parcel = new RoadParcel(roadSet);
        roadQueue.roadQueue.add(parcel);
        logger.info("Added the empty road parcel {}", parcel);

        roadQueue.roadQueue.addAll(calculateRoadParcels(settlement));

        //Note: Saving of the actual added parcels to the parcel list happens when they are successfully build in the build() method
        //This is due to ensuring that changes made while constructing are added

        settlement.saveComponent(nameTagComponent);
        settlement.saveComponent(populationComponent);
        settlement.saveComponent(buildingQueue);
        settlement.saveComponent(parcels);
        settlement.send(new SettlementGrowthEvent());
    }

    private Set<RoadParcel> calculateRoadParcels(EntityRef sourceSettlement) {
        Vector3f sourceSite = sourceSettlement.getComponent(LocationComponent.class).getWorldPosition();
        Vector2i source = new Vector2i(sourceSite.x, sourceSite.z);
        SettlementsCacheComponent container = settlementEntities.getComponent(SettlementsCacheComponent.class);
        for (String vector2iString : container.settlementEntities.keySet()) {
            Vector2i dest = Toolbox.stringToVector2i(vector2iString);
            if (dest.equals(source)) {
                continue;
            }

            logger.info("Building road from {} to {}", source, dest);
        }

        /*
        LocationComponent locationComponent = sourceSettlement.getComponent(LocationComponent.class);
        Vector3f source = locationComponent.getWorldPosition();
        Set<RoadParcel> result = new HashSet<>();

        for (EntityRef destinationSettlement : entityManager.getEntitiesWith(Settlement.class)) {
            if (destinationSettlement.equals(sourceSettlement)) {
                continue;
            }

            LocationComponent destinationLocation = destinationSettlement.getComponent(LocationComponent.class);
            Vector3f destination = destinationLocation.getWorldPosition();

            Vector3f direction = destination.sub(source).normalize();

            Set<Vector3f> path = new HashSet<>();
            for (int x = (int) source.x; x < destination.x; x++) {
                float z = (direction.z / direction.x) * (x - source.x) + destination.z;
                float y = (direction.y / direction.x) * (x - source.x) + destination.y;

                path.add(new Vector3f(x, y, z));
            }

            result.add(new RoadParcel(path));

        }*/


        return new HashSet<>();
    }

    public void buildRoads(EntityRef sourceSettlement) {
        RoadQueue roadQueue = sourceSettlement.getComponent(RoadQueue.class);
        ParcelList parcelList = sourceSettlement.getComponent(ParcelList.class);
        Set<RoadParcel> removedParcels = new HashSet<>();
        Set<RoadParcel> parcelsInQueue = roadQueue.roadQueue;


        for (RoadParcel parcel : parcelsInQueue) {
            logger.info("Processing parcel {} with rects {}", parcel, parcel.getRects());
            Set<Rect2i> expandedParcels = parcel.expand(SettlementConstants.MAX_TREE_RADIUS, SettlementConstants.MAX_TREE_RADIUS);
            for (Rect2i region : expandedParcels) {
                treeRemovalSystem.removeTreesInRegions(region);
            }

            if (constructer.buildRoadParcel(parcel, sourceSettlement)) {
                removedParcels.add(parcel);
                logger.info("Removed parcel {} from pending parcels", parcel);
            }
        }
        for (RoadParcel parcel : removedParcels) {
            parcelList.addParcel(parcel);
        }
        parcelsInQueue.removeAll(removedParcels);

        sourceSettlement.saveComponent(roadQueue);
        sourceSettlement.saveComponent(parcelList);

        logger.info("Building complete, new road queue: {} \n new parcel list: {}", roadQueue, parcelList);
    }

    private Optional<DynParcel> placeParcel(Vector3i center, String zone, ParcelList parcels,
                                            BuildingQueue buildingQueue, DistrictFacetComponent districtFacetComponent, int maxIterations) {
        int iter = 0;
        Map<String, List<Vector2i>> minMaxSizes = buildingManager.getMinMaxSizePerZone();
        int minSize = (minMaxSizes.get(zone).get(0).getX() < minMaxSizes.get(zone).get(0).getY())
                ? minMaxSizes.get(zone).get(0).getX() : minMaxSizes.get(zone).get(0).getY();
        int maxSize = (minMaxSizes.get(zone).get(1).getX() < minMaxSizes.get(zone).get(1).getY())
                ? minMaxSizes.get(zone).get(1).getX() : minMaxSizes.get(zone).get(1).getY();
        int sizeX = rng.nextInt(minSize, maxSize);
        int sizeY = rng.nextInt(minSize, maxSize);
        Rect2i shape;
        Orientation orientation = Orientation.NORTH.getRotated(90 * rng.nextInt(5));
        Vector2i rectPosition = new Vector2i();
        float radius;
        do {
            iter++;
            float angle = rng.nextFloat(0, 360);
            //Subtract the maximum tree radius (13) from the parcel radius -> Some bigger buildings still could cause issues
            radius = rng.nextFloat(0, parcels.cityRadius - 32);
            rectPosition.set((int) Math.round(radius * Math.sin((double) angle) + center.x()),
                    (int) Math.round(radius * Math.cos((double) angle)) + center.z());
            shape = Rect2i.createFromMinAndSize(rectPosition.x(), rectPosition.y(), sizeX, sizeY);
        } while ((!parcels.isNotIntersecting(shape) || !buildingQueue.isNotIntersecting(shape)
                || !(districtFacetComponent.getDistrict(rectPosition.x(), rectPosition.y()).isValidType(zone)) || !checkIfTerrainIsBuildable(shape))
                && iter != maxIterations);
        //Keep track of the most distant building to the center
        if (radius > parcels.builtUpRadius) {
            parcels.builtUpRadius = radius;
        }
        if (iter == maxIterations) {
            return Optional.empty();
        } else {
            return Optional.of(new DynParcel(shape, orientation, zone, center.y()));
        }
    }

    private boolean checkIfTerrainIsBuildable(Rect2i area) {
        List<EntityRef> regions = regionEntityManager.getRegionsInArea(area);
        if (regions.isEmpty()) {
            //logger.debug("No regions found in area " + area.toString());
            return false;
        }
        for (EntityRef region : regions) {
            RoughnessFacetComponent roughnessFacetComponent = region.getComponent(RoughnessFacetComponent.class);
            ResourceFacetComponent resourceFacetComponent = region.getComponent(ResourceFacetComponent.class);
            if (roughnessFacetComponent == null) {
                logger.error("No RoughnessFacetComponent found for region");
                return false;
            }
            if (roughnessFacetComponent.meanDeviation > SettlementConstants.MAX_BUILDABLE_ROUGHNESS) {
                return false;
            }
            if (resourceFacetComponent.getResourceSum(ResourceType.WATER.toString()) != 0) {
                return false;
            }
        }
        return true;
    }


}
