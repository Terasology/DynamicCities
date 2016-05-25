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
package org.terasology.dynamicCities.facets;

import com.google.common.base.Preconditions;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.world.generation.Border3D;

import java.lang.reflect.Array;


/**
 * Base class for storing objects of the specified type in a 2D grid for a facet.
 * This facet will allow to have only a small number of gridpoints embedded in a larger region.
 * It can be used as storage for world data after worldgeneration
 * @param <T> Type of objects stored.
 */
public abstract class Grid2DObjectFacet<T> extends Grid2DFacet {

    private T[] data;

    @SuppressWarnings(value = "unchecked")
    public Grid2DObjectFacet(Region3i targetRegion, Border3D border, int gridSize, Class<T> objectType) {
        super(targetRegion, border, gridSize);
        this.data = (T[]) Array.newInstance(objectType, gridWorldRegion.area());
    }


    public T get(int x, int y) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        return data[getRelativeGridIndex(gridPos.x(), gridPos.y())];
    }

    public T get(BaseVector2i pos) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        return get(gridPos.x(), gridPos.y());
    }

    public T getWorld(int x, int y) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        return data[getWorldGridIndex(gridPos.x(), gridPos.y())];
    }

    public T getWorld(BaseVector2i pos) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        return getWorld(gridPos.x(), gridPos.y());
    }

    public T[] getInternal() {
        return data;
    }

    public void set(int x, int y, T value) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        data[getRelativeGridIndex(gridPos.x(), gridPos.y())] = value;
    }

    public void set(BaseVector2i pos, T value) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        set(pos.x(), pos.y(), value);
    }

    public void setWorld(int x, int y, T value) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        data[getWorldGridIndex(gridPos.x(), gridPos.y())] = value;
    }

    public void setWorld(BaseVector2i pos, T value) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        setWorld(gridPos.x(), gridPos.y(), value);
    }

    public void set(T[] newData) {
        Preconditions.checkArgument(newData.length == data.length, "New data must have same length as existing");
        System.arraycopy(newData, 0, data, 0, newData.length);
    }

}
