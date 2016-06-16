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


import com.google.common.base.Preconditions;
import org.terasology.entitySystem.Component;
import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.Border3D;

import java.util.ArrayList;
import java.util.List;

public class DistrictFacetComponent implements Component {


    public Rect2i relativeRegion = Rect2i.EMPTY;
    public Rect2i worldRegion = Rect2i.EMPTY;
    public Rect2i gridWorldRegion = Rect2i.EMPTY;
    public Rect2i gridRelativeRegion = Rect2i.EMPTY;
    public int gridSize;
    public Vector2i center = new Vector2i();
    public List<Float>[] data;

    public DistrictFacetComponent(Region3i targetRegion, Border3D border, int gridSize, long seed) {
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
        data = new List[gridRelativeRegion.area()];
        /**
         * Currently there are 4 Zones: Governmental, Residential, Clerical and Commercial.
         * Add additional random numbers here for new zones.
         */
        for (List list : data) {
            list = new ArrayList<Float>();
            list.add(TeraMath.fastAbs(randNumberGen.noise(452324, 323942)));
            list.add(TeraMath.fastAbs(randNumberGen.noise(253242, 213102)));
            list.add(TeraMath.fastAbs(randNumberGen.noise(786332, 262333)));
            list.add(TeraMath.fastAbs(randNumberGen.noise(126743, 748323)));
        }
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

    public List<Float> get(int x, int y) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        return data[getRelativeGridIndex(gridPos.x(), gridPos.y())];
    }

    public List<Float> get(BaseVector2i pos) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        return get(gridPos.x(), gridPos.y());
    }

    public List<Float> getWorld(int x, int y) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        return data[getWorldGridIndex(gridPos.x(), gridPos.y())];
    }

    public List<Float> getWorld(BaseVector2i pos) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        return getWorld(gridPos.x(), gridPos.y());
    }

    public List<Float>[] getInternal() {
        return data;
    }

    public void set(int x, int y, List<Float> value) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        data[getRelativeGridIndex(gridPos.x(), gridPos.y())] = value;
    }

    public void set(BaseVector2i pos, List<Float> value) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        set(pos.x(), pos.y(), value);
    }

    public void setWorld(int x, int y, List<Float> value) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        data[getWorldGridIndex(gridPos.x(), gridPos.y())] = value;
    }

    public void setWorld(BaseVector2i pos, List<Float> value) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        setWorld(gridPos.x(), gridPos.y(), value);
    }

    public void set(List<Float>[] newData) {
        Preconditions.checkArgument(newData.length == data.length, "New data must have same length as existing");
        System.arraycopy(newData, 0, data, 0, data.length);
    }

}
