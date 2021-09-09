// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.playerTracking;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.commonworld.geom.CircleUtility;
import org.terasology.dynamicCities.buildings.components.SettlementRefComponent;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.settlements.SettlementCachingSystem;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.characters.events.OnEnterBlockEvent;
import org.terasology.engine.logic.console.Console;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.logic.nameTags.NameTagComponent;
import org.terasology.engine.network.Client;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.WorldComponent;
import org.terasology.engine.world.chunks.event.PurgeWorldEvent;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;
import org.terasology.joml.geom.Circlef;
import org.terasology.nui.FontColor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Tracks player movements with respect to {@link EntityRef} entities.
 */
@Share(value = PlayerTracker.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class PlayerTracker extends BaseComponentSystem {

    private static final Logger logger = LoggerFactory.getLogger(PlayerTracker.class);

    @In
    private NetworkSystem networkSystem;

    @In
    private Console console;

    @In
    private SettlementEntityManager settlementEntityManager;

    @In
    private SettlementCachingSystem settlementCachingSystem;

    private final Map<EntityRef, EntityRef> prevLoc = new HashMap<>();

    /**
     * Called whenever a block is entered
     *
     * @param event the event
     * @param entity the character entity reference "player:engine"
     */
    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f worldPos3d = loc.getWorldPosition(new Vector3f());
        Vector2f worldPos = new Vector2f(worldPos3d.x, worldPos3d.z);
        SettlementsCacheComponent knownSettlements = settlementCachingSystem.getSettlementEntitiesComponent();
        Client client = networkSystem.getOwner(entity);

        // TODO: entity can be AI-controlled, too. These don't have an owner
        if (client == null) {
            return;
        }

        String id = client.getId();
        String name = client.getName();

        EntityRef newSettlement = null;
        for (EntityRef settlement : knownSettlements.settlementEntities.values()) {
            float radius = 0;
            if (settlement.hasComponent(ParcelList.class)) {
                radius = settlement.getComponent(ParcelList.class).builtUpRadius;
            } else {
                return;
            }
            LocationComponent site;
            if (settlement.hasComponent(LocationComponent.class)) {
                site = settlement.getComponent(LocationComponent.class);
            } else {
                return;
            }

            Circlef circle = new Circlef(site.getLocalPosition().x(), site.getLocalPosition().z(), radius);
            if (CircleUtility.contains(circle, worldPos.x, worldPos.y)) {
                if (newSettlement != null) {
                    logger.warn("{} appears to be in {} and {} at the same time!", name, newSettlement.getComponent(NameTagComponent.class).text,
                        settlement.getComponent(NameTagComponent.class).text);
                }

                newSettlement = settlement;
            }
        }

        if (!Objects.equals(newSettlement, prevLoc.get(entity))) {       // both can be null
            if (newSettlement != null) {
                entity.send(new OnEnterSettlementEvent(newSettlement));
            }
            EntityRef prevArea = prevLoc.put(entity, newSettlement);
            if (prevArea != null) {
                entity.send(new OnLeaveSettlementEvent(prevArea));
            }
        }
    }

    /**
     * Called whenever a named area is entered
     *
     * @param event the event
     * @param entity the character entity reference "player:engine"
     */

    @ReceiveEvent
    public void onEnterArea(OnEnterSettlementEvent event, EntityRef entity) {
        entity.addComponent(new SettlementRefComponent(event.getSettlement()));

        Client client = networkSystem.getOwner(entity);
        String playerName = String.format("%s (%s)", client.getName(), client.getId());
        String areaName = event.getSettlement().getComponent(NameTagComponent.class).text;

        playerName = FontColor.getColored(playerName, CitiesColors.PLAYER);
        areaName = FontColor.getColored(areaName, CitiesColors.AREA);

        console.addMessage(playerName + " entered " + areaName);
    }

    /**
     * Called whenever a named area is entered
     *
     * @param event the event
     * @param entity the character entity reference "player:engine"
     */
    @ReceiveEvent
    public void onLeaveArea(OnLeaveSettlementEvent event, EntityRef entity) {
        entity.removeComponent(SettlementRefComponent.class);

        Client client = networkSystem.getOwner(entity);
        String playerName = String.format("%s (%s)", client.getName(), client.getId());
        String areaName = event.getSettlement().getComponent(NameTagComponent.class).text;

        playerName = FontColor.getColored(playerName, CitiesColors.PLAYER);
        areaName = FontColor.getColored(areaName, CitiesColors.AREA);

        console.addMessage(playerName + " left " + areaName);
    }

    @ReceiveEvent(components = {WorldComponent.class})
    public void onPurgeWorld(PurgeWorldEvent event, EntityRef worldEntity) {
        prevLoc.clear();
    }

    public Map<EntityRef, EntityRef> getPlayerCityMap() {
        return prevLoc;
    }

}
