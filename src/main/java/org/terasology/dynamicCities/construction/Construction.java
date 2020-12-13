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
package org.terasology.dynamicCities.construction;

import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.management.AssetManager;
import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.Building;
import org.terasology.cities.bldg.BuildingPart;
import org.terasology.cities.bldg.gen.BuildingGenerator;
import org.terasology.cities.deco.Decoration;
import org.terasology.cities.door.Door;
import org.terasology.cities.model.roof.Roof;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.window.Window;
import org.terasology.commonworld.Orientation;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.dynamicCities.buildings.BuildingManager;
import org.terasology.dynamicCities.buildings.GenericBuildingComponent;
import org.terasology.dynamicCities.buildings.components.ChestPositionsComponent;
import org.terasology.dynamicCities.buildings.components.DynParcelRefComponent;
import org.terasology.dynamicCities.buildings.components.ProductionChestComponent;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.buildings.events.OnSpawnDynamicStructureEvent;
import org.terasology.dynamicCities.construction.events.BufferBlockEvent;
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.construction.events.RequestRasterTargetEvent;
import org.terasology.dynamicCities.construction.events.SetBlockEvent;
import org.terasology.dynamicCities.construction.events.SpawnStructureBufferedEvent;
import org.terasology.dynamicCities.decoration.ColumnRasterizer;
import org.terasology.dynamicCities.decoration.DecorationRasterizer;
import org.terasology.dynamicCities.decoration.SingleBlockRasterizer;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.RoadParcel;
import org.terasology.dynamicCities.parcels.RoadStatus;
import org.terasology.dynamicCities.playerTracking.PlayerTracker;
import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.dynamicCities.rasterizer.BufferRasterTarget;
import org.terasology.dynamicCities.rasterizer.RoadRasterizer;
import org.terasology.dynamicCities.rasterizer.doors.DoorRasterizer;
import org.terasology.dynamicCities.rasterizer.doors.SimpleDoorRasterizer;
import org.terasology.dynamicCities.rasterizer.doors.WingDoorRasterizer;
import org.terasology.dynamicCities.rasterizer.parts.HollowBuildingPartRasterizer;
import org.terasology.dynamicCities.rasterizer.parts.RectPartRasterizer;
import org.terasology.dynamicCities.rasterizer.parts.RoundPartRasterizer;
import org.terasology.dynamicCities.rasterizer.parts.StaircaseRasterizer;
import org.terasology.dynamicCities.rasterizer.roofs.ConicRoofRasterizer;
import org.terasology.dynamicCities.rasterizer.roofs.DomeRoofRasterizer;
import org.terasology.dynamicCities.rasterizer.roofs.FlatRoofRasterizer;
import org.terasology.dynamicCities.rasterizer.roofs.HipRoofRasterizer;
import org.terasology.dynamicCities.rasterizer.roofs.PentRoofRasterizer;
import org.terasology.dynamicCities.rasterizer.roofs.RoofRasterizer;
import org.terasology.dynamicCities.rasterizer.roofs.SaddleRoofRasterizer;
import org.terasology.dynamicCities.rasterizer.window.RectWindowRasterizer;
import org.terasology.dynamicCities.rasterizer.window.SimpleWindowRasterizer;
import org.terasology.dynamicCities.rasterizer.window.WindowRasterizer;
import org.terasology.dynamicCities.roads.RoadSegment;
import org.terasology.dynamicCities.settlements.events.CheckBuildingForParcelEvent;
import org.terasology.economy.components.MultiInvStorageComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.JomlUtil;
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.ImmutableVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.structureTemplates.components.SpawnBlockRegionsComponent;
import org.terasology.structureTemplates.interfaces.StructureTemplateProvider;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.structureTemplates.util.BlockRegionUtilities;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockRegion;
import org.terasology.world.block.BlockRegionIterable;
import org.terasology.world.block.BlockRegions;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.ElevationFacet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


//TODO: Move Generators and Templates to the BuildingManager
@Share(value = Construction.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class Construction extends BaseComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private BlockBufferSystem blockBufferSystem;

    @In
    private PlayerTracker playerTracker;

    @In
    private NetworkSystem networkSystem;

    @In
    private BuildingManager buildingManager;

    @In
    private StructureTemplateProvider templateProviderSystem;

    @In
    private EntityManager entityManager;

    @In
    private AssetManager assetManager;

    @In
    private InventoryManager inventoryManager;


    private BlockTheme cityTheme;
    private BlockTheme roadTheme;

    private Block air;
    private Block plant;
    private Block water;
    private Block defaultBlock;
    private int maxMinDeviation = 40;
    private RoadRasterizer roadRasterizer;
    private final List<AbsDynBuildingRasterizer> stdRasterizers = new ArrayList<>();
    private final List<WindowRasterizer> windowRasterizers = new ArrayList<>();
    private final List<DoorRasterizer> doorRasterizers = new ArrayList<>();
    private final List<RoofRasterizer> roofRasterizers = new ArrayList<>();
    private final List<DecorationRasterizer> decorationRasterizers = new ArrayList<>();
    private final List<Block> plantBlocks = new ArrayList<>();
    private Logger logger = LoggerFactory.getLogger(Construction.class);

    private Map<Integer, Integer> segmentCache = new HashMap<>();

    /**
     * Initialises the system and rasterizers with default themes
     */
    public void initialise() {
        cityTheme = BlockTheme.builder(blockManager)
                .register(DefaultBlockType.ROAD_FILL, "CoreAssets:dirt")
                .register(DefaultBlockType.ROAD_SURFACE, "CoreAssets:Gravel")
                .register(DefaultBlockType.LOT_EMPTY, "CoreAssets:dirt")
                .register(DefaultBlockType.BUILDING_WALL, "StructuralResources:StoneBlocks")
                .register(DefaultBlockType.BUILDING_FLOOR, "StructuralResources:StoneBlocksDark")
                .register(DefaultBlockType.BUILDING_FOUNDATION, "CoreAssets:gravel")
                .register(DefaultBlockType.TOWER_STAIRS, "CoreAssets:CobbleStone")
                .register(DefaultBlockType.ROOF_FLAT, "StructuralResources:RoofTilesLarge")
                .register(DefaultBlockType.ROOF_HIP, "StructuralResources:PlanksEvenDark")
                .register(DefaultBlockType.ROOF_SADDLE, "StructuralResources:PlanksEvenDark")
                .register(DefaultBlockType.ROOF_DOME, "CoreAssets:plank")
                .register(DefaultBlockType.ROOF_GABLE, "CoreAssets:plank")
                .register(DefaultBlockType.SIMPLE_DOOR, BlockManager.AIR_ID)
                .register(DefaultBlockType.WING_DOOR, BlockManager.AIR_ID)
                .register(DefaultBlockType.WINDOW_GLASS, BlockManager.AIR_ID)
                .register(DefaultBlockType.TOWER_WALL, "StructuralResources:StoneBlocks")

                // -- requires Fences module
                .registerFamily(DefaultBlockType.FENCE, "Fences:Fence")
                .registerFamily(DefaultBlockType.FENCE_GATE, BlockManager.AIR_ID)  // there is no fence gate :-(
                .registerFamily(DefaultBlockType.TOWER_STAIRS, "CoreAssets:CobbleStone:engine:stair")
                .registerFamily(DefaultBlockType.BARREL, "StructuralResources:Barrel")
                .registerFamily(DefaultBlockType.LADDER, "CoreAssets:Ladder")
                .registerFamily(DefaultBlockType.PILLAR_BASE, "CoreAssets:CobbleStone:StructuralResources:pillarBase")
                .registerFamily(DefaultBlockType.PILLAR_MIDDLE, "CoreAssets:CobbleStone:StructuralResources:pillar")
                .registerFamily(DefaultBlockType.PILLAR_TOP, "CoreAssets:CobbleStone:StructuralResources:pillarTop")
                .registerFamily(DefaultBlockType.TORCH, "CoreAssets:Torch")

                .build();

        if (roadRasterizer == null) {
            roadRasterizer = new RoadRasterizer();
            roadTheme = BlockTheme.builder(blockManager)
                    .register(DefaultBlockType.ROAD_FILL, "CoreAssets:dirt")
                    .register(DefaultBlockType.ROAD_SURFACE, "CoreAssets:Gravel")
                    .build();
        }

        blockManager = CoreRegistry.get(BlockManager.class);
        air = blockManager.getBlock("engine:air");
        water = blockManager.getBlock("CoreAssets:Water");
        plant = blockManager.getBlock("CoreAssets:plant");
        defaultBlock = blockManager.getBlock("CoreAssets:Dirt");

        stdRasterizers.add(new HollowBuildingPartRasterizer(cityTheme, worldProvider));
        stdRasterizers.add(new RectPartRasterizer(cityTheme, worldProvider));
        stdRasterizers.add(new RoundPartRasterizer(cityTheme, worldProvider));
        stdRasterizers.add(new StaircaseRasterizer(cityTheme, worldProvider));

        decorationRasterizers.add(new SingleBlockRasterizer(cityTheme));
        decorationRasterizers.add(new ColumnRasterizer(cityTheme));

        doorRasterizers.add(new SimpleDoorRasterizer(cityTheme));
        doorRasterizers.add(new WingDoorRasterizer(cityTheme));

        windowRasterizers.add(new RectWindowRasterizer(cityTheme));
        windowRasterizers.add(new SimpleWindowRasterizer(cityTheme));

        roofRasterizers.add(new ConicRoofRasterizer(cityTheme));
        roofRasterizers.add(new DomeRoofRasterizer(cityTheme));
        roofRasterizers.add(new FlatRoofRasterizer(cityTheme));
        roofRasterizers.add(new HipRoofRasterizer(cityTheme));
        roofRasterizers.add(new PentRoofRasterizer(cityTheme));
        roofRasterizers.add(new SaddleRoofRasterizer(cityTheme));

        //Register plant blocks
        plantBlocks.add(blockManager.getBlock("CoreAssets:GreenLeaf"));
        plantBlocks.add(blockManager.getBlock("CoreAssets:OakTrunk"));
        plantBlocks.add(blockManager.getBlock("CoreAssets:DarkLeaf"));
        plantBlocks.add(blockManager.getBlock("CoreAssets:PineTrunk"));
        plantBlocks.add(blockManager.getBlock("CoreAssets:BirchTrunk"));
        plantBlocks.add(blockManager.getBlock("CoreAssets:RedLeaf"));
        plantBlocks.add(blockManager.getBlock("CoreAssets:Cactus"));
    }

    /**
     * Setup external rasterizer to be used for the road and it's block theme
     * @param rasterizer to be used for the road
     * @param theme      theme that the rasterizer will use
     */
    public void setRoadRasterizer(RoadRasterizer rasterizer, BlockTheme theme) {
        roadRasterizer = rasterizer;
        roadTheme = theme;
    }

    /**
     * Maybe return a structured data with (int or false) as return value
     *
     * @param area          The area which should be flattened
     * @param defaultHeight A rough estimation of the mean height of the terrain
     * @param filler        The blocktype which should be used to fill up terrain under the mean height
     * @return The height on which it was flattened to
     */
    public int flatten(Rect2i area, int defaultHeight, Block filler) {

        if (area.area() == 0) {
            logger.error("The area which should be flattened is empty!");
            return -9999;
        }
        ElevationFacet elevationFacet = sample(area, defaultHeight);
        int meanHeight = 0;
        Vector3i setPos = new Vector3i();
        Region3i areaRegion = Region3i.createFromMinMax(new Vector3i(area.minX(), defaultHeight - maxMinDeviation, area.minY()),
                new Vector3i(area.maxX(), defaultHeight + maxMinDeviation, area.maxY()));

        if (!worldProvider.isRegionRelevant(areaRegion)) {
            return -9999;
        }
        for (BaseVector2i pos : area.contents()) {
            meanHeight += elevationFacet.getWorld(pos);
        }
        meanHeight /= area.area();

        for (BaseVector2i pos : area.contents()) {
            int y = Math.round(elevationFacet.getWorld(pos));
            if (y <= meanHeight) {
                for (int i = y; i <= meanHeight; i++) {
                    setPos.set(pos.x(), i, pos.y());
                    worldProvider.getWorldEntity().send(new SetBlockEvent(setPos, filler));
                }
            }
            if (y >= meanHeight) {
                for (int i = y; i > meanHeight; i--) {
                    setPos.set(pos.x(), i, pos.y());
                    worldProvider.getWorldEntity().send(new SetBlockEvent(setPos, air));
                }
            }
        }


        return meanHeight;
    }

    public int flatten(Rect2i area, int defaultHeight) {
        return flatten(area, defaultHeight, defaultBlock);
    }

    /**
     * @param area   The area which should be sampled
     * @param height A rough estimation of the mean height of the terrain
     * @return
     */
    public ElevationFacet sample(Rect2i area, int height) {

        Vector3ic minRegionPos = new org.joml.Vector3i(area.minX(), height - maxMinDeviation, area.minY());
        Vector3ic maxRegionPos = new org.joml.Vector3i(area.maxX(), height + maxMinDeviation, area.maxY());
        Border3D border = new Border3D(0, 0, 0);
        ElevationFacet elevationFacet = new ElevationFacet(BlockRegions.createFromMinAndMax(minRegionPos, maxRegionPos), border);
        Vector3i pos = new Vector3i();

        for (int x = area.minX(); x <= area.maxX(); x++) {
            for (int z = area.minY(); z <= area.maxY(); z++) {
                for (int y = height + maxMinDeviation; y >= height - maxMinDeviation; y--) {
                    pos.set(x, y, z);
                    if (worldProvider.getBlock(pos) != air && !worldProvider.getBlock(pos).isLiquid()
                            && !plantBlocks.contains(worldProvider.getBlock(pos))) {
                        elevationFacet.setWorld(x, z, y);
                        break;
                    }
                }
            }
        }
        return elevationFacet;
    }

    /**
     * Gets a random building according to {@link BuildingManager#getRandomBuildingOfZoneForCulture(String, Rect2i, CultureComponent)}
     *
     * @param event
     * @param settlement
     */
    @ReceiveEvent
    public void checkBuildingForParcel(CheckBuildingForParcelEvent event, EntityRef settlement, CultureComponent
            cultureComponent) {
        String zone = event.dynParcel.getZone();
        Rect2i shape = event.dynParcel.getShape();
        event.building = buildingManager.getRandomBuildingOfZoneForCulture(zone, shape, cultureComponent);
    }

    //the standard strategy used in Cities and StaticCities module
    public boolean buildParcel(DynParcel dynParcel, EntityRef settlement) {
        Region3i region = Region3i.createFromMinMax(new Vector3i(dynParcel.getShape().minX(), 255, dynParcel.getShape().minY()),
                new Vector3i(dynParcel.getShape().maxX(), -255, dynParcel.getShape().maxY()));
        if (!worldProvider.isRegionRelevant(region)) {
            return false;
        }

        CheckBuildingForParcelEvent event = new CheckBuildingForParcelEvent(dynParcel);
        settlement.send(event);

        if (!event.building.isPresent()) {
            return false;
        }

        GenericBuildingComponent building = event.building.get();
        if (building.isScaledDown) {
            Vector2i difference = dynParcel.shape.size().sub(building.minSize).div(2);
            dynParcel.shape = Rect2i.createFromMinAndMax(dynParcel.shape.min().add(difference), dynParcel.shape.max().sub(difference));
            region = Region3i.createFromMinMax(new Vector3i(dynParcel.getShape().minX(), 255, dynParcel.getShape().minY()),
                    new Vector3i(dynParcel.getShape().maxX(), -255, dynParcel.getShape().maxY()));
        }

        //Flatten the parcel area
        dynParcel.height = flatten(dynParcel.shape, dynParcel.height);

        RequestRasterTargetEvent requestRasterTargetEvent = new RequestRasterTargetEvent(cityTheme, dynParcel.shape);
        settlement.send(requestRasterTargetEvent);
        RasterTarget rasterTarget = requestRasterTargetEvent.rasterTarget;
        Rect2i shape = dynParcel.shape;
        HeightMap hm = HeightMaps.constant(dynParcel.height);


        if (dynParcel.height == -9999) {
            return false;
        }


        /**
         * Check for player collision
         */
        Map<EntityRef, EntityRef> playerCityMap = playerTracker.getPlayerCityMap();

        for (EntityRef player : playerCityMap.keySet()) {
            if (playerCityMap.get(player) == settlement) {
                LocationComponent playerLocation = player.getComponent(LocationComponent.class);
                if (playerLocation != null && dynParcel.getShape().contains(playerLocation.getLocalPosition().x(), playerLocation.getLocalPosition().z())) {
                    return false;
                }
            }
        }

        /**
         * Generate the building entity if there is one and send an event.
         */
        if (building.isEntity) {
            Optional<Prefab> entityPrefab = assetManager.getAsset(building.resourceUrn, Prefab.class);
            if (entityPrefab.isPresent()) {
                dynParcel.buildingEntity = entityManager.create(entityPrefab.get());
                dynParcel.buildingEntity.addComponent(new LocationComponent(new Vector3f(
                        (dynParcel.getShape().minX() + dynParcel.getShape().maxX()) / 2, dynParcel.height,
                        (dynParcel.getShape().minY() + dynParcel.getShape().maxY()) / 2))); // midpoint of the parcel shape, and the bottom of the building
                dynParcel.buildingEntity.addComponent(new SettlementRefComponent(settlement));
                dynParcel.buildingEntity.addComponent(new DynParcelRefComponent(dynParcel));
                dynParcel.buildingEntity.send(new BuildingEntitySpawnedEvent());

            }
        }

        /**
         * Checks for rotation
         */
        boolean needsRotation = buildingManager.needsRotation(dynParcel, building);
        if (needsRotation) {
            switch (dynParcel.getOrientation()) {
                case NORTH:
                    dynParcel.orientation = Orientation.EAST;
                    break;
                case SOUTH:
                    dynParcel.orientation = Orientation.WEST;
                    break;
                case WEST:
                    dynParcel.orientation = Orientation.NORTH;
                    break;
                case EAST:
                    dynParcel.orientation = Orientation.SOUTH;
                    break;
            }
        }
        /**
         * Generate buildings with BuildingGenerators
         */
        Optional<List<BuildingGenerator>> generatorsOptional = buildingManager.getGeneratorsForBuilding(building);
        if (generatorsOptional.isPresent()) {
            List<BuildingGenerator> generators = generatorsOptional.get();
            List<Building> compositeBuildings = generators.stream().map(generator -> generator.generate(dynParcel, hm)).collect(Collectors.toList());

            for (Building compositeBuilding : compositeBuildings) {
                for (AbsDynBuildingRasterizer rasterizer : stdRasterizers) {
                    rasterizer.raster(rasterTarget, compositeBuilding, hm);
                }

                for (BuildingPart part : compositeBuilding.getParts()) {
                    for (Door door : part.getDoors()) {
                        for (DoorRasterizer doorRasterizer : doorRasterizers) {
                            doorRasterizer.tryRaster(rasterTarget, door, hm);
                        }
                    }
                    for (Window window : part.getWindows()) {
                        for (WindowRasterizer windowRasterizer : windowRasterizers) {
                            windowRasterizer.tryRaster(rasterTarget, window, hm);
                        }
                    }
                    for (Decoration decoration : part.getDecorations()) {
                        for (DecorationRasterizer decorationRasterizer : decorationRasterizers) {
                            decorationRasterizer.tryRaster(rasterTarget, decoration, hm);
                        }
                    }
                    Roof roof = part.getRoof();
                    for (RoofRasterizer roofRasterizer : roofRasterizers) {
                        roofRasterizer.tryRaster(rasterTarget, roof, hm);
                    }
                }
            }
        }
        /**
         * Generate buildings with StructuredTemplates
         */
        Optional<List<EntityRef>> templatesOptional = buildingManager.getTemplatesForBuilding(building);
        if (templatesOptional.isPresent()) {
            List<EntityRef> templates = templatesOptional.get();
            for (EntityRef template : templates) {
                //Transform Commonworlds rotation to StructureTemplates rotation
                Side startSide = Side.BACK;
                Side endSide;
                switch (dynParcel.orientation.ordinal() / 2) {
                    case 0:
                        endSide = Side.FRONT;
                        break;
                    case 1:
                        endSide = Side.RIGHT;
                        break;
                    case 2:
                        endSide = Side.BACK;
                        break;
                    case 3:
                        endSide = Side.LEFT;
                        break;
                    default:
                        endSide = Side.FRONT;
                }

                // offset within the parcel, so the template is placed at the centre.
                BlockRegionTransform localTransform = BlockRegionTransform.createMovingThenRotating(new org.joml.Vector3i(), startSide, endSide);
                org.joml.Vector3i microOffset = localTransform.transformVector3i(
                        BlockRegionUtilities.determineBottomCenter(template.getComponent(SpawnBlockRegionsComponent.class))).negate();

                // offset in world space
                Vector3i worldOffset = new Vector3i(shape.minX() + Math.round((shape.sizeX()) / 2f) - 1, dynParcel.height,
                        shape.minY() + Math.round((shape.sizeY()) / 2f) - 1);
                Vector3i finalLocation = worldOffset.add(JomlUtil.from(microOffset));
                BlockRegionTransform blockRegionTransform = BlockRegionTransform.createRotationThenMovement(startSide, endSide, JomlUtil.from(finalLocation));

                template.send(new SpawnStructureBufferedEvent(blockRegionTransform));
                if (building.isEntity) {
                    template.send(new OnSpawnDynamicStructureEvent(blockRegionTransform, dynParcel.buildingEntity));
                }
            }
        }


        /**
         * Send block-change event to refresh the minimap
         */
        Map<org.joml.Vector3i, Block> blockPos = new HashMap<>();
        for (BaseVector2i rectPos : dynParcel.getShape().contents()) {
            blockPos.put(new org.joml.Vector3i(rectPos.x(), dynParcel.getHeight(), rectPos.y()), defaultBlock);
        }
        settlement.send(new PlaceBlocks(blockPos));
        dynParcel.setBuildingTypeName(building.name);
        return true;
    }

    public RoadStatus buildRoadParcel(RoadParcel parcel, EntityRef settlement) {
        boolean containsRelevantRegion = false;
        boolean segmentFailed = false;

        final int vertLimit = 255; // To check if region is relevant

        final int segmentHeight = 10; // Height to be given to the flatten function
        final int failHeight = -9999;

        // Factor by which the rect will be expanded while flattening
        final int expWidth = 1;
        final int expHeight = 1;
        final ImmutableVector2i rectExpansionFactor = new ImmutableVector2i(expWidth, expHeight);

        for (int i = 0; i < parcel.rects.size(); i++) {
            RoadSegment segment = parcel.rects.elementAt(i);

            if (segmentCache.containsKey(segment.hashCode()) && segmentCache.get(segment.hashCode()) == parcel.hashCode()) {
                continue;
            }

            // Check if the region is relevant
            Region3i region = Region3i.createFromMinMax(
                    new Vector3i(segment.rect.minX(), vertLimit, segment.rect.minY()),
                    new Vector3i(segment.rect.maxX(), -1 * vertLimit, segment.rect.maxY())
            );
            if (!worldProvider.isRegionRelevant(region)) {
                continue;
            } else {
                containsRelevantRegion = true;
            }

            // Flatten the rect
            // TODO: Find a way to store the surface height at that point to the segment here.
            segment.height = flatten(segment.rect.expand(rectExpansionFactor), segmentHeight);

            // Create raster targets
            RasterTarget rasterTarget = new BufferRasterTarget(blockBufferSystem, roadTheme, segment.rect);
            HeightMap hm = HeightMaps.constant(segment.height);

            if (segment.height == failHeight) {
                segmentFailed = true;
                continue;
            }

            // Check for player collision
            Map<EntityRef, EntityRef> playerCityMap = playerTracker.getPlayerCityMap();

            boolean shouldRaster = true;
            for (EntityRef player : playerCityMap.keySet()) {
                if (playerCityMap.get(player) == settlement) {
                    LocationComponent playerLocation = player.getComponent(LocationComponent.class);
                    if (playerLocation != null && segment.rect.contains(playerLocation.getLocalPosition().x(), playerLocation.getLocalPosition().z())) {
                        segmentFailed = true;
                        shouldRaster = false;
                        break;
                    }
                }
            }

            // Rasterize the road
            if (shouldRaster) {
                roadRasterizer.raster(rasterTarget, segment, hm);
                segmentCache.put(segment.hashCode(), parcel.hashCode());
            }
        }

        if (!containsRelevantRegion) {
            return RoadStatus.NONE;
        }

        if (segmentFailed) {
            return RoadStatus.PARTIAL;
        }

        return RoadStatus.COMPLETE;
    }

    /**
     * Catches the spawn of a structure template on a parcel and assigns potential chest entities to the parcels entity
     */
    @ReceiveEvent
    public void catchOnSpawnDynamicStructure(OnSpawnDynamicStructureEvent event, EntityRef entityRef) {
        if (!entityRef.hasComponent(ChestPositionsComponent.class) && !entityRef.hasComponent(ProductionChestComponent.class)) {
            return;
        }

        MultiInvStorageComponent multiInvStorageComponent = new MultiInvStorageComponent();
        if (entityRef.hasComponent(ChestPositionsComponent.class)) {
            ChestPositionsComponent chestPositionsComponent = entityRef.getComponent(ChestPositionsComponent.class);
            multiInvStorageComponent.chests = new ArrayList<>();
            for (org.joml.Vector3i pos : chestPositionsComponent.positions) {
                org.joml.Vector3i transformedPos = event.getTransformation().transformVector3i(pos);
                multiInvStorageComponent.chests.add(blockEntityRegistry.getBlockEntityAt(transformedPos));
            }
        }
        if (entityRef.hasComponent(ProductionChestComponent.class)) {
            if (multiInvStorageComponent.chests == null) {
                multiInvStorageComponent.chests = new ArrayList<>();
            }
            ProductionChestComponent productionChestComponent = entityRef.getComponent(ProductionChestComponent.class);

            for (org.joml.Vector3i pos : productionChestComponent.positions) {
                org.joml.Vector3i transformedPos = event.getTransformation().transformVector3i(pos);
                multiInvStorageComponent.chests.add(blockEntityRegistry.getBlockEntityAt(transformedPos));
            }
        }

        event.getBuildingEntity().addComponent(multiInvStorageComponent);
    }

    @ReceiveEvent
    public void onSpawnBlockRegions(SpawnStructureBufferedEvent event, EntityRef entity,
                                    SpawnBlockRegionsComponent spawnBlockRegionComponent) {
        BlockRegionTransform transformation = event.getTransformation();
        for (SpawnBlockRegionsComponent.RegionToFill regionToFill : spawnBlockRegionComponent.regionsToFill) {
            Block block = regionToFill.blockType;

            BlockRegion region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);
            if (block.getBlockFamily() == blockManager.getBlockFamily("CoreAdvancedAssets:chest")) {
                for (Vector3ic pos : BlockRegions.iterableInPlace(region)) {
                    entity.send(new SetBlockEvent(JomlUtil.from(pos), block));
                }
            } else {
                for (Vector3ic pos : BlockRegions.iterableInPlace(region)) {
                    entity.send(new BufferBlockEvent(JomlUtil.from(pos), block));
                }
            }
        }
    }

    @ReceiveEvent
    public void onSetBlockEvent(SetBlockEvent event, EntityRef entity) {
        worldProvider.setBlock(event.getPos(), event.block);
    }

    @ReceiveEvent
    public void onBufferBlockEvent(BufferBlockEvent event, EntityRef entity) {
        blockBufferSystem.saveBlock(event.getPos(), event.block);
    }

    @ReceiveEvent
    public void onRequestRasterTargetEvent(RequestRasterTargetEvent event, EntityRef entity) {
        event.rasterTarget = new BufferRasterTarget(blockBufferSystem, event.theme, event.shape);
    }
}
