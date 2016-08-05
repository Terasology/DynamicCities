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
package org.terasology.dynamicCities.settlements.components;


import org.terasology.dynamicCities.districts.DistrictManager;
import org.terasology.dynamicCities.districts.DistrictType;
import org.terasology.dynamicCities.districts.Kmeans;
import org.terasology.dynamicCities.population.Culture;
import org.terasology.dynamicCities.settlements.SettlementConstants;
import org.terasology.dynamicCities.utilities.ProbabilityDistribution;
import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.network.Replicate;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DistrictFacetComponent implements Component {

    /**
     * TODO: Count the area for each district. Define consumption of zonearea for each district (evenly distributed atm).
     * TODO: Assign districts similar to parcels (look up if needs are already fulfilled before placement)
     */
    @Replicate
    public Rect2i relativeRegion = Rect2i.EMPTY;
    @Replicate
    public Rect2i worldRegion = Rect2i.EMPTY;
    @Replicate
    public Rect2i gridWorldRegion = Rect2i.EMPTY;
    @Replicate
    public Rect2i gridRelativeRegion = Rect2i.EMPTY;
    @Replicate
    public int gridSize;
    @Replicate
    public Vector2i center = new Vector2i();
    @Replicate
    public List<Integer> districtMap;
    @Replicate
    public List<Integer> districtSize;
    @Replicate
    public Map<String, DistrictType> districtTypeMap;
    @Replicate
    public int districtCount;
    @Replicate
    public List<Vector2i> districtCenters;

    public DistrictFacetComponent() { }

    public DistrictFacetComponent(Region3i targetRegion, Border3D border, int gridSize, long seed, DistrictManager districtManager, Culture culture) {
        worldRegion = border.expandTo2D(targetRegion);
        relativeRegion = border.expandTo2D(targetRegion.size());
        this.gridSize = gridSize;
        center = new Vector2i(targetRegion.center().x(), targetRegion.center().z());
        gridWorldRegion = Rect2i.createFromMinAndMax(center.x() - targetRegion.sizeX() / (2 * gridSize),
                center.y() - targetRegion.sizeY() / (2 * gridSize),
                center.x() + targetRegion.sizeX() / (2 * gridSize),
                center.y() + targetRegion.sizeY() / (2 * gridSize));
        gridRelativeRegion = Rect2i.createFromMinAndMax(0, 0, targetRegion.sizeX() / gridSize, targetRegion.sizeY() / gridSize);
        WhiteNoise randNumberGen = new WhiteNoise(seed);

        /**
         * The first two values will be the x-pos and y-pos
         * Add additional random numbers or scale them for scattering
         */
        List<List<Float>> data = new ArrayList<>();
        Kmeans kmeans = new Kmeans();
        for (int i = 0; i < gridRelativeRegion.area(); i++) {
            List<Float> list = new ArrayList<>();
            list.add((float) Math.round(i / gridWorldRegion.sizeX()));
            list.add((float) Math.round(i % gridWorldRegion.sizeX()));
            list.add(TeraMath.fastAbs(randNumberGen.noise(786332, 262333)));
            list.add(TeraMath.fastAbs(randNumberGen.noise(126743, 748323)));
            data.add(list);
        }
        /*districtCount = Math.round(TeraMath.fastAbs(randNumberGen.noise(15233, 45129)
                * SettlementConstants.MAX_DISTRICTS));*/
        districtCount = SettlementConstants.MAX_DISTRICTS;
        int[] intMap = kmeans.kmeans(data, districtCount);
        districtMap = new ArrayList<>();
        districtMap = IntStream.of(intMap).boxed().collect(Collectors.toList());
        districtTypeMap = new HashMap<>(districtMap.size());
        districtCenters = new ArrayList<>();
        for (Vector2i districtCenter : kmeans.getCenters()) {
            districtCenters.add(districtCenter.mul(gridSize).add(worldRegion.min()));
        }
        //Calc district sizes
        int[] tempSize = new int[districtCount];
        for (Integer cluster : districtMap) {
            tempSize[cluster] += 1;
        }
        districtSize = new ArrayList<>();
        districtSize = IntStream.of(tempSize).boxed().collect(Collectors.toList());
        mapDistrictTypes(districtManager, culture);
    }


    private void mapDistrictTypes(DistrictManager districtManager, Culture culture) {
        Map<String, Float> zoneArea = new HashMap<>();
        ProbabilityDistribution<DistrictType> probabilityDistribution = new ProbabilityDistribution<>(districtManager.hashCode() | 413357);
        Map<String, Float> culturalNeedsPercentage = culture.getProcentualsForZone();
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
                Map<DistrictType, Float> probabilites = new HashMap<>(districtManager.getDistrictTypes().size());
                float totalDiff = 0;

                for (DistrictType districtType : districtManager.getDistrictTypes()) {
                    float diff = 0;
                    Map<String, Float> tempZoneArea = new HashMap<>(zoneArea);
                    for (String zone : districtType.zones) {
                        float area = districtSize.get(i) / districtType.zones.size();
                        tempZoneArea.put(zone, tempZoneArea.getOrDefault(zone, 0f) + area);
                        if (!culturalNeedsPercentage.containsKey(zone)) {
                            diff = Float.MAX_VALUE;
                        }
                        else if (tempZoneArea.get(zone) / totalAssignedArea > culturalNeedsPercentage.get(zone)) {
                            diff = 9999999f;
                        } else {
                            diff += TeraMath.fastAbs(tempZoneArea.get(zone) / totalAssignedArea - culturalNeedsPercentage.get(zone));
                        }
                    }
                    diff = 1 / diff;
                    probabilites.put(districtType, diff);
                    totalDiff += diff;
                }
                for (DistrictType districtType : districtManager.getDistrictTypes()) {
                    probabilites.put(districtType, probabilites.getOrDefault(districtType, 0f) / totalDiff);
                }

                //Assign District
                probabilityDistribution.initialise(probabilites);
                DistrictType nextDistrict = probabilityDistribution.get();
                for (String zone : nextDistrict.zones) {
                    float area = districtSize.get(i) / nextDistrict.zones.size();
                    zoneArea.put(zone, zoneArea.getOrDefault(zone, 0f) + area);
                }
                districtTypeMap.put(Integer.toString(districtCenters.indexOf(minCenter)), nextDistrict);
            }
        }
        return;

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
        return x - gridRelativeRegion.minX() + gridRelativeRegion.sizeX() * (z - gridRelativeRegion.minY());
    }

    protected int getWorldGridIndex(int x, int z) {
        if (!gridWorldRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, gridWorldRegion.toString()));
        }
        return x - gridWorldRegion.minX() + gridWorldRegion.sizeX() * (z - gridWorldRegion.minY());
    }

    public Rect2i getGridWorldRegion() {
        return gridWorldRegion;
    }

    public Rect2i getGridRelativeRegion() {
        return gridRelativeRegion;
    }

    public int get(int x, int y) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        return districtMap.get(getRelativeGridIndex(gridPos.x(), gridPos.y()));
    }

    public int get(BaseVector2i pos) {
        return get(pos.x(), pos.y());
    }

    public int getWorld(int x, int y) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        return districtMap.get(getWorldGridIndex(gridPos.x(), gridPos.y()));
    }

    public int getWorld(BaseVector2i pos) {
        return getWorld(pos.x(), pos.y());
    }

    public List<Integer> getInternal() {
        return districtMap;
    }
/*
    public void set(int x, int y, List<Float> value) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        data.set(getRelativeGridIndex(gridPos.x(), gridPos.y()), value);
    }

    public void set(BaseVector2i pos, List<Float> value) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        set(pos.x(), pos.y(), value);
    }

    public void setWorld(int x, int y, List<Float> value) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        data.set(getWorldGridIndex(gridPos.x(), gridPos.y()), value);
    }

    public void setWorld(BaseVector2i pos, List<Float> value) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        setWorld(gridPos.x(), gridPos.y(), value);
    }

    public void set(List<Float>[] newData) {
        Preconditions.checkArgument(newData.length == data.size(), "New data must have same length as existing");
        System.arraycopy(newData, 0, data, 0, data.size());
    }
*/
}
