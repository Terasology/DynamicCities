// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.joml.RoundingMode;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.commonworld.Orientation;
import org.terasology.commonworld.geom.CircleUtility;
import org.terasology.dynamicCities.buildings.BuildingManager;
import org.terasology.dynamicCities.buildings.BuildingQueue;
import org.terasology.dynamicCities.construction.BlockBufferSystem;
import org.terasology.dynamicCities.construction.Construction;
import org.terasology.dynamicCities.construction.TreeRemovalSystem;
import org.terasology.dynamicCities.districts.DistrictManager;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.dynamicCities.parcels.RoadStatus;
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
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.dynamicCities.settlements.components.ActiveSettlementComponent;
import org.terasology.dynamicCities.settlements.components.DistrictFacetComponent;
import org.terasology.dynamicCities.settlements.events.CheckSiteSuitabilityEvent;
import org.terasology.dynamicCities.settlements.events.CheckZoneNeededEvent;
import org.terasology.dynamicCities.settlements.events.SettlementFilterResult;
import org.terasology.dynamicCities.settlements.events.SettlementGrowthEvent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.dynamicCities.sites.SiteComponent;
import org.terasology.economy.components.MarketSubscriberComponent;
import org.terasology.economy.events.SubscriberRegistrationEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.nameTags.NameTagComponent;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.utilities.random.FastRandom;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.time.WorldTimeEvent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.joml.geom.Circlef;
import org.terasology.logic.players.MinimapSystem;
import org.terasology.namegenerator.town.TownNameProvider;
import org.terasology.nui.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;

@Share(SettlementEntityManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class SettlementEntityManager extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(SettlementEntityManager.class);

    @In
    private EntityManager entityManager;

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

    private int minDistance = 500;
    private int settlementMaxRadius = 150;
    private int cyclesLeft = 2; // 1 cycle = approx. 20 seconds
    private Random rng;
    private Multimap<String, String> roadCache = MultimapBuilder.hashKeys().hashSetValues().build();

    @Override
    public void postBegin() {
        long seed = regionEntityManager.hashCode() & 0x921233;
        rng = new FastRandom(seed);

    }

    @ReceiveEvent
    public void onWorldTimeEvent(WorldTimeEvent worldTimeEvent, EntityRef entityRef) {
        if (!settlementCachingSystem.isInitialised()) {
            return;
        }

        cyclesLeft--;
        if (cyclesLeft != 0) {
            return;
        }
        Iterable<EntityRef> uncheckedSiteRegions = entityManager.getEntitiesWith(SiteComponent.class);
        for (EntityRef siteRegion : uncheckedSiteRegions) {
            CheckSiteSuitabilityEvent checkSiteSuitabilityEvent = new CheckSiteSuitabilityEvent();
            siteRegion.send(checkSiteSuitabilityEvent);
            switch (checkSiteSuitabilityEvent.getResult()) {
                case SUITABLE:
                    EntityRef newSettlement = createSettlement(siteRegion);
                    newSettlement.send(new SettlementRegisterEvent());
                    siteRegion.removeComponent(SiteComponent.class);
                    break;

                case UNSUITABLE:
                    siteRegion.removeComponent(SiteComponent.class);
                    break;

                case UNKNOWN:
                    // not enough information, do nothing
                    break;
            }
        }
        Iterable<EntityRef> activeSettlements = entityManager.getEntitiesWith(BuildingQueue.class);
        for (EntityRef settlement : activeSettlements) {
            growSettlement(settlement);
            build(settlement);
            buildRoads(settlement);
        }
        cyclesLeft = 2;
    }

    /**
     * Checks the provided region entity for suitability as a settlement in response to a CheckSiteSuitabilityEvent
     *
     * The default behavior will check that the region satisfies default distance and build area thresholds, and also
     * ensures that the region's "sides" have been loaded. This can be extended by registering a new event handler
     * with the same signature and annotation, but a lower priority. It can also be disabled completely by using a
     * higher priority and consuming the event in your handler.
     *
     * @param event
     * @param siteRegion
     */
    @ReceiveEvent(components = SiteComponent.class)
    public void filterSettlement(CheckSiteSuitabilityEvent event, EntityRef siteRegion) {
        boolean checkDistance = checkMinDistance(siteRegion);
        boolean checkBuildArea = checkBuildArea(siteRegion);
        if (checkDistance && regionEntityManager.checkSidesLoadedNear(siteRegion) && checkBuildArea) {
            event.setResult(SettlementFilterResult.SUITABLE);
        } else if (!checkDistance || !checkBuildArea) {
            event.setResult(SettlementFilterResult.UNSUITABLE);
        }
    }

    /**
     * Checks whether the settlement needs the given zone
     *
     * The default behavior checks the culture need for the zone, multiplies that by the population, then subtracts
     * the areaPerZone for the given zone according to the ParcelList. If that "allowed zone area" is greater than
     * the minimum area of all buildings for that zone, the check passes.
     *
     * @param event
     * @param settlement
     * @param culture
     * @param population
     * @param parcels
     */
    @ReceiveEvent
    public void checkZoneNeeded(CheckZoneNeededEvent event, EntityRef settlement, CultureComponent culture,
                                PopulationComponent population,
                                ParcelList parcels) {
        Map<String, List<Vector2i>> minMaxSizes = buildingManager.getMinMaxSizePerZone();
        double minimumZoneArea = minMaxSizes.get(event.zone).get(0).x * minMaxSizes.get(event.zone).get(0).y;
        double need = culture.getBuildingNeedsForZone(event.zone);
        double allowedZoneArea = need * population.populationSize - parcels.areaPerZone.getOrDefault(event.zone, 0);
        event.needed = allowedZoneArea > minimumZoneArea;
    }

    public boolean checkMinDistance(EntityRef siteRegion) {
        Vector3fc sitePos = siteRegion.getComponent(LocationComponent.class).getLocalPosition();
        Vector2i pos = new Vector2i(sitePos.x(), sitePos.z(), RoundingMode.FLOOR);
        SettlementsCacheComponent container = settlementCachingSystem.getSettlementCacheEntity().getComponent(SettlementsCacheComponent.class);
        for (Vector2i loc : container.settlementEntities.keySet()) {
            if (pos.distance(loc) < minDistance) {
                return false;
            }
        }
        return true;
    }

    public boolean checkMinDistanceCell(Vector2ic pos) {
        if (!regionEntityManager.cellIsLoaded(pos)) {
            return true;
        }

        SettlementsCacheComponent container = settlementCachingSystem.getSettlementCacheEntity().getComponent(SettlementsCacheComponent.class);
        for (Vector2ic activePos : container.settlementEntities.keySet()) {
            if (pos.distance(activePos) < minDistance - settlementMaxRadius) {
                return false;
            }
        }
        return true;
    }
//
//    public boolean checkMinDistanceCell(String posString) {
//        return checkMinDistanceCell(Toolbox.stringToVector2i(posString));
//    }

    /**
     * Check if a point lies in any settlement
     *
     * @param pos Point to be tested
     * @return True if pos is not inside any settlement
     */
    public boolean checkOutsideAllSettlements(Vector2ic pos) {
        SettlementsCacheComponent container = settlementCachingSystem.getSettlementCacheEntity().getComponent(SettlementsCacheComponent.class);
        for (Vector2ic activePos : container.settlementEntities.keySet()) {
            EntityRef settlement = container.settlementEntities.get(new Vector2i(activePos));
            ParcelList parcels = settlement.getComponent(ParcelList.class);

            if (pos.distance(activePos) < parcels.builtUpRadius) {
                return false;
            }
        }
        return true;
    }

    EntityRef createSettlement(EntityRef siteRegion) {
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
        BlockRegion region = new BlockRegion(new Vector3i(locationComponent.getLocalPosition(), RoundingMode.FLOOR)).expand(new Vector3i(SettlementConstants.SETTLEMENT_RADIUS));
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
        Vector3fc loc = locationComponent.getLocalPosition();
        Vector2i pos = new Vector2i(loc.x(), loc.z(), RoundingMode.FLOOR);
        float radius = parcelList.cityRadius;
        int size = Math.max(Math.round(radius / 32), 1);
        BlockArea settlementRectArea = new BlockArea(-size, -size, size, size);
        Circlef settlementCircle = new Circlef(pos.x, pos.y, radius);
        Vector2i regionWorldPos = new Vector2i();

        for (Vector2ic regionPos : settlementRectArea) {
            regionWorldPos.set(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            if (new Vector2f(settlementCircle.x, settlementCircle.y).distance(regionWorldPos.x, regionWorldPos.y) < settlementCircle.r) {
                EntityRef region = regionEntityManager.getNearest(regionWorldPos);
                if (region != null && region.hasComponent(UnassignedRegionComponent.class)) {
                    LocationComponent location = region.getComponent(LocationComponent.class);
                    Vector3f worldPos = location.getWorldPosition(new Vector3f());
                    Vector2i position = new Vector2i(worldPos.x(), worldPos.z(), RoundingMode.FLOOR);
                    regionEntitiesComponent.regionEntities.put(position, region);
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
        Vector2i pos = new Vector2i(siteLocation.getLocalPosition().x(), siteLocation.getLocalPosition().z(), RoundingMode.FLOOR);
        int unusableRegionsCount = 0;
        BlockArea settlementRectArea = new BlockArea(-3, -3, 3, 3);
        Circlef settlementCircle = new Circlef(pos.x, pos.y, settlementMaxRadius);

        for (Vector2ic regionPos : settlementRectArea) {
            Vector2i regionWorldPos = new Vector2i(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);
            if (CircleUtility.contains(settlementCircle, regionWorldPos.x, regionWorldPos.y)) {
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
        ParcelList parcelList = settlement.getComponent(ParcelList.class);
        Set<DynParcel> removedParcels = new HashSet<>();
        Set<DynParcel> parcelsInQueue = buildingQueue.buildingQueue;

        for (DynParcel dynParcel : parcelsInQueue) {
            BlockArea expandedParcel = dynParcel.shape.expand(SettlementConstants.MAX_TREE_RADIUS, SettlementConstants.MAX_TREE_RADIUS, new BlockArea(BlockArea.INVALID));
            if (!treeRemovalSystem.removeTreesInRegions(expandedParcel)) {
                continue;
            }

            if (constructer.buildParcel(dynParcel, settlement)) {
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

    /**
     * Attempts to place new parcels for the settlement as needed
     *
     * For each zone type, this settlement sends a {@link CheckZoneNeededEvent}. If the event's
     * `needed` field is true, a parcel is placed for that zone if possible. This method will then continue to place
     * parcels for that zone as needed, sending a new event each time until one finally returns false or until the
     * SettlementEntityManager is unable to place a needed parcel. In that case, the city's radius is increased and
     * process for that particular zone type stops.
     *
     * Also inflates the population capacity based on the area of each residential zone in the settlement
     *
     * @param settlement
     */
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

        Vector3i center = new Vector3i(locationComponent.getLocalPosition(), RoundingMode.FLOOR);

        for (String zone : zones) {
            //Checks if the demand for a building of that zone is enough
            while (buildingSpawned < SettlementConstants.MAX_BUILDINGSPAWN) {
                CheckZoneNeededEvent preconditionsEvent = new CheckZoneNeededEvent(zone);
                settlement.send(preconditionsEvent);
                if (!preconditionsEvent.needed) {
                    break;
                }

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
        /*
          grow population
         */
        for (String residentialZone : cultureComponent.residentialZones) {
            populationComponent.capacity += parcels.areaPerZone.getOrDefault(residentialZone, 0);
        }


        /*
          Create roads between settlements
         */
        SettlementsCacheComponent container = settlementCachingSystem.getSettlementCacheEntity().getComponent(SettlementsCacheComponent.class);
        Vector2f source = new Vector2f(center.x, center.z);
        Vector2f dest = new Vector2f(source);
        float min = Float.MAX_VALUE;
        for (EntityRef entity : container.settlementEntities.values()) {
            if (!settlement.equals(entity)) {
                Vector3fc location = entity.getComponent(LocationComponent.class).getLocalPosition();
                Vector2fc location2D = new Vector2f(location.x(), location.z());
                if (!roadCache.containsEntry(source.toString(), location2D.toString()) && source.distance(location2D) <= min) {
                    dest.set(location2D);
                    min = source.distance(location2D);
                }
            }
        }

        if (!source.equals(dest)) {
            roadQueue.roadQueue.add(calculateRoadParcel(source, dest, center.y));
            roadCache.put(source.toString(), dest.toString());
            roadCache.put(dest.toString(), source.toString());
        }

        //Note: Saving of the actual added parcels to the parcel list happens when they are successfully build in the build() method
        //This is due to ensuring that changes made while constructing are added

        settlement.saveComponent(nameTagComponent);
        settlement.saveComponent(populationComponent);
        settlement.saveComponent(buildingQueue);
        settlement.saveComponent(parcels);
        settlement.send(new SettlementGrowthEvent());
    }

    private RoadParcel calculateRoadParcel(Vector2fc source, Vector2f dest, int height) {
        Vector<RoadSegment> segments = new Vector<>();

        Vector2f diff = new Vector2f(dest.sub(source));
        Vector2f direction = new Vector2f(diff.normalize());

        Vector2f roadStart = source.add(direction.mul(settlementMaxRadius + RoadParcel.MARGIN, new Vector2f()), new Vector2f());
        Vector2f roadEnd = dest.sub(direction.mul(settlementMaxRadius + RoadParcel.MARGIN, new Vector2f()), new Vector2f());

        Vector2f i = new Vector2f(roadStart);

        boolean shouldContinue;
        do {
            Vector2i a = new Vector2i((int) i.x, (int) i.y);
            i.add(direction.mul(RoadParcel.RECT_SIZE, new Vector2f()));
            Vector2i b = new Vector2i((int) i.x, (int) i.y);
            i.sub(direction.mul(RoadParcel.OVERLAP, new Vector2f()));

            // Must calculate actual min and max for the rect
            // Ensure 'a' has lower x
            BlockArea rect = new BlockArea(BlockArea.INVALID);
            if (a.x() > b.x()) {
                Vector2i tmp = b;
                b = a;
                a = tmp;
            }

            if (a.y() < b.y()) {
                rect = new BlockArea(a, b);
            } else {
                rect = new BlockArea(a.x(), b.y(),b.x(), a.y());
            }

            segments.add(new RoadSegment(rect, height, a, b));

            // Because these are floats, equating values will be a problem
            // Instead, we'll make sure their different is below a threshold
            float threshold = 0.0001f;
            Vector2f remaining = new Vector2f(roadEnd.sub(i));
            remaining.normalize();
            shouldContinue = (new Vector2f(remaining)).sub(direction).length() < threshold;
        } while (shouldContinue);

        return new RoadParcel(segments);
    }

    public void buildRoads(EntityRef sourceSettlement) {
        RoadQueue roadQueue = sourceSettlement.getComponent(RoadQueue.class);
        ParcelList parcelList = sourceSettlement.getComponent(ParcelList.class);
        Set<RoadParcel> removedParcels = new HashSet<>();
        Set<RoadParcel> parcelsInQueue = roadQueue.roadQueue;


        for (RoadParcel parcel : parcelsInQueue) {
            Set<BlockArea> expandedParcels = parcel.expand(SettlementConstants.MAX_TREE_RADIUS, SettlementConstants.MAX_TREE_RADIUS);
            for (BlockArea region : expandedParcels) {
                treeRemovalSystem.removeTreesInRegions(region);
            }

            RoadStatus status = constructer.buildRoadParcel(parcel, sourceSettlement);
            if (status == RoadStatus.COMPLETE) {
                removedParcels.add(parcel);
            } else {
                logger.warn("Parcel {} couldn't be completed. Status: {}", parcel, status);
            }
        }
        for (RoadParcel parcel : removedParcels) {
            parcelList.addParcel(parcel);
        }
        parcelsInQueue.removeAll(removedParcels);

        sourceSettlement.saveComponent(roadQueue);
        sourceSettlement.saveComponent(parcelList);
    }

    Optional<DynParcel> placeParcel(Vector3ic center, String zone, ParcelList parcels,
                                    BuildingQueue buildingQueue, DistrictFacetComponent districtFacetComponent, int maxIterations) {
        int iter = 0;
        Map<String, List<Vector2i>> minMaxSizes = buildingManager.getMinMaxSizePerZone();
        int minSize = (minMaxSizes.get(zone).get(0).x() < minMaxSizes.get(zone).get(0).y())
                ? minMaxSizes.get(zone).get(0).x() : minMaxSizes.get(zone).get(0).y();
        int maxSize = (minMaxSizes.get(zone).get(1).x() < minMaxSizes.get(zone).get(1).y())
                ? minMaxSizes.get(zone).get(1).x() : minMaxSizes.get(zone).get(1).y();
        int sizeX = rng.nextInt(minSize, maxSize);
        int sizeY = rng.nextInt(minSize, maxSize);
        BlockArea shape;
        Orientation orientation = Orientation.NORTH.getRotated(90 * rng.nextInt(5));
        float radius;
        ParcelLocationValidator parcelLocationValidator = new ParcelLocationValidator(parcels, buildingQueue, districtFacetComponent,
                zone, regionEntityManager);
        do {
            iter++;
            float angle = rng.nextFloat(0, 360);
            //Subtract the maximum tree radius (13) from the parcel radius -> Some bigger buildings still could cause issues
            radius = rng.nextFloat(0, parcels.cityRadius - 32);
            int x = (int) Math.round(radius * Math.sin(angle)) + center.x();
            int z = (int) Math.round(radius * Math.cos(angle)) + center.z();
            shape = new BlockArea(x, z).setSize(sizeX, sizeY);
        } while (!parcelLocationValidator.isValid(shape) && iter < maxIterations);

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

    static class ParcelLocationValidator {
        private final ParcelList parcels;
        private final BuildingQueue buildingQueue;
        private final DistrictFacetComponent districtFacetComponent;
        private final String zone;
        private final RegionEntityManager regionEntityManager;

        ParcelLocationValidator(ParcelList parcels, BuildingQueue buildingQueue, DistrictFacetComponent districtFacetComponent, String zone, RegionEntityManager regionEntityManager) {
            this.parcels = parcels;
            this.buildingQueue = buildingQueue;
            this.districtFacetComponent = districtFacetComponent;
            this.zone = zone;
            this.regionEntityManager = regionEntityManager;
        }

        private boolean checkIfTerrainIsBuildable(BlockAreac area) {
            List<EntityRef> regions = regionEntityManager.getRegionsInArea(area);
            if (regions.isEmpty()) {
                logger.debug("No regions found in area {}", area);
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

        boolean isValid(BlockAreac shape) {
            // TODO: This no longer short-circuits on the first false, is that a performance problem?
            boolean freeOfOtherParcels = parcels.isNotIntersecting(shape);
            boolean freeOfOtherBuildings = buildingQueue.isNotIntersecting(shape);
            boolean districtAllowsZone = districtFacetComponent.getDistrict(shape.minX(), shape.minY()).isValidType(zone);
            boolean terrainBuildable = checkIfTerrainIsBuildable(shape);
            logger.debug("P:{} B:{} Z:{} T:{}", freeOfOtherBuildings, freeOfOtherBuildings, districtAllowsZone, terrainBuildable);
            return freeOfOtherParcels && freeOfOtherBuildings && districtAllowsZone && terrainBuildable;
        }
    }
}
