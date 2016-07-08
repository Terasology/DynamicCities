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
import org.terasology.dynamicCities.buildings.BuildingQueue;
import org.terasology.dynamicCities.construction.Construction;
import org.terasology.dynamicCities.minimap.DistrictOverlay;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.parcels.Zone;
import org.terasology.dynamicCities.population.Population;
import org.terasology.dynamicCities.population.PopulationConstants;
import org.terasology.dynamicCities.region.RegionEntityManager;
import org.terasology.dynamicCities.region.components.RegionEntities;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.UnassignedRegionComponent;
import org.terasology.dynamicCities.region.events.AssignRegionEvent;
import org.terasology.dynamicCities.settlements.components.ActiveSettlementComponent;
import org.terasology.dynamicCities.settlements.components.DistrictFacetComponent;
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
import org.terasology.logic.players.MinimapSystem;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.*;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.Color;
import org.terasology.utilities.procedural.Noise;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.generation.Border3D;

import java.util.HashSet;
import java.util.Set;

/**
 * Current tasks: Rewrite site to settlement conversion: Check sides and remove sitecomponent if all 
 */

@Share(value = SettlementEntityManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class SettlementEntityManager extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    private SettlementEntities settlementEntities;

    @In
    private RegionEntityManager regionEntityManager;

    @In
    private Construction constructer;

    @In
    private MinimapSystem minimapSystem;

    private int minDistance = 500;
    private RegionEntities regionEntitiesStore;
    private int settlementMaxRadius = 96;
    private int counter = 50;
    private int timer = 0;
    private Noise randNumGen;

    private Logger logger = LoggerFactory.getLogger(SettlementEntityManager.class);
    @Override
    public void postBegin() {
        settlementEntities = new SettlementEntities();
        regionEntitiesStore = regionEntityManager.getRegionEntities();
        randNumGen = new WhiteNoise(regionEntitiesStore.hashCode() & 0x921233);
        minimapSystem.addOverlay(new DistrictOverlay(this));
    }

    @Override
    public void update(float delta) {
        counter--;
        timer++;
        if (counter != 0) {
            return;
        }
        Iterable<EntityRef> uncheckedSiteRegions = entityManager.getEntitiesWith(Site.class);
        for (EntityRef siteRegion : uncheckedSiteRegions) {
            boolean checkDistance = checkMinDistance(siteRegion);
            boolean checkBuildArea = checkBuildArea(siteRegion);
            if (checkDistance && regionEntitiesStore.checkSidesLoadedNear(siteRegion)
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
        counter = 100;
    }

    @ReceiveEvent(components = {ActiveSettlementComponent.class})
    public void registerSettlement(SettlementRegisterEvent event, EntityRef settlement) {
        settlementEntities.add(settlement);
    }


    public boolean checkMinDistance(EntityRef siteRegion) {
        Vector3f sitePos = siteRegion.getComponent(LocationComponent.class).getLocalPosition();
        Vector2i pos = new Vector2i(sitePos.x(), sitePos.z());

        for (String vector2iString : settlementEntities.getMap().keySet()) {
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
        for (String vector2iString : settlementEntities.getMap().keySet()) {
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

        Vector2i regionCenter = new Vector2i(siteRegion.getComponent(LocationComponent.class).getLocalPosition().x(),
                                             siteRegion.getComponent(LocationComponent.class).getLocalPosition().z());
        Site site = siteRegion.getComponent(Site.class);
        LocationComponent locationComponent = siteRegion.getComponent(LocationComponent.class);
        Population population = new Population(site.getPopulation());

        //add surrounding regions to settlement
        RegionEntities regionEntities = new RegionEntities();
        getSurroundingRegions(regionCenter, regionEntities);

        //Create the district facet and DistrictTypeMap
        Region3i region = Region3i.createFromCenterExtents(new Vector3i(locationComponent.getLocalPosition()), SettlementConstants.SETTLEMENT_RADIUS);
        Border3D border = new Border3D(0, 0, 0);
        DistrictFacetComponent districtGrid = new DistrictFacetComponent(region, border, SettlementConstants.DISTRICT_GRIDSIZE, site.hashCode());

        //districtGrid.districtTypeMapTemp.put(districtGrid.getWorld(regionCenter), DistrictType.CITYCENTER.toString());
        if (districtGrid.districtMap.size() < 1) {
            logger.error("DistrictFacetComponent.districtMap not initialised!");
        }
        ParcelList parcels = new ParcelList(1);

        BuildingQueue buildingQueue = new BuildingQueue();

        NameTagComponent settlementName = new NameTagComponent();
        settlementName.text = "testcity regions: " + regionEntities.regionEntities.size() + " " + population.populationSize;

        settlementName.textColor = Color.CYAN;
        settlementName.yOffset = 20;
        settlementName.scale = 20;


        EntityRef settlement = entityManager.create(locationComponent, districtGrid,
                population, settlementName, regionEntities, parcels, buildingQueue, new ActiveSettlementComponent());


        return settlement;
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



    public void build(EntityRef settlement) {
        BuildingQueue buildingQueue = settlement.getComponent(BuildingQueue.class);

        Set<DynParcel> removedParcels = new HashSet<>();
        Set<DynParcel> parcels = buildingQueue.buildingQueue;

        for (DynParcel dynParcel : parcels) {
            if (constructer.buildParcel(dynParcel, settlement)) {
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
        if (population == null) {
            return;
        }
        if (parcels == null) {
            return;
        }
        if (buildingQueue == null) {
            return;
        }
        if (locationComponent == null) {
            return;
        }
        if (nameTagComponent == null) {
            return;
        }
        if (districtFacetComponent == null || districtFacetComponent.districtMap == null || districtFacetComponent.districtTypeMap == null) {
            logger.error("No DistrictFacetComponent found or was uninitialised!");
            return;
        }
        Vector3i center = new Vector3i(locationComponent.getLocalPosition());

        Random rng = new FastRandom(locationComponent.getLocalPosition().hashCode() ^ 0x1496327 ^ time);

        float minRadius = parcels.minBuildRadius;
        float maxRadius = parcels.maxBuildRadius;
        int maxIterations = 40;
        int buildingSpawned = 0;
        int[] zoneArea = new int[5];
        int[] buildingNeeds = new int[5];
        Zone[] zones = new Zone[5];
        Vector2i[] maxMinSizes = new Vector2i[5];

        class  Updater {
            public void update() {
                zoneArea[0] = parcels.clericalArea;
                zoneArea[1] = parcels.commercialArea;
                zoneArea[2] = parcels.governmentalArea;
                zoneArea[3] = parcels.militaryArea;
                zoneArea[4] = parcels.residentialArea;
            }
        }
        Updater updater = new Updater();
        updater.update();

        population.grow();

        buildingNeeds[0] = population.getClericalNeed();
        buildingNeeds[1] = population.getCommercialNeed();
        buildingNeeds[2] = population.getGovernmentalNeed();
        buildingNeeds[3] = population.getMilitaryNeed();
        buildingNeeds[4] = population.getResidentialNeed();

        zones[0] = Zone.CLERICAL;
        zones[1] = Zone.COMMERCIAL;
        zones[2] = Zone.GOVERNMENTAL;
        //Set this one to military ones it is properly integrated:
        zones[3] = Zone.RESIDENTIAL;
        zones[4] = Zone.RESIDENTIAL;

        maxMinSizes[0] = population.getMaxMinClerical();
        maxMinSizes[1] = population.getMaxMinCommercial();
        maxMinSizes[2] = population.getMaxMinGovernmental();
        maxMinSizes[3] = population.getMaxMinMilitary();
        maxMinSizes[4] = population.getMaxMinResidential();

        /**
         * TODO: Generate more random rectangles (currently only squares).
         * TODO: Integrate military zone here. Currently it skips i == 3
         * TODO: Integrate a flag when there aren't any building spots found after a certain amount of time
         */
        for (int i = 0; i < zones.length; i++) {
            while (buildingNeeds[i] - zoneArea[i] > maxMinSizes[i].y() && buildingSpawned < SettlementConstants.MAX_BUILDINGSPAWN && i != 3) {
                int iter = 0;
                int size = Math.round(TeraMath.fastAbs(rng.nextInt((int) Math.round(Math.sqrt((double) maxMinSizes[i].y())),
                        (int) Math.round(Math.sqrt(maxMinSizes[i].x())))));
                Rect2i shape = Rect2i.EMPTY;
                Orientation orientation = Orientation.NORTH.getRotated(90 * rng.nextInt(5));
                Vector2i rectPosition = new Vector2i();
                float radius = 0;
                int district = 0;
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
                        || !(districtFacetComponent.getDistrict(rectPosition.x(), rectPosition.y()).isValidType(zones[i])))
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

                DynParcel newParcel = new DynParcel(shape, orientation, zones[i], Math.round(locationComponent.getLocalPosition().y()));
                parcels.addParcel(newParcel);
                buildingQueue.buildingQueue.add(newParcel);
                updater.update();
                buildingSpawned++;
            }
        }

        nameTagComponent.text =  nameTagComponent.text.replaceAll(Float.toString((population.populationSize - PopulationConstants.GROWTH_RATE)), Float.toString(population.populationSize));
        settlement.saveComponent(parcels);
        settlement.saveComponent(nameTagComponent);
        settlement.saveComponent(population);
        settlement.saveComponent(buildingQueue);
    }

    public SettlementEntities getSettlementEntities() {
        return settlementEntities;
    }

}
