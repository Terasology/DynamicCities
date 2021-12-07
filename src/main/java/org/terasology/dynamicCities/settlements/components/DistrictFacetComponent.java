// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements.components;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.dynamicCities.districts.DistrictManager;
import org.terasology.dynamicCities.districts.DistrictType;
import org.terasology.dynamicCities.districts.Kmeans;
import org.terasology.dynamicCities.population.CultureComponent;
import org.terasology.dynamicCities.settlements.SettlementConstants;
import org.terasology.engine.network.Replicate;
import org.terasology.engine.utilities.procedural.WhiteNoise;
import org.terasology.engine.utilities.random.DiscreteDistribution;
import org.terasology.engine.utilities.random.MersenneRandom;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.math.TeraMath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

public class DistrictFacetComponent implements Component<DistrictFacetComponent> {

    /**
     * TODO: Count the area for each district. Define consumption of zonearea for each district (evenly distributed atm).
     * TODO: Assign districts similar to parcels (look up if needs are already fulfilled before placement)
     */
    @Replicate
    public BlockAreac relativeRegion = new BlockArea(BlockArea.INVALID);
    @Replicate
    public BlockAreac worldRegion = new BlockArea(BlockArea.INVALID);
    @Replicate
    public BlockAreac gridWorldRegion = new BlockArea(BlockArea.INVALID);
    @Replicate
    public BlockArea gridRelativeRegion = new BlockArea(BlockArea.INVALID);
    @Replicate
    public int gridSize;
    @Replicate
    public Vector2i center = new Vector2i();
    @Replicate
    public List<Integer> districtMap = Lists.newArrayList();
    @Replicate
    public List<Integer> districtSize = Lists.newArrayList();
    @Replicate
    public Map<String, DistrictType> districtTypeMap = Maps.newHashMap();
    @Replicate
    public int districtCount;
    @Replicate
    public List<Vector2i> districtCenters = Lists.newArrayList();

    public DistrictFacetComponent() { }

    public DistrictFacetComponent(BlockRegion targetRegion, Border3D border, int gridSize, long seed, DistrictManager districtManager,
                                  CultureComponent cultureComponent) {
        worldRegion = border.expandTo2D(targetRegion);
        relativeRegion = border.expandTo2D(targetRegion.getSize(new Vector3i()));
        this.gridSize = gridSize;
        Vector3f regionCenter = targetRegion.center(new Vector3f());
        center = new Vector2i((int) regionCenter.x(), (int) regionCenter.z());
        gridWorldRegion = new BlockArea(center.x() - targetRegion.getSizeX() / (2 * gridSize),
                center.y() - targetRegion.getSizeY() / (2 * gridSize),
                center.x() + targetRegion.getSizeX() / (2 * gridSize),
                center.y() + targetRegion.getSizeY() / (2 * gridSize));
        gridRelativeRegion = new BlockArea(0, 0, targetRegion.getSizeX() / gridSize, targetRegion.getSizeY() / gridSize);
        WhiteNoise randNumberGen = new WhiteNoise(seed);

        /**
         * The first two values will be the x-pos and y-pos
         * Add additional random numbers or scale them for scattering
         */
        List<List<Float>> data = new ArrayList<>();
        Kmeans kmeans = new Kmeans();
        for (int i = 0; i < gridRelativeRegion.area(); i++) {
            List<Float> list = new ArrayList<>();
            list.add((float) Math.round(i / gridWorldRegion.getSizeX()));
            list.add((float) Math.round(i % gridWorldRegion.getSizeX()));
            list.add(TeraMath.fastAbs(randNumberGen.noise(786332, 262333)));
            list.add(TeraMath.fastAbs(randNumberGen.noise(126743, 748323)));
            data.add(list);
        }
        /*districtCount = Math.round(TeraMath.fastAbs(randNumberGen.noise(15233, 45129)
                * SettlementConstants.MAX_DISTRICTS));*/
        districtCount = SettlementConstants.MAX_DISTRICTS;
        int[] intMap = kmeans.kmeans(data, districtCount);
        districtMap = IntStream.of(intMap).boxed().collect(Collectors.toList());
        for (Vector2i districtCenter : kmeans.getCenters()) {
            districtCenters.add(districtCenter.mul(gridSize).add(new Vector2i(worldRegion.minX(), worldRegion.minY())));
        }
        //Calc district sizes
        int[] tempSize = new int[districtCount];
        for (Integer cluster : districtMap) {
            tempSize[cluster] += 1;
        }
        districtSize = new ArrayList<>();
        districtSize = IntStream.of(tempSize).boxed().collect(Collectors.toList());
        mapDistrictTypes(districtManager, cultureComponent);
    }


    private void mapDistrictTypes(DistrictManager districtManager, CultureComponent cultureComponent) {
        checkArgument(!districtManager.getDistrictTypes().isEmpty(), "There are no district types!");
        Map<String, Float> zoneArea = new HashMap<>();
        DiscreteDistribution<DistrictType> probabilityDistribution = new DiscreteDistribution<>();
        MersenneRandom rng = new MersenneRandom(districtManager.hashCode() | 413357);
        Map<String, Float> culturalNeedsPercentage = cultureComponent.getProcentualsForZone();
        int totalAssignedArea = 0;

        for (int i = 0; i < districtCount; i++) {
            float min = 99999;
            Vector2i minCenter = null;
            for (Vector2i districtCenter : districtCenters) {
                String key = Integer.toString(districtCenters.indexOf(districtCenter));
                if (districtTypeMap.containsKey(key)) {
                    continue;
                }
                float temp = (float) districtCenter.distance(center);
                if (temp < min) {
                    min = temp;
                    minCenter = districtCenter;
                }
            }
            if (minCenter != null) {
                totalAssignedArea += districtSize.get(i);

                //Calculate probabilities
                for (DistrictType districtType : districtManager.getDistrictTypes()) {
                    float diff = 0;
                    Map<String, Float> tempZoneArea = new HashMap<>(zoneArea);
                    for (String zone : districtType.zones) {
                        float area = (float) districtSize.get(i) / districtType.zones.size();
                        tempZoneArea.put(zone, tempZoneArea.getOrDefault(zone, 0f) + area);
                        if (!culturalNeedsPercentage.containsKey(zone)) {
                            diff = Float.MAX_VALUE;
                        } else if (tempZoneArea.get(zone) / totalAssignedArea > culturalNeedsPercentage.get(zone)) {
                            diff = 9999999f;
                        } else {
                            diff += TeraMath.fastAbs(tempZoneArea.get(zone) / totalAssignedArea - culturalNeedsPercentage.get(zone));
                        }
                    }

                    diff = (diff == 0) ? 0 : 1 / diff;
                    probabilityDistribution.add(districtType, diff);
                }

                //Assign District
                DistrictType nextDistrict = probabilityDistribution.sample(rng);
                for (String zone : nextDistrict.zones) {
                    float area = (float) districtSize.get(i) / nextDistrict.zones.size();
                    zoneArea.put(zone, zoneArea.getOrDefault(zone, 0f) + area);
                }
                districtTypeMap.put(Integer.toString(districtCenters.indexOf(minCenter)), nextDistrict);
            }
        }
    }

    public DistrictType getDistrict(Vector2i worldPoint) {
        return getDistrict(worldPoint.x(), worldPoint.y());
    }

    public DistrictType getDistrict(int x, int y) {
        return districtTypeMap.get(Integer.toString(getWorld(x, y)));
    }

    //Copy of the methods used to access the data. Maybe there is a better way than storing them all here

    public Vector2i getWorldPoint(Vector2i gridPoint) {
        return getWorldPoint(gridPoint.x(), gridPoint.y());
    }

    public Vector2i getWorldPoint(int x, int y) {
        if (!gridWorldRegion.contains(x, y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y, gridWorldRegion.toString()));
        }
        int xRelative = x - center.x();
        int yRelative = y - center.y();
        int xNew = center.x() + Math.round((float) xRelative * gridSize);
        int yNew = center.y() + Math.round((float) yRelative * gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!worldRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew, worldRegion.toString()));
        }
        return gridPoint;
    }

    public Vector2i getRelativeGridPoint(Vector2i worldPoint) {
        return getRelativeGridPoint(worldPoint.x(), worldPoint.y());
    }

    public Vector2i getRelativeGridPoint(int x, int y) {
        if (!relativeRegion.contains(x, y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y, relativeRegion.toString()));
        }
        int xNew = Math.round((float) x / gridSize);
        int yNew = Math.round((float) y / gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!gridRelativeRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew, gridRelativeRegion.toString()));
        }
        return gridPoint;
    }

    public Vector2i getWorldGridPoint(Vector2i worldPoint) {
        return getRelativeGridPoint(worldPoint).add(center);
    }

    public Vector2i getWorldGridPoint(int x, int y) {
        if (!worldRegion.contains(x, y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y, worldRegion.toString()));
        }
        int xRelative = x - center.x();
        int yRelative = y - center.y();
        int xNew = center.x() + Math.round((float) xRelative / gridSize);
        int yNew = center.y() + Math.round((float) yRelative / gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!gridWorldRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew, gridWorldRegion.toString()));
        }
        return gridPoint;
    }

    public int getGridSize() {
        return gridSize;
    }

    public Vector2i getCenter() {
        return center;
    }

    protected int getRelativeGridIndex(int x, int z) {
        if (!gridRelativeRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, gridWorldRegion.toString()));
        }
        return x - gridRelativeRegion.minX() + gridRelativeRegion.getSizeX() * (z - gridRelativeRegion.minY());
    }

    protected int getWorldGridIndex(int x, int z) {
        if (!gridWorldRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, gridWorldRegion.toString()));
        }
        return x - gridWorldRegion.minX() + gridWorldRegion.getSizeX() * (z - gridWorldRegion.minY());
    }

    public BlockArea getGridWorldRegion() {
        return new BlockArea(gridWorldRegion.minX(), gridWorldRegion.minY(), gridWorldRegion.maxX(), gridWorldRegion.maxY());
    }

    public int get(int x, int y) {
        Vector2i gridPos = getRelativeGridPoint(x, y);
        return districtMap.get(getRelativeGridIndex(gridPos.x(), gridPos.y()));
    }

    public int get(Vector2ic pos) {
        return get(pos.x(), pos.y());
    }

    public int getWorld(int x, int y) {
        Vector2i gridPos = getWorldGridPoint(x, y);
        return districtMap.get(getWorldGridIndex(gridPos.x(), gridPos.y()));
    }

    public int getWorld(Vector2ic pos) {
        return getWorld(pos.x(), pos.y());
    }

    @Override
    public void copyFrom(DistrictFacetComponent other) {
        this.relativeRegion = new BlockArea(other.relativeRegion);
        this.worldRegion = new BlockArea(other.worldRegion);
        this.gridWorldRegion = new BlockArea(other.gridWorldRegion);
        this.gridRelativeRegion.set(other.gridRelativeRegion);
        this.gridSize = other.gridSize;
        this.center = other.center;
        this.districtMap = Lists.newArrayList(other.districtMap);
        this.districtSize = Lists.newArrayList(other.districtSize);
        this.districtTypeMap = Maps.newHashMap(other.districtTypeMap);
        this.districtCount = other.districtCount;
        this.districtCenters = Lists.newArrayList(other.districtCenters).stream().map(new Function<Vector2i, Vector2i>() {
            @Override
            public Vector2i apply(Vector2i res) {
                return new Vector2i(res);
            }
        }).collect(Collectors.toList());
    }
}
