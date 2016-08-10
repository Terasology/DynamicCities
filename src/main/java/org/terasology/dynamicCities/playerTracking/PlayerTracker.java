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

package org.terasology.dynamicCities.playerTracking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.parcels.ParcelList;
import org.terasology.dynamicCities.settlements.SettlementCachingSystem;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.characters.events.OnEnterBlockEvent;
import org.terasology.logic.console.Console;
import org.terasology.logic.location.LocationComponent;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.math.geom.Circle;
import org.terasology.math.geom.Vector2f;
import org.terasology.math.geom.Vector3f;
import org.terasology.network.Client;
import org.terasology.network.NetworkSystem;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.FontColor;
import org.terasology.world.WorldComponent;
import org.terasology.world.chunks.event.PurgeWorldEvent;

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
     * @param event the event
     * @param entity the character entity reference "player:engine"
     */
    @ReceiveEvent
    public void onEnterBlock(OnEnterBlockEvent event, EntityRef entity) {
        LocationComponent loc = entity.getComponent(LocationComponent.class);
        Vector3f worldPos3d = loc.getWorldPosition();
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

            Circle circle = new Circle(site.getLocalPosition().x(), site.getLocalPosition().z(), radius);
            if (circle.contains(worldPos)) {
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
     * @param event the event

     * @param entity the character entity reference "player:engine"
     */

    @ReceiveEvent
    public void onEnterArea(OnEnterSettlementEvent event, EntityRef entity) {

        Client client = networkSystem.getOwner(entity);
        String playerName = String.format("%s (%s)", client.getName(), client.getId());
        String areaName = event.getSettlement().getComponent(NameTagComponent.class).text;

        playerName = FontColor.getColored(playerName, CitiesColors.PLAYER);
        areaName = FontColor.getColored(areaName, CitiesColors.AREA);

        console.addMessage(playerName + " entered " + areaName);
    }

    /**
     * Called whenever a named area is entered
     * @param event the event
     * @param entity the character entity reference "player:engine"
     */
    @ReceiveEvent
    public void onLeaveArea(OnLeaveSettlementEvent event, EntityRef entity) {

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

    public Map getPlayerCityMap() {
        return prevLoc;
    }

}
