// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region;

import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector2ic;
import org.joml.Vector3f;
import org.joml.Vector3fc;
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
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.internal.EntityScope;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.OnActivatedComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.console.commandSystem.annotations.Command;
import org.terasology.engine.logic.console.commandSystem.annotations.Sender;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.nameTags.NameTagComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.block.BlockArea;
import org.terasology.engine.world.block.BlockAreac;
import org.terasology.engine.world.chunks.Chunks;
import org.terasology.math.TeraMath;
import org.terasology.nui.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Share(value = RegionEntityManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class RegionEntityManager extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(RegionEntityManager.class);

    @In
    private EntityManager entityManager;

    @In
    private SettlementEntityManager settlementEntities;

    private RegionEntitiesComponent regionEntitiesComponent;
    private EntityRef regionStoreEntity;


    private final int gridSize = 96;
    private boolean toggledNameTags = false;

    @Override
    public void postBegin() {
        for (EntityRef regionStore : entityManager.getEntitiesWith(RegionEntitiesComponent.class)) {
            if (regionStore.hasComponent(RegionMainStoreComponent.class)) {
                regionStoreEntity = regionStore;
                regionEntitiesComponent = regionStore.getComponent(RegionEntitiesComponent.class);
                return;
            }
        }
        regionEntitiesComponent = new RegionEntitiesComponent(gridSize);
        regionStoreEntity = entityManager.create(regionEntitiesComponent, new RegionMainStoreComponent());
        regionStoreEntity.setScope(EntityScope.GLOBAL);
    }

    @ReceiveEvent(components = {UnregisteredRegionComponent.class, LocationComponent.class, RoughnessFacetComponent.class,
            ResourceFacetComponent.class})
    public void registerRegion(OnActivatedComponent event, EntityRef region) {
        add(region);
        region.removeComponent(UnregisteredRegionComponent.class);
        region.addComponent(new UnassignedRegionComponent());
    }

    @ReceiveEvent(components = UnassignedRegionComponent.class)
    public void assignRegion(AssignRegionEvent event, EntityRef region) {
        region.addComponent(new ActiveRegionComponent());
        region.removeComponent(UnassignedRegionComponent.class);
    }

    public RegionEntitiesComponent getRegionEntitiesComponent() {
        return regionEntitiesComponent;
    }

    public void add(EntityRef region) {
        if (region != null) {
            Map<Vector2i, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
            Vector3f location = region.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
            Vector2i position = new Vector2i(location.x(), location.z(), RoundingMode.FLOOR);
            addCell(position);
            regionEntities.put(position, region);
            regionStoreEntity.saveComponent(regionEntitiesComponent);
        }
    }

    public void addDeleted(EntityRef region) {
        if (region != null) {
            LocationComponent location = region.getComponent(LocationComponent.class);
            Vector3f loc = location.getWorldPosition(new Vector3f());
            Vector2i position = new Vector2i(loc.x(), loc.z(), RoundingMode.FLOOR);
            addCell(position);
        }
    }

    public EntityRef get(Vector2ic position) {
        Map<Vector2i, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
        return regionEntities.get(position);
    }

    public EntityRef getNearest(Vector2ic position) {
        Map<Vector2i, EntityRef> regionEntities = regionEntitiesComponent.regionEntities;
        int x = Chunks.toChunkPosX(position.x()) * Chunks.SIZE_X + ((Chunks.SIZE_X / 2) - 1);
        int y = Chunks.toChunkPosZ(position.y()) * Chunks.SIZE_Z + ((Chunks.SIZE_Z / 2) - 1);

        Vector2i regionPos = new Vector2i(x, y);
        return regionEntities.get(regionPos);
    }

    public void addCell(Vector2ic position) {
        Vector2i cellPos = toCellPos(position);
        Map<Vector2i, Integer> cellGrid = regionEntitiesComponent.cellGrid;
        if (cellGrid.containsKey(cellPos)) {
            int count = cellGrid.get(cellPos);
             cellGrid.replace(cellPos, count + 1);
        } else {
            cellGrid.put(cellPos, 1);
        }
    }

    private Vector2i toCellPos(Vector2ic pos) {
        return new Vector2i(Math.round(((float) pos.x()) / gridSize) * gridSize, Math.round(((float) pos.y()) / gridSize) * gridSize);
    }


    public boolean cellIsLoaded(Vector2ic position) {
        Map<Vector2i, Integer> cellGrid = regionEntitiesComponent.cellGrid;
        Vector2i cellPos = toCellPos(position);
        return cellGrid.containsKey(cellPos) && (cellGrid.get(cellPos) == regionEntitiesComponent.cellSize);
    }

    public List<EntityRef> getRegionsInCell(Vector2ic position) {
        int cellSize = regionEntitiesComponent.cellSize;
        List<EntityRef> regions = new ArrayList<>();

        Vector2i cellCenter = toCellPos(position);
        int edgeLength = Math.round((float) Math.sqrt(cellSize));
        BlockArea cellRegion = new BlockArea(-edgeLength, -edgeLength, edgeLength, edgeLength);
        Vector2i regionWorldPos = new Vector2i();
        for (Vector2ic pos : cellRegion) {

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
        Vector3fc loc = regionLocation.getLocalPosition();
        Vector2i pos = new Vector2i(loc.x(), loc.z(), RoundingMode.FLOOR);
        return getRegionsInCell(pos);
    }

    public boolean checkSidesLoadedLong(Vector2ic pos) {
        Vector2i temp = new Vector2i();
        return (cellIsLoaded(pos.add(3 * gridSize, 0, temp)) && cellIsLoaded(pos.add(-3 * gridSize, 0, temp))
            && cellIsLoaded(pos.add(0, 3 * gridSize, temp)) && cellIsLoaded(pos.add(0, -3 * gridSize, temp)));
    }

    public boolean checkSidesLoadedLong(EntityRef region) {
        LocationComponent regionLocation = region.getComponent(LocationComponent.class);
        Vector3fc loc = regionLocation.getLocalPosition();
        Vector2i pos = toCellPos(new Vector2i(loc.x(), loc.z(), RoundingMode.FLOOR));
        return checkSidesLoadedLong(pos);
    }

    public boolean checkSidesLoadedNear(Vector2ic pos) {
        Vector2i temp = new Vector2i();
        //FIXME: the old logic did in-place mutation of the vector, and just checked the following cells:
        //          (pos.x, pos.y), (pos.x + gridSize, pos.y), (pos.x, pos.y), (pos.x, pos.y + gridSize)
        //       the code looked like it should check the four adjacent cells, but this will result in too many restrictions,
        //       not finding any suitable location at all (in MetalRenegades)
//        return (cellIsLoaded(pos.add(gridSize,0, temp)) && cellIsLoaded(pos.add(-gridSize, 0, temp))
//                && cellIsLoaded(pos.add(0, gridSize, temp)) && cellIsLoaded(pos.add(0, -gridSize, temp))); // horribly hacky logic but ok?
        return cellIsLoaded(pos)
                && cellIsLoaded(pos.add(gridSize, 0, temp))
                && cellIsLoaded(pos.add(0, gridSize, temp));
    }

    public boolean checkSidesLoadedNear(EntityRef region) {
        LocationComponent regionLocation = region.getComponent(LocationComponent.class);
        Vector3fc loc = regionLocation.getLocalPosition();
        Vector2i pos = toCellPos(new Vector2i(loc.x(), loc.z(), RoundingMode.FLOOR));
        return checkSidesLoadedNear(pos);
    }

    public boolean checkFullLoaded(Vector2ic pos) {
        BlockArea cube = new BlockArea(-1, -1, 1, 1);
        Vector2i cellPos = new Vector2i();
        for (Vector2ic cubePos : cube) {
            cellPos.set(pos.x() + cubePos.x() * gridSize, pos.y() + cubePos.y() * gridSize);
            if (!cellIsLoaded(cellPos)) {
                return false;
            }
        }
        return true;
    }

    public boolean checkFullLoaded(EntityRef region) {
        LocationComponent regionLocation = region.getComponent(LocationComponent.class);
        Vector3fc loc = regionLocation.getLocalPosition();
        Vector2i pos = toCellPos(new Vector2i(loc.x(), loc.z(), RoundingMode.FLOOR));
        return checkFullLoaded(pos);
    }


    //maybe add variable component filters here
    public void clearCell(Vector2ic pos) {
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
                + locationComponent.getWorldPosition(new Vector3f());
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

    @Command(shortDescription = "Toggles the view of name tags for region entities", runOnServer = true)
    public String toggleRegionTags(@Sender EntityRef client) {
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

    public List<EntityRef> getRegionsInArea(BlockAreac area) {
        List<EntityRef> result = new ArrayList<>();
        for (Vector2ic pos : area) {
            EntityRef region = getNearest(new Vector2i(pos.x(), pos.y()));

            if (region == null || !region.isActive() || !region.exists()) {
                logger.debug("Failed to get nearest region for {}", pos);
            } else if (!result.contains(region)) {
                result.add(region);
            }
        }
        return result;
    }
}
