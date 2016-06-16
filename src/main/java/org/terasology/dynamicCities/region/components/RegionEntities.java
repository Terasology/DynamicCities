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


import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.reflection.MappedContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@MappedContainer
public class RegionEntities implements Component {

    public Map<String, EntityRef> regionEntities;
    public int gridSize;
    //This stores information about the loaded state of several regions packed into a cell
    public Map<String, Integer> cellGrid;
    public List<String> processed;
    public int cellSize;

    public RegionEntities() {
        regionEntities = new HashMap<>();
        cellGrid = new HashMap<>();
        processed = new ArrayList<>();
    }

    public RegionEntities(int gridSize) {
        regionEntities = new HashMap<>();
        cellGrid = new HashMap<>();
        processed = new ArrayList<>();
        this.gridSize = gridSize;
        cellSize = gridSize * gridSize / (32 * 32);
    }

    public void add(EntityRef region) {
        if (region != null) {
            LocationComponent location = region.getComponent(LocationComponent.class);
            Vector2i position = new Vector2i(location.getWorldPosition().x(), location.getWorldPosition().z());
            addCell(position);
            regionEntities.put(position.toString(), region);
        }
    }

    public void addDeleted(EntityRef region) {
        if (region != null) {
            LocationComponent location = region.getComponent(LocationComponent.class);
            Vector2i position = new Vector2i(location.getWorldPosition().x(), location.getWorldPosition().z());
            addCell(position);
        }
    }

    public EntityRef get(Vector2i position) {
        return regionEntities.get(position.toString());
    }

    public EntityRef getNearest(Vector2i position) {
        float x = position.x();
        float y = position.y();
        Vector2i regionPos = new Vector2i(Math.round((x - Math.signum(x) * 16) / 32) * 32 + Math.signum(x) * 16,
                Math.round((y - Math.signum(y) * 16) / 32) * 32 + Math.signum(y) * 16);
        return regionEntities.get(regionPos.toString());
    }

    public EntityRef getNearest(String posString) {
        return getNearest(Toolbox.stringToVector2i(posString));
    }

    public void addCell(Vector2i position) {
        String cellPos = getCellString(position);
        if (cellGrid.containsKey(cellPos)) {
            int count = cellGrid.get(cellPos);
            cellGrid.replace(cellPos, count + 1);
        } else {
            cellGrid.put(cellPos, 1);
        }
    }

    public String getCellString(Vector2i position) {
        float x = position.x();
        float y = position.y();
        Vector2i cellPos = new Vector2i(Math.round(x / gridSize) * gridSize,
                Math.round(y / gridSize) * gridSize);
        return cellPos.toString();
    }

    public Vector2i getCellVector(Vector2i position) {
        float x = position.x();
        float y = position.y();
        Vector2i cellPos = new Vector2i(Math.round(x / gridSize) * gridSize,
                Math.round(y / gridSize) * gridSize);
        return cellPos;
    }

    public boolean cellIsLoaded(Vector2i position) {
        return cellGrid.containsKey(getCellString(position)) && (cellGrid.get(getCellString(position)) == cellSize);
    }

    public boolean cellIsLoaded(String posString) {
        Vector2i position = Toolbox.stringToVector2i(posString);
        return cellGrid.containsKey(getCellString(position)) && (cellGrid.get(getCellString(position)) == cellSize);
    }

    public List<EntityRef> getRegionsInCell(Vector2i position) {
        List<EntityRef> regions = new ArrayList<>();

        Vector2i cellCenter = getCellVector(position);
        int edgeLength = Math.round((float)Math.sqrt(cellSize));
        Rect2i cellRegion = Rect2i.createFromMinAndMax(-edgeLength, -edgeLength, edgeLength, edgeLength);
        Vector2i regionWorldPos = new Vector2i();
        for (BaseVector2i pos : cellRegion.contents()) {

            regionWorldPos.set(cellCenter.x() + (int) Math.signum(pos.x()) * ((TeraMath.fastAbs(pos.x()) - 1) * 32 + 16),
                    cellCenter.y() + (int) Math.signum(pos.y()) * ((TeraMath.fastAbs(pos.y()) - 1) * 32 + 16));
            EntityRef region = getNearest(regionWorldPos);

            if (region != null) {
                regions.add(region);
            }
        }

        return regions;
    }

    public List<EntityRef> getRegionsInCell(EntityRef region) {
        LocationComponent regionLocation = region.getComponent(LocationComponent.class);
        Vector2i pos = new Vector2i(regionLocation.getLocalPosition().x(), regionLocation.getLocalPosition().z());
        return  getRegionsInCell(pos);
    }
    public List<EntityRef> getRegionsInCell(String posString) {
        return  getRegionsInCell(Toolbox.stringToVector2i(posString));
    }



    public boolean checkSidesLoadedLong(Vector2i pos) {
        return (cellIsLoaded(pos.addX(2 * gridSize)) && cellIsLoaded(pos.addX(-2 * gridSize))
                && cellIsLoaded(pos.addY(2 * gridSize)) && cellIsLoaded(pos.addY(-2 * gridSize)));
    }

    public boolean checkSidesLoadedLong(EntityRef region) {
        LocationComponent regionLocation = region.getComponent(LocationComponent.class);
        Vector2i pos = getCellVector(new Vector2i(regionLocation.getLocalPosition().x(), regionLocation.getLocalPosition().z()));
        return checkSidesLoadedLong(pos);
    }

    public boolean checkSidesLoadedLong(String posString) {
        return checkSidesLoadedLong(Toolbox.stringToVector2i(posString));
    }


    public boolean checkSidesLoadedNear(Vector2i pos) {
        return (cellIsLoaded(pos.addX(gridSize)) && cellIsLoaded(pos.addX(-gridSize))
                && cellIsLoaded(pos.addY(gridSize)) && cellIsLoaded(pos.addY(-gridSize)));
    }

    public boolean checkSidesLoadedNear(EntityRef region) {
        LocationComponent regionLocation = region.getComponent(LocationComponent.class);
        Vector2i pos = getCellVector(new Vector2i(regionLocation.getLocalPosition().x(), regionLocation.getLocalPosition().z()));
        return checkSidesLoadedNear(pos);
    }

    public boolean checkFullLoaded(Vector2i pos) {
        Rect2i cube = Rect2i.createFromMinAndMax(-1, -1, 1, 1);
        Vector2i cellPos = new Vector2i();
        for(BaseVector2i cubePos : cube.contents()) {
            cellPos.set(pos.x() + cubePos.x() * gridSize, pos.y() + cubePos.y() * gridSize);
            if (!cellIsLoaded(cellPos)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkFullLoaded(EntityRef region) {
        LocationComponent regionLocation = region.getComponent(LocationComponent.class);
        Vector2i pos = getCellVector(new Vector2i(regionLocation.getLocalPosition().x(), regionLocation.getLocalPosition().z()));
        return checkFullLoaded(pos);
    }


    //maybe add variable component filters here
    public void clearCell(Vector2i pos) {
        for (EntityRef region : getRegionsInCell(pos)) {
            if (!region.hasComponent(ActiveRegionComponent.class)) {
                region.destroy();
            }
        }

        processed.add(pos.toString());
    }

    public void clearCell(String posString) {
        clearCell(Toolbox.stringToVector2i(posString));
    }

}


