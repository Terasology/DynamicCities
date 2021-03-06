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
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;


/**
 * This facet will allow to have only a small number of gridpoints embedded in a larger region.
 * It can be used as storage for world data after worldgeneration
 */

public abstract class Grid2DFloatFacet extends Grid2DFacet {

    protected float[] data;

    public Grid2DFloatFacet(BlockRegion targetRegion, Border3D border, int gridSize) {
        super(targetRegion, border, gridSize);
        this.data = new float[gridWorldRegion.area()];
    }


    public float get(int x, int y) {
        Vector2i gridPos = getRelativeGridPoint(x, y);
        return data[getRelativeGridIndex(gridPos.x(), gridPos.y())];
    }

    public float get(Vector2ic pos) {
        Vector2ic gridPos = getRelativeGridPoint(pos.x(), pos.y());
        return get(gridPos.x(), gridPos.y());
    }

    public float getWorld(int x, int y) {
        Vector2ic gridPos = getWorldGridPoint(x, y);
        return data[getWorldGridIndex(gridPos.x(), gridPos.y())];
    }

    public float getWorld(Vector2ic pos) {
        Vector2ic gridPos = getWorldGridPoint(pos.x(), pos.y());
        return getWorld(gridPos.x(), gridPos.y());
    }

    public float[] getInternal() {
        return data;
    }

    public void set(int x, int y, float value) {
        Vector2ic gridPos = getRelativeGridPoint(x, y);
        data[getRelativeGridIndex(gridPos.x(), gridPos.y())] = value;
    }

    public void set(Vector2ic pos, float value) {
        Vector2ic gridPos = getRelativeGridPoint(pos.x(), pos.y());
        set(pos.x(), pos.y(), value);
    }

    public void setWorld(int x, int y, float value) {
        Vector2ic gridPos = getWorldGridPoint(x,y);
        data[getWorldGridIndex(gridPos.x(), gridPos.y())] = value;
    }

    public void setWorld(Vector2ic pos, float value) {
        Vector2ic gridPos = getWorldGridPoint(pos.x(), pos.y());
        setWorld(gridPos.x(), gridPos.y(), value);
    }

    public void set(float[] newData) {
        Preconditions.checkArgument(newData.length == data.length, "New data must have same length as existing");
        System.arraycopy(newData, 0, data, 0, newData.length);
    }

}
