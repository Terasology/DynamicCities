// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region;


import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.region.components.ActiveRegionComponent;
import org.terasology.dynamicCities.region.components.RegionEntitiesComponent;
import org.terasology.dynamicCities.region.components.RegionMainStoreComponent;
import org.terasology.dynamicCities.region.components.ResourceFacetComponent;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.UnassignedRegionComponent;
import org.terasology.dynamicCities.region.components.UnregisteredRegionComponent;
import org.terasology.dynamicCities.region.events.AssignRegionEvent;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.dynamicCities.sites.SiteComponent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.console.commandSystem.annotations.Command;
import org.terasology.logic.console.commandSystem.annotations.Sender;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.logic.permission.PermissionManager;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.nui.Color;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.chunks.Chunks;

import java.util.ArrayList;
import java.util.Iterator;
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
    private EntityRef regionStoreEntity;


    private final int gridSize = 96;
    private Logger logger = LoggerFactory.getLogger(RegionEntityManager.class);
    private boolean toggledNameTags = false;

    @Override
    public void postBegin() {
        Iterator<EntityRef> regionEntitiesIterator = entityManager.getEntitiesWith(RegionEntitiesComponent.class).iterator();
        while (regionEntitiesIterator.hasNext()) {
            EntityRef regionStore = regionEntitiesIterator.next();
            if (regionStore.hasComponent(RegionMainStoreComponent.class)) {
                regionStoreEntity = regionStore;
                regionEntitiesComponent = regionStore.getComponent(RegionEntitiesComponent.class);
                return;
            }
        }
        regionEntitiesComponent = new RegionEntitiesComponent(gridSize);
        regionStoreEntity = entityManager.create(regionEntitiesComponent, new RegionMainStoreComponent());
        regionStoreEntity.setAlwaysRelevant(true);
    }

    @ReceiveEvent(components = {UnregisteredRegionComponent.class, LocationComponent.class, RoughnessFacetComponent.class, ResourceFacetComponent.class})
    public void registerRegion(OnActivatedComponent event, EntityRef region) {
        add(region);
        region.removeComponent(UnregisteredRegionComponent.class);
        region.addComponent(new UnassignedRegionComponent());

    }

    @ReceiveEvent(components = {UnassignedRegionComponent.class})
    public void assignRegion(AssignRegionEvent event, EntityRef region) {
        region.addComponent(new ActiveRegionComponent());
        region.removeComponent(UnassignedRegionComponent.class);
    }

    public RegionEntitiesComponent getRegionEntitiesComponent() {
        return regionEntitiesComponent;
    }

    public void add(EntityRef region) {
        if (region != null) {
            Map<String, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
            Vector3f location = region.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
            Vector2i position = new Vector2i(location.x(), location.z());
            addCell(position);
            regionEntities.put(position.toString(), region);
            regionStoreEntity.saveComponent(regionEntitiesComponent);
        }
    }

    public void addDeleted(EntityRef region) {
        if (region != null) {
            Vector3f location = region.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
            Vector2i position = new Vector2i(location.x(), location.z());
            addCell(position);
        }
    }

    public EntityRef get(Vector2i position) {
        Map<String, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
        return regionEntities.get(position.toString());
    }

    public EntityRef getNearest(Vector2i position) {
        Map<String, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
        int x = Chunks.toChunkPosX(position.x) * Chunks.SIZE_X + ((Chunks.SIZE_X / 2) - 1);
        int y = Chunks.toChunkPosZ(position.y) * Chunks.SIZE_Z + ((Chunks.SIZE_Z / 2) - 1);

        Vector2i regionPos = new Vector2i(x, y);
        return regionEntities.get(regionPos.toString());
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

    public boolean checkSidesLoadedLong(Vector2i pos) {
        return (cellIsLoaded(pos.addX(3 * gridSize)) && cellIsLoaded(pos.addX(-3 * gridSize))
                && cellIsLoaded(pos.addY(3 * gridSize)) && cellIsLoaded(pos.addY(-3 * gridSize)));
    }

    public boolean checkSidesLoadedLong(EntityRef region) {
        LocationComponent regionLocation = region.getComponent(LocationComponent.class);
        Vector2i pos = getCellVector(new Vector2i(regionLocation.getLocalPosition().x(), regionLocation.getLocalPosition().z()));
        return checkSidesLoadedLong(pos);
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

    public void setNameTagForRegion(EntityRef region) {
        if (!region.iterateComponents().iterator().hasNext()) {
            logger.error("Region with no components found!");
            return;
        }
        NameTagComponent nT = new NameTagComponent();
        RoughnessFacetComponent roughnessFacetComponent = region.getComponent(RoughnessFacetComponent.class);
        ResourceFacetComponent resourceFacetComponent = region.getComponent(ResourceFacetComponent.class);
        LocationComponent locationComponent = region.getComponent(LocationComponent.class);
        SiteComponent siteComponent = region.getComponent(SiteComponent.class);
        nT.text = "Roughness: "
                + roughnessFacetComponent.meanDeviation + " Grass: " + resourceFacetComponent.getResourceSum("Grass")
                + locationComponent.getWorldPosition(new Vector3f()).toString();
        nT.yOffset = 10;
        nT.scale = 10;

        if (region.hasComponent(UnassignedRegionComponent.class)) {
            nT.textColor = Color.GREEN;
        } else if (region.hasComponent(UnregisteredRegionComponent.class)) {
            nT.textColor = Color.WHITE;
        } else if (region.hasComponent(ActiveRegionComponent.class)) {
            nT.textColor = Color.YELLOW;
        }

        if (siteComponent != null) {
            nT.text += " SiteComponent";
        }
        region.addComponent(nT);
    }

    @Command(shortDescription = "Toggles the view of nametags for region entities", runOnServer = true,
            requiredPermission = PermissionManager.DEBUG_PERMISSION)
    public String toggleRegionTags(
            @Sender EntityRef client) {
        if (toggledNameTags) {
            for (EntityRef region : regionEntitiesComponent.regionEntities.values()) {
                region.removeComponent(NameTagComponent.class);
            }
            toggledNameTags = false;
            return "Region tags disabled";
        } else {
            for (EntityRef region : regionEntitiesComponent.regionEntities.values()) {
                setNameTagForRegion(region);
            }
            toggledNameTags = true;
            return "Region tags enabled";
        }
    }

    public List<EntityRef> getRegionsInArea(Rect2i area) {
        List<EntityRef> result = new ArrayList<>();
        for (BaseVector2i pos : area.contents()) {
            EntityRef region = getNearest(new Vector2i(pos.x(), pos.y()));

            if (region == null || !region.isActive() || !region.exists()) {
                logger.debug("Failed to get nearest region for " + pos.toString());
            } else if (!result.contains(region)) {
                result.add(region);
            }
        }
        return result;
    }
 /*
    public List<EntityRef> getRegionsInArea(Rect2i area) {
        List<EntityRef> result = new ArrayList<>();
        Vector2i regionWorldPos = new Vector2i();
        for (BaseVector2i regionPos : area.contents()) {
            regionWorldPos.set(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            EntityRef region = getNearest(regionWorldPos);
            if (region != null && region.hasComponent(UnassignedRegionComponent.class)) {
                result.add(region);
            }
        }
        return result;
    }*/
    /* Unused and buggy


    public List<EntityRef> getSurroundingRegions(Vector2i pos, int size) {
        Rect2i settlementRectArea = Rect2i.createFromMinAndMax(-size, -size, size, size);
        Vector2i regionWorldPos = new Vector2i();
        List<EntityRef> result = new ArrayList<>();
        for (BaseVector2i regionPos : settlementRectArea.contents()) {
            regionWorldPos.set(pos.x() + regionPos.x() * 32, pos.y() + regionPos.y() * 32);

            EntityRef region = getNearest(regionWorldPos);
            if (region != null && region.hasComponent(UnassignedRegionComponent.class)) {
                result.add(region);
            }
        }
        return result;
    }
    */
}
