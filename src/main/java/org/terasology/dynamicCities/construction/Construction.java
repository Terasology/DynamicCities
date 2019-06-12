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
import org.terasology.dynamicCities.construction.events.BuildingEntitySpawnedEvent;
import org.terasology.dynamicCities.construction.events.SpawnStructureBufferedEvent;
import org.terasology.dynamicCities.decoration.ColumnRasterizer;
import org.terasology.dynamicCities.decoration.DecorationRasterizer;
import org.terasology.dynamicCities.decoration.SingleBlockRasterizer;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.playerTracking.PlayerTracker;
import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.dynamicCities.rasterizer.BufferRasterTarget;
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
import org.terasology.math.Region3i;
import org.terasology.math.Side;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.BaseVector3i;
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
import org.terasology.structureTemplates.util.BlockRegionUtilities;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.world.BlockEntityRegistry;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.placement.PlaceBlocks;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

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


    private BlockTheme theme;

    private Block air;
    private Block plant;
    private Block water;
    private Block defaultBlock;
    private int maxMinDeviation = 40;
    private final List<AbsDynBuildingRasterizer> stdRasterizers = new ArrayList<>();
    private final List<WindowRasterizer> windowRasterizers = new ArrayList<>();
    private final List<DoorRasterizer> doorRasterizers = new ArrayList<>();
    private final List<RoofRasterizer> roofRasterizers = new ArrayList<>();
    private final List<DecorationRasterizer> decorationRasterizers = new ArrayList<>();
    private final List<Block> plantBlocks = new ArrayList<>();
    private Logger logger = LoggerFactory.getLogger(Construction.class);

    public void initialise() {
        theme = BlockTheme.builder(blockManager)
                .register(DefaultBlockType.ROAD_FILL, "core:dirt")
                .register(DefaultBlockType.ROAD_SURFACE, "core:Gravel")
                .register(DefaultBlockType.LOT_EMPTY, "core:dirt")
                .register(DefaultBlockType.BUILDING_WALL, "Cities:stonawall1")
                .register(DefaultBlockType.BUILDING_FLOOR, "Cities:stonawall1dark")
                .register(DefaultBlockType.BUILDING_FOUNDATION, "core:gravel")
                .register(DefaultBlockType.TOWER_STAIRS, "core:CobbleStone")
                .register(DefaultBlockType.ROOF_FLAT, "Cities:rooftiles2")
                .register(DefaultBlockType.ROOF_HIP, "Cities:wood3")
                .register(DefaultBlockType.ROOF_SADDLE, "Cities:wood3")
                .register(DefaultBlockType.ROOF_DOME, "core:plank")
                .register(DefaultBlockType.ROOF_GABLE, "core:plank")
                .register(DefaultBlockType.SIMPLE_DOOR, BlockManager.AIR_ID)
                .register(DefaultBlockType.WING_DOOR, BlockManager.AIR_ID)
                .register(DefaultBlockType.WINDOW_GLASS, BlockManager.AIR_ID)
                .register(DefaultBlockType.TOWER_WALL, "Cities:stonawall1")

                // -- requires Fences module
                .registerFamily(DefaultBlockType.FENCE, "Fences:Fence")
                .registerFamily(DefaultBlockType.FENCE_GATE, BlockManager.AIR_ID)  // there is no fence gate :-(
                .registerFamily(DefaultBlockType.TOWER_STAIRS, "core:CobbleStone:engine:stair")
                .registerFamily(DefaultBlockType.BARREL, "StructuralResources:Barrel")
                .registerFamily(DefaultBlockType.LADDER, "Core:Ladder")
                .registerFamily(DefaultBlockType.PILLAR_BASE, "core:CobbleStone:StructuralResources:pillarBase")
                .registerFamily(DefaultBlockType.PILLAR_MIDDLE, "core:CobbleStone:StructuralResources:pillar")
                .registerFamily(DefaultBlockType.PILLAR_TOP, "core:CobbleStone:StructuralResources:pillarTop")
                .registerFamily(DefaultBlockType.TORCH, "Core:Torch")

                .build();

        blockManager = CoreRegistry.get(BlockManager.class);
        air = blockManager.getBlock("engine:air");
        water = blockManager.getBlock("core:water");
        plant = blockManager.getBlock("core:plant");
        defaultBlock = blockManager.getBlock("core:dirt");

        stdRasterizers.add(new HollowBuildingPartRasterizer(theme, worldProvider));
        stdRasterizers.add(new RectPartRasterizer(theme, worldProvider));
        stdRasterizers.add(new RoundPartRasterizer(theme, worldProvider));
        stdRasterizers.add(new StaircaseRasterizer(theme, worldProvider));


        decorationRasterizers.add(new SingleBlockRasterizer(theme));
        decorationRasterizers.add(new ColumnRasterizer(theme));

        doorRasterizers.add(new SimpleDoorRasterizer(theme));
        doorRasterizers.add(new WingDoorRasterizer(theme));

        windowRasterizers.add(new RectWindowRasterizer(theme));
        windowRasterizers.add(new SimpleWindowRasterizer(theme));

        roofRasterizers.add(new ConicRoofRasterizer(theme));
        roofRasterizers.add(new DomeRoofRasterizer(theme));
        roofRasterizers.add(new FlatRoofRasterizer(theme));
        roofRasterizers.add(new HipRoofRasterizer(theme));
        roofRasterizers.add(new PentRoofRasterizer(theme));
        roofRasterizers.add(new SaddleRoofRasterizer(theme));

        //Register plant blocks
        plantBlocks.add(blockManager.getBlock("core:GreenLeaf"));
        plantBlocks.add(blockManager.getBlock("core:OakTrunk"));
        plantBlocks.add(blockManager.getBlock("core:DarkLeaf"));
        plantBlocks.add(blockManager.getBlock("core:PineTrunk"));
        plantBlocks.add(blockManager.getBlock("core:BirchTrunk"));
        plantBlocks.add(blockManager.getBlock("core:RedLeaf"));
        plantBlocks.add(blockManager.getBlock("core:Cactus"));
    }

    /**
     * Maybe return a structured data with (int or false) as return value
     * @param area The area which should be flattened
     * @param defaultHeight A rough estimation of the mean height of the terrain
     * @param filler The blocktype which should be used to fill up terrain under the mean height
     * @return The height on which it was flattened to
     */
    public int flatten(Rect2i area, int defaultHeight, Block filler) {

        if (area.area() == 0) {
            logger.error("The area which should be flattened is empty!");
            return -9999;
        }
        SurfaceHeightFacet surfaceHeightFacet = sample(area, defaultHeight);
        int meanHeight = 0;
        Vector3i setPos = new Vector3i();
        Region3i areaRegion = Region3i.createFromMinMax(new Vector3i(area.minX(), defaultHeight - maxMinDeviation, area.minY()),
                new Vector3i(area.maxX(), defaultHeight + maxMinDeviation, area.maxY()));

        if (!worldProvider.isRegionRelevant(areaRegion)) {
            return -9999;
        }
        for (BaseVector2i pos : area.contents()) {
            meanHeight += surfaceHeightFacet.getWorld(pos);
        }
        meanHeight /= area.area();

        for (BaseVector2i pos : area.contents()) {
            int y = Math.round(surfaceHeightFacet.getWorld(pos));
            if (y <= meanHeight) {
                for (int i = y; i <= meanHeight; i++) {
                    setPos.set(pos.x(), i, pos.y());
                    worldProvider.setBlock(setPos, filler);
                }
            }
            if (y >= meanHeight) {
                for (int i = y; i > meanHeight; i--) {
                    setPos.set(pos.x(), i, pos.y());
                    worldProvider.setBlock(setPos, air);
                }
            }
        }


        return meanHeight;
    }

    public int flatten(Rect2i area, int defaultHeight) {
        return flatten(area, defaultHeight, defaultBlock);
    }
    /**
     *
     * @param area The area which should be sampled
     * @param height A rough estimation of the mean height of the terrain
     * @return
     */
    public SurfaceHeightFacet sample(Rect2i area, int height) {

        BaseVector3i minRegionPos = new Vector3i(area.minX(), height - maxMinDeviation, area.minY());
        BaseVector3i maxRegionPos = new Vector3i(area.maxX(), height + maxMinDeviation, area.maxY());
        Border3D border = new Border3D(0, 0, 0);
        SurfaceHeightFacet surfaceHeightFacet = new SurfaceHeightFacet(Region3i.createBounded(minRegionPos, maxRegionPos), border);
        Vector3i pos = new Vector3i();

        for (int x = area.minX(); x <= area.maxX(); x++) {
            for (int z = area.minY(); z <= area.maxY(); z++) {
                for (int y = height + maxMinDeviation; y >= height - maxMinDeviation; y--) {
                    pos.set(x, y, z);
                    if (worldProvider.getBlock(pos) != air && !worldProvider.getBlock(pos).isLiquid()
                            && !plantBlocks.contains(worldProvider.getBlock(pos))) {
                        surfaceHeightFacet.setWorld(x, z, y);
                        break;
                    }
                }
            }
        }
        return surfaceHeightFacet;
    }

    //the standard strategy used in Cities and StaticCities module
    public boolean buildParcel(DynParcel dynParcel, EntityRef settlement, CultureComponent cultureComponent) {
        Region3i region = Region3i.createFromMinMax(new Vector3i(dynParcel.getShape().minX(), 255, dynParcel.getShape().minY()),
                new Vector3i(dynParcel.getShape().maxX(), -255, dynParcel.getShape().maxY()));
        if (!worldProvider.isRegionRelevant(region)) {
            return false;
        }
        /**
         * get the building or shrink parcel size if no fitting building was found.
         */
        Optional<GenericBuildingComponent> buildingOptional;
        GenericBuildingComponent building;
        buildingOptional = buildingManager.getRandomBuildingOfZoneForCulture(dynParcel.getZone(), dynParcel.getShape(), cultureComponent);

        if (!buildingOptional.isPresent()) {
            return false;
        } else {
            building = buildingOptional.get();
        }
        if (building.isScaledDown) {
            Vector2i difference = dynParcel.shape.size().sub(building.minSize).div(2);
            dynParcel.shape = Rect2i.createFromMinAndMax(dynParcel.shape.min().add(difference), dynParcel.shape.max().sub(difference));
            region = Region3i.createFromMinMax(new Vector3i(dynParcel.getShape().minX(), 255, dynParcel.getShape().minY()),
                    new Vector3i(dynParcel.getShape().maxX(), -255, dynParcel.getShape().maxY()));
        }

        //Flatten the parcel area
        dynParcel.height = flatten(dynParcel.shape, dynParcel.height);

        RasterTarget rasterTarget = new BufferRasterTarget(blockBufferSystem, theme, dynParcel.shape);
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
                case NORTH: dynParcel.orientation = Orientation.EAST;
                    break;
                case SOUTH: dynParcel.orientation = Orientation.WEST;
                    break;
                case WEST: dynParcel.orientation = Orientation.NORTH;
                    break;
                case EAST: dynParcel.orientation = Orientation.SOUTH;
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
                Side startSide = Side.RIGHT;
                Side endSide;
                switch (dynParcel.orientation.ordinal() / 2) {
                    case 0:
                        endSide = Side.RIGHT;
                        break;
                    case 1:
                        endSide = Side.FRONT;
                        break;
                    case 2:
                        endSide = Side.LEFT;
                        break;
                    case 3:
                        endSide = Side.BACK;
                        break;
                    default:
                        endSide = Side.RIGHT;
                }


                BlockRegionTransform blockRegionTransform = BlockRegionTransform.createRotationThenMovement(startSide, endSide, BlockRegionUtilities.determineBottomCenter(template.getComponent(SpawnBlockRegionsComponent.class)));
                Vector3i finalLocation = blockRegionTransform.transformVector3i(new Vector3i(shape.minX() + Math.round(shape.sizeX() / 2f), dynParcel.height,
                        shape.minY() + Math.round(shape.sizeY() / 2f)));
                blockRegionTransform = BlockRegionTransform.createMovingThenRotating(finalLocation, startSide, endSide);

                template.send(new SpawnStructureBufferedEvent(blockRegionTransform));
                if (building.isEntity) {
                    template.send(new OnSpawnDynamicStructureEvent(blockRegionTransform, dynParcel.buildingEntity));
                }
            }
        }




        /**
         * Send block-change event to refresh the minimap
         */
        Map<Vector3i, Block> blockPos = new HashMap<>();
        for (BaseVector2i rectPos : dynParcel.getShape().contents()) {
            blockPos.put(new Vector3i(rectPos.x(), dynParcel.getHeight(), rectPos.y()), defaultBlock);
        }
        settlement.send(new PlaceBlocks(blockPos));
        dynParcel.setBuildingTypeName(building.name);
        return true;
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
            for (Vector3i pos : chestPositionsComponent.positions) {
                Vector3i transformedPos = event.getTransformation().transformVector3i(pos);
                multiInvStorageComponent.chests.add(blockEntityRegistry.getBlockEntityAt(transformedPos));
            }
        }
        if (entityRef.hasComponent(ProductionChestComponent.class)) {
            if (multiInvStorageComponent.chests == null) {
                multiInvStorageComponent.chests = new ArrayList<>();
            }
            ProductionChestComponent productionChestComponent = entityRef.getComponent(ProductionChestComponent.class);

            for (Vector3i pos : productionChestComponent.positions) {
                Vector3i transformedPos = event.getTransformation().transformVector3i(pos);
                multiInvStorageComponent.chests.add(blockEntityRegistry.getBlockEntityAt(transformedPos));
            }
        }

        event.getBuildingEntity().addComponent(multiInvStorageComponent);
    }

    @ReceiveEvent
    public void onSpawnBlockRegions(SpawnStructureBufferedEvent event, EntityRef entity,
                                    SpawnBlockRegionsComponent spawnBlockRegionComponent) {
        BlockRegionTransform transformation = event.getTransformation();
        for (SpawnBlockRegionsComponent.RegionToFill regionToFill: spawnBlockRegionComponent.regionsToFill) {
            Block block = regionToFill.blockType;

            Region3i region = regionToFill.region;
            region = transformation.transformRegion(region);
            block = transformation.transformBlock(block);
            if (block.getBlockFamily() == blockManager.getBlockFamily("core:chest")) {
                for (Vector3i pos : region) {
                    worldProvider.setBlock(pos, block);
                }
            } else {
                for (Vector3i pos : region) {
                    blockBufferSystem.saveBlock(pos, block);
                }
            }
        }
    }


}
