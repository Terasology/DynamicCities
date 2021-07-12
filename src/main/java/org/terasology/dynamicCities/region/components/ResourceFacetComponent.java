// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.dynamicCities.resource.Resource;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedContainer
public final class ResourceFacetComponent implements Component<ResourceFacetComponent> {

    public boolean privateToOwner = true;

    public BlockArea relativeRegion = new BlockArea(BlockArea.INVALID);
    public BlockArea worldRegion = new BlockArea(BlockArea.INVALID);
    public BlockArea gridWorldRegion = new BlockArea(BlockArea.INVALID);
    public BlockArea gridRelativeRegion = new BlockArea(BlockArea.INVALID);
    public int gridSize;
    public Vector2i center = new Vector2i();

    public List<Map<String, Resource>> data = Lists.newArrayList();

    public ResourceFacetComponent() { }

    public ResourceFacetComponent(ResourceFacet resourceFacet) {

        relativeRegion.set(resourceFacet.getRelativeArea());
        worldRegion.set(resourceFacet.getWorldArea());
        gridWorldRegion.set(resourceFacet.getGridWorldRegion());
        gridRelativeRegion.set(resourceFacet.getGridRelativeRegion());
        gridSize = resourceFacet.getGridSize();
        center = new Vector2i(resourceFacet.getCenter());
        for (int i = 0; i < resourceFacet.getInternal().length; i++) {
            HashMap<String, Resource> map = new HashMap<>();
            map.putAll(resourceFacet.getInternal()[i]);
            data.add(i, map);
        }
    }

    public int getResourceSum(String resourceType) {
        int sum = 0;
        for (Map<String, Resource> map : data) {
            if (map.containsKey(resourceType)) {
                sum += map.get(resourceType).amount;
            }
        }
        return sum;
    }

    /**
     * Copy of the methods used to access the data. Maybe there is a better way than storing them all here but @MappedContainer
     * wants flat hierarchies.
     */


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

    public Map<String, Resource> get(int x, int y) {
        Vector2ic gridPos = getRelativeGridPoint(x, y);
        return data.get(getRelativeGridIndex(gridPos.x(), gridPos.y()));
    }

    public Map<String, Resource> get(Vector2ic pos) {
        Vector2ic gridPos = getRelativeGridPoint(pos.x(), pos.y());
        return get(gridPos.x(), gridPos.y());
    }

    public Map<String, Resource> getWorld(int x, int y) {
        Vector2ic gridPos = getWorldGridPoint(x, y);
        return data.get(getWorldGridIndex(gridPos.x(), gridPos.y()));
    }

    public Map<String, Resource> getWorld(Vector2ic pos) {
        Vector2ic gridPos = getWorldGridPoint(pos.x(), pos.y());
        return getWorld(gridPos.x(), gridPos.y());
    }

    public List<Map<String, Resource>> getInternal() {
        return data;
    }

    public void set(int x, int y, Map<String, Resource> value) {
        Vector2ic gridPos = getRelativeGridPoint(x, y);
        data.set(getRelativeGridIndex(gridPos.x(), gridPos.y()), value);
    }

    public void set(Vector2ic pos, Map<String, Resource> value) {
        Vector2ic gridPos = getRelativeGridPoint(pos.x(), pos.y());
        set(pos.x(), pos.y(), value);
    }

    public void setWorld(int x, int y, Map<String, Resource> value) {
        Vector2ic gridPos = getWorldGridPoint(x, y);
        data.set(getWorldGridIndex(gridPos.x(), gridPos.y()), value);
    }

    public void setWorld(Vector2ic pos, Map<String, Resource> value) {
        Vector2ic gridPos = getWorldGridPoint(pos.x(), pos.y());
        setWorld(gridPos.x(), gridPos.y(), value);
    }

    public void set(List<Map<String, Resource>> newData) {
        Preconditions.checkArgument(newData.size() == data.size(), "New data must have same length as existing");
        data.clear();
        data.addAll(newData);
    }

    @Override
    public void copy(ResourceFacetComponent other) {
        this.relativeRegion.set(other.relativeRegion);
        this.worldRegion.set(other.worldRegion);
        this.gridWorldRegion.set(other.gridWorldRegion);
        this.gridRelativeRegion.set(other.gridRelativeRegion);
        this.gridSize = other.gridSize;
        this.center.set(other.center);
    }
}
