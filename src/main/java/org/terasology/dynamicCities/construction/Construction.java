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

import org.terasology.cities.BlockTheme;
import org.terasology.cities.DefaultBlockType;
import org.terasology.cities.bldg.Building;
import org.terasology.cities.bldg.BuildingPart;
import org.terasology.cities.deco.Decoration;
import org.terasology.cities.door.Door;
import org.terasology.cities.model.roof.Roof;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.window.Window;
import org.terasology.commonworld.heightmap.HeightMap;
import org.terasology.commonworld.heightmap.HeightMaps;
import org.terasology.dynamicCities.decoration.ColumnRasterizer;
import org.terasology.dynamicCities.decoration.DecorationRasterizer;
import org.terasology.dynamicCities.decoration.SingleBlockRasterizer;
import org.terasology.dynamicCities.gen.*;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.rasterizer.AbsDynBuildingRasterizer;
import org.terasology.dynamicCities.rasterizer.WorldRasterTarget;
import org.terasology.dynamicCities.rasterizer.doors.DoorRasterizer;
import org.terasology.dynamicCities.rasterizer.doors.SimpleDoorRasterizer;
import org.terasology.dynamicCities.rasterizer.doors.WingDoorRasterizer;
import org.terasology.dynamicCities.rasterizer.parts.HollowBuildingPartRasterizer;
import org.terasology.dynamicCities.rasterizer.parts.RectPartRasterizer;
import org.terasology.dynamicCities.rasterizer.parts.RoundPartRasterizer;
import org.terasology.dynamicCities.rasterizer.parts.StaircaseRasterizer;
import org.terasology.dynamicCities.rasterizer.roofs.*;
import org.terasology.dynamicCities.rasterizer.window.RectWindowRasterizer;
import org.terasology.dynamicCities.rasterizer.window.SimpleWindowRasterizer;
import org.terasology.dynamicCities.rasterizer.window.WindowRasterizer;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.SurfaceHeightFacet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Share(value = Construction.class)
@RegisterSystem
public class Construction extends BaseComponentSystem {

    @In
    static WorldProvider worldProvider;

    @In
    static BlockManager blockManager;

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

    private CommercialBuildingGenerator commercialBuildingGenerator;
    private RectHouseGenerator rectHouseGenerator;
    private SimpleChurchGenerator simpleChurchGenerator;
    private TownHallGenerator townHallGenerator;
    private DefaultBuildingGenerator defaultBuildingGenerator;

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


        commercialBuildingGenerator = new CommercialBuildingGenerator(worldProvider.getSeed().hashCode() / 10);
        rectHouseGenerator = new RectHouseGenerator();
        simpleChurchGenerator = new SimpleChurchGenerator(worldProvider.getSeed().hashCode() / 7);
        townHallGenerator = new TownHallGenerator();
        defaultBuildingGenerator = new DefaultBuildingGenerator(worldProvider.getSeed().hashCode() / 3);
    }

    /**
     *
     * @param area The area which should be flattened
     * @param defaultHeight A rough estimation of the mean height of the terrain
     * @param filler The blocktype which should be used to fill up terrain under the mean height
     * @return The height on which it was flattened to
     */
    public int flatten(Rect2i area, int defaultHeight, Block filler) {

        SurfaceHeightFacet surfaceHeightFacet = sample(area, defaultHeight);
        int meanHeight = 0;
        Vector3i setPos = new Vector3i();

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
                    if (worldProvider.getBlock(pos) != air && !worldProvider.getBlock(pos).isLiquid() && worldProvider.getBlock(pos) != plant) {
                        surfaceHeightFacet.setWorld(x, z, y);
                        break;
                    }
                }
            }
        }
        return surfaceHeightFacet;
    }

    //the standard strategy used in Cities and StaticCities module
    public boolean buildParcel(DynParcel dynParcel) {
        RasterTarget rasterTarget = new WorldRasterTarget(worldProvider, theme, dynParcel.shape);
        dynParcel.height = flatten(dynParcel.shape, dynParcel.height);
        HeightMap hm = HeightMaps.constant(dynParcel.height);
        Region3i region = Region3i.createFromMinMax(new Vector3i(dynParcel.getShape().maxX(), 255, dynParcel.getShape().maxY()),
                new Vector3i(dynParcel.getShape().minX(), -255, dynParcel.getShape().minY()));
        /**
         *
         * TODO: Insert advanced building generation here
         *
         */

        //dynParcel.assignZone();
        Set<Building> buildings = (defaultBuildingGenerator.generate(dynParcel, hm));

        if (!worldProvider.isRegionRelevant(region)) {
            return false;
        }

        for (Building building : buildings) {
            for (AbsDynBuildingRasterizer rasterizer : stdRasterizers) {
                rasterizer.raster(rasterTarget, building, hm);
            }

            for (BuildingPart part : building.getParts()) {
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
        return true;
    }
}
