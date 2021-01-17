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
package org.terasology.dynamicCities.region.components;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.entitySystem.Component;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.reflection.MappedContainer;
import org.terasology.world.block.BlockArea;
import org.terasology.world.block.BlockAreac;

import java.util.List;

@MappedContainer
public final class RoughnessFacetComponent implements Component {

    public BlockAreac relativeRegion = BlockArea.INVALID;
    public BlockAreac worldRegion = BlockArea.INVALID;
    public BlockAreac gridWorldRegion = BlockArea.INVALID;
    public BlockAreac gridRelativeRegion = BlockArea.INVALID;
    public int gridSize;
    public Vector2i center = new Vector2i();
    public List<Float> data = Lists.newArrayList();
    public float meanDeviation;

    public RoughnessFacetComponent() { }

    public RoughnessFacetComponent(RoughnessFacet roughnessFacet) {

        relativeRegion = new BlockArea(roughnessFacet.getRelativeArea());
        worldRegion = new BlockArea(roughnessFacet.getWorldArea());
        gridWorldRegion = new BlockArea(roughnessFacet.getGridWorldRegion());
        gridRelativeRegion = new BlockArea(roughnessFacet.getGridRelativeRegion());
        gridSize = roughnessFacet.getGridSize();
        center = new Vector2i(roughnessFacet.getCenter());
        for (int i = 0; i < roughnessFacet.getInternal().length; i++) {
            data.add(i, roughnessFacet.getInternal()[i]);
        }
        meanDeviation = roughnessFacet.getMeanDeviation();
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

    public BlockAreac getGridWorldRegion() {
        return gridWorldRegion;
    }

    public BlockAreac getGridRelativeRegion() {
        return gridRelativeRegion;
    }

    public float get(int x, int y) {
        Vector2ic gridPos = getRelativeGridPoint(x, y);
        return data.get(getRelativeGridIndex(gridPos.x(), gridPos.y()));
    }

    public float get(BaseVector2i pos) {
        Vector2ic gridPos = getRelativeGridPoint(pos.x(), pos.y());
        return get(gridPos.x(), gridPos.y());
    }

    public float getWorld(int x, int y) {
        Vector2ic gridPos = getWorldGridPoint(x, y);
        return data.get(getWorldGridIndex(gridPos.x(), gridPos.y()));
    }

    public float getWorld(BaseVector2i pos) {
        Vector2ic gridPos = getWorldGridPoint(pos.x(), pos.y());
        return getWorld(gridPos.x(), gridPos.y());
    }

    public List<Float> getInternal() {
        return data;
    }

    public void set(int x, int y, float value) {
        Vector2ic gridPos = getRelativeGridPoint(x, y);
        data.set(getRelativeGridIndex(gridPos.x(), gridPos.y()), value);
    }

    public void set(Vector2ic pos, float value) {
        Vector2ic gridPos = getRelativeGridPoint(pos.x(), pos.y());
        set(pos.x(), pos.y(), value);
    }

    public void setWorld(int x, int y, float value) {
        Vector2ic gridPos = getWorldGridPoint(x, y);
        data.set(getWorldGridIndex(gridPos.x(), gridPos.y()), value);
    }

    public void setWorld(Vector2ic pos, float value) {
        Vector2ic gridPos = getWorldGridPoint(pos.x(), pos.y());
        setWorld(gridPos.x(), gridPos.y(), value);
    }

    public void set(List<Float> newData) {
        Preconditions.checkArgument(newData.size() == data.size(), "New data must have same length as existing");
        data.clear();
        data.addAll(newData);
    }

}
