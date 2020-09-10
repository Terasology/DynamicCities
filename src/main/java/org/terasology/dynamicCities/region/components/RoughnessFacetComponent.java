// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.terasology.dynamicCities.facets.RoughnessFacet;
import org.terasology.engine.entitySystem.Component;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.nui.reflection.MappedContainer;

import java.util.List;

@MappedContainer
public final class RoughnessFacetComponent implements Component {

    public Rect2i relativeRegion = Rect2i.EMPTY;
    public Rect2i worldRegion = Rect2i.EMPTY;
    public Rect2i gridWorldRegion = Rect2i.EMPTY;
    public Rect2i gridRelativeRegion = Rect2i.EMPTY;
    public int gridSize;
    public Vector2i center = new Vector2i();
    public List<Float> data = Lists.newArrayList();
    public float meanDeviation;

    public RoughnessFacetComponent() {
    }

    public RoughnessFacetComponent(RoughnessFacet roughnessFacet) {

        relativeRegion = copyRect2i(roughnessFacet.getRelativeRegion());
        worldRegion = copyRect2i(roughnessFacet.getWorldRegion());
        gridWorldRegion = copyRect2i(roughnessFacet.getGridWorldRegion());
        gridRelativeRegion = copyRect2i(roughnessFacet.getGridRelativeRegion());
        gridSize = roughnessFacet.getGridSize();
        center = new Vector2i(roughnessFacet.getCenter());
        for (int i = 0; i < roughnessFacet.getInternal().length; i++) {
            data.add(i, roughnessFacet.getInternal()[i]);
        }
        meanDeviation = roughnessFacet.getMeanDeviation();
    }


    private Rect2i copyRect2i(Rect2i value) {
        return Rect2i.createFromMinAndMax(value.minX(), value.minY(), value.maxX(), value.maxY());
    }

    //Copy of the methods used to access the data. Maybe there is a better way than storing them all here

    public Vector2i getWorldPoint(Vector2i gridPoint) {
        return getWorldPoint(gridPoint.x(), gridPoint.y());
    }

    public Vector2i getWorldPoint(int x, int y) {
        if (!gridWorldRegion.contains(x, y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y,
                    gridWorldRegion.toString()));
        }
        int xRelative = x - center.x();
        int yRelative = y - center.y();
        int xNew = center.x() + Math.round((float) xRelative * gridSize);
        int yNew = center.y() + Math.round((float) yRelative * gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!worldRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew,
                    worldRegion.toString()));
        }
        return gridPoint;
    }

    public Vector2i getRelativeGridPoint(Vector2i worldPoint) {
        return getRelativeGridPoint(worldPoint.x(), worldPoint.y());
    }

    public Vector2i getRelativeGridPoint(int x, int y) {
        if (!relativeRegion.contains(x, y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y,
                    relativeRegion.toString()));
        }
        int xNew = Math.round((float) x / gridSize);
        int yNew = Math.round((float) y / gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!gridRelativeRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew,
                    gridRelativeRegion.toString()));
        }
        return gridPoint;
    }

    public Vector2i getWorldGridPoint(Vector2i worldPoint) {
        return getRelativeGridPoint(worldPoint).add(center);
    }

    public Vector2i getWorldGridPoint(int x, int y) {
        if (!worldRegion.contains(x, y)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y,
                    worldRegion.toString()));
        }
        int xRelative = x - center.x();
        int yRelative = y - center.y();
        int xNew = center.x() + Math.round((float) xRelative / gridSize);
        int yNew = center.y() + Math.round((float) yRelative / gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!gridWorldRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", xNew, yNew,
                    gridWorldRegion.toString()));
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
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z,
                    gridWorldRegion.toString()));
        }
        return x - gridRelativeRegion.minX() + gridRelativeRegion.sizeX() * (z - gridRelativeRegion.minY());
    }

    protected int getWorldGridIndex(int x, int z) {
        if (!gridWorldRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z,
                    gridWorldRegion.toString()));
        }
        return x - gridWorldRegion.minX() + gridWorldRegion.sizeX() * (z - gridWorldRegion.minY());
    }

    public Rect2i getGridWorldRegion() {
        return gridWorldRegion;
    }

    public Rect2i getGridRelativeRegion() {
        return gridRelativeRegion;
    }

    public float get(int x, int y) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        return data.get(getRelativeGridIndex(gridPos.x(), gridPos.y()));
    }

    public float get(BaseVector2i pos) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        return get(gridPos.x(), gridPos.y());
    }

    public float getWorld(int x, int y) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        return data.get(getWorldGridIndex(gridPos.x(), gridPos.y()));
    }

    public float getWorld(BaseVector2i pos) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        return getWorld(gridPos.x(), gridPos.y());
    }

    public List<Float> getInternal() {
        return data;
    }

    public void set(int x, int y, float value) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        data.set(getRelativeGridIndex(gridPos.x(), gridPos.y()), value);
    }

    public void set(BaseVector2i pos, float value) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        set(pos.x(), pos.y(), value);
    }

    public void setWorld(int x, int y, float value) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        data.set(getWorldGridIndex(gridPos.x(), gridPos.y()), value);
    }

    public void setWorld(BaseVector2i pos, float value) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        setWorld(gridPos.x(), gridPos.y(), value);
    }

    public void set(List<Float> newData) {
        Preconditions.checkArgument(newData.size() == data.size(), "New data must have same length as existing");
        data.clear();
        data.addAll(newData);
    }

}
