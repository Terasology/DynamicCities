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

import org.terasology.math.Region3i;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.facets.base.BaseFacet2D;


/**
 * This facet will allow to have only a small number of gridpoints embedded in a larger region.
 * It can be used as storage for world data after worldgeneration
 */
public abstract class Grid2DFacet extends BaseFacet2D {

    protected int gridSize;
    protected Vector2i center;
    protected Rect2i gridWorldRegion;
    protected Rect2i gridRelativeRegion;

    public Grid2DFacet(Region3i targetRegion, Border3D border, int gridSize) {
        super(targetRegion, border);
        this.gridSize = gridSize;
        Rect2i rect2i = getWorldRegion();
        gridWorldRegion = Rect2i.createFromMinAndMax(getWorldGridPoint(getWorldRegion().min()), getWorldGridPoint(getWorldRegion().max()));
        gridRelativeRegion = Rect2i.createFromMinAndMax(getRelativeGridPoint(getRelativeRegion().min()), getRelativeGridPoint(getRelativeRegion().max()));
        center = new Vector2i(rect2i.minX() + Math.round(rect2i.sizeX() / 2), rect2i.minY() + Math.round(rect2i.sizeY() / 2));
    }

    public Vector2i getWorldPoint(Vector2i gridPoint) {
        return center.add(gridPoint.sub(center).mul(gridSize));
    }

    public Vector2i getRelativeGridPoint(Vector2i worldPoint) {
        return getRelativeGridPoint(worldPoint.x(), worldPoint.y());
    }

    public Vector2i getRelativeGridPoint(int x, int y) {
        int xRelative = x-center.x();
        int yRelative = y-center.y();
        int xNew = Math.round(xRelative / gridSize);
        int yNew = Math.round(yRelative / gridSize);
        Vector2i gridPoint = new Vector2i(xNew, yNew);
        if (!gridWorldRegion.contains(gridPoint)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, y, gridWorldRegion.toString()));
        }
        return gridPoint;
    }

    public Vector2i getWorldGridPoint(Vector2i worldPoint) {
        return getRelativeGridPoint(worldPoint).add(center);
    }

    public Vector2i getWorldGridPoint(int x, int y) {
        return getRelativeGridPoint(x, y).add(center);
    }

    public int getGridSize() {
        return gridSize;
    }

    public Vector2i getCenter() {
        return center;
    }

    protected final int getRelativeGridIndex(int x, int z) {
        if (!gridRelativeRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, gridWorldRegion.toString()));
        }
        return x - gridRelativeRegion.minX() + gridRelativeRegion.sizeX() * (z - gridRelativeRegion.minY());
    }

    protected final int getWorldGridIndex(int x, int z) {
        if (!gridWorldRegion.contains(x, z)) {
            throw new IllegalArgumentException(String.format("Out of bounds: (%d, %d) for region %s", x, z, gridWorldRegion.toString()));
        }
        return x - gridWorldRegion.minX() + gridWorldRegion.sizeX() * (z - gridWorldRegion.minY());
    }
}
