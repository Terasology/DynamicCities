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
package org.terasology.dynamicCities.region;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.region.components.ActiveRegionComponent;
import org.terasology.dynamicCities.region.components.RegionEntitiesComponent;
import org.terasology.dynamicCities.region.components.ResourceFacetComponent;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.UnassignedRegionComponent;
import org.terasology.dynamicCities.region.components.UnregisteredRegionComponent;
import org.terasology.dynamicCities.region.events.AssignRegionEvent;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Share(value = RegionEntityManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class RegionEntityManager extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private SettlementEntityManager settlementEntities;

    private RegionEntitiesComponent regionEntitiesComponent;


    private final int gridSize = 96;
    private Logger logger = LoggerFactory.getLogger(RegionEntityManager.class);
    @Override
    public void initialise() {
        regionEntitiesComponent = new RegionEntitiesComponent(gridSize);
    }

    @ReceiveEvent(components = {UnregisteredRegionComponent.class, LocationComponent.class, RoughnessFacetComponent.class, ResourceFacetComponent.class, NameTagComponent.class})
    public void registerRegion(OnActivatedComponent event, EntityRef region) {
        add(region);
        region.removeComponent(UnregisteredRegionComponent.class);
        region.addComponent(new UnassignedRegionComponent());
        NameTagComponent nT = region.getComponent(NameTagComponent.class);
        nT.textColor = Color.GREEN;
        region.saveComponent(nT);
    }

    @ReceiveEvent(components = {UnassignedRegionComponent.class})
    public void assignRegion(AssignRegionEvent event, EntityRef region) {
        region.addComponent(new ActiveRegionComponent());
        region.removeComponent(UnassignedRegionComponent.class);
        NameTagComponent nT = region.getComponent(NameTagComponent.class);
        nT.textColor = Color.YELLOW;
        region.saveComponent(nT);
    }

    
    public RegionEntitiesComponent getRegionEntitiesComponent() {
        return regionEntitiesComponent;
    }

    public void add(EntityRef region) {
        if (region != null) {
            Map<String, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
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
        Map<String, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
        return regionEntities.get(position.toString());
    }

    public EntityRef getNearest(Vector2i position) {
        Map<String, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
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
        Map<String, Integer> cellGrid = regionEntitiesComponent.cellGrid;
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
        Map<String, Integer> cellGrid =regionEntitiesComponent.cellGrid;
        int cellSize = regionEntitiesComponent.cellSize;
        return cellGrid.containsKey(getCellString(position)) && (cellGrid.get(getCellString(position)) == cellSize);
    }

    public boolean cellIsLoaded(String posString) {
        Map<String, Integer> cellGrid =regionEntitiesComponent.cellGrid;
        int cellSize = regionEntitiesComponent.cellSize;
        Vector2i position = Toolbox.stringToVector2i(posString);
        return cellGrid.containsKey(getCellString(position)) && (cellGrid.get(getCellString(position)) == cellSize);
    }

    public List<EntityRef> getRegionsInCell(Vector2i position) {
        int cellSize = regionEntitiesComponent.cellSize;
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
        return (cellIsLoaded(pos.addX(3 * gridSize)) && cellIsLoaded(pos.addX(-3 * gridSize))
                && cellIsLoaded(pos.addY(3 * gridSize)) && cellIsLoaded(pos.addY(-3 * gridSize)));
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
        List<String> processed = regionEntitiesComponent.processed;
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

    public List<EntityRef> getRegionsInArea(Rect2i area) {
        List<EntityRef> result = new ArrayList<>();
        for (BaseVector2i pos : area.contents()) {
            EntityRef region = getNearest(new Vector2i(pos.x(), pos.y()));
            if (region == null) {
                logger.error("Failed to get nearest region for " + pos.toString());
            }
            if (!result.contains(region)) {
                result.add(region);
            }
        }
        return result;
    }

    public List<EntityRef> getSurroundingRegions(Vector2i pos, int size) {
        Rect2i settlementRectArea = Rect2i.createFromMinAndMax(-size, -size, size, size);
        Vector2i regionWorldPos = new Vector2i();
        List<EntityRef> result = new ArrayList<>();
        for (BaseVector2i regionPos : settlementRectArea.contents()) {
            regionWorldPos.set(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            EntityRef region = getNearest(regionWorldPos);
            if (region == null) {
                //throw new NullPointerException();
            }
            if (region != null && region.hasComponent(UnassignedRegionComponent.class)) {
                result.add(region);
            }
        }
        return result;
    }
}
