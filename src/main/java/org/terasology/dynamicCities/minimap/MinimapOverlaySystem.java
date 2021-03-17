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
package org.terasology.dynamicCities.minimap;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.minimap.events.AddCentreOverlayEvent;
import org.terasology.dynamicCities.minimap.events.AddDistrictOverlayEvent;
import org.terasology.dynamicCities.minimap.events.RemoveDistrictOverlayEvent;
import org.terasology.dynamicCities.settlements.SettlementsCacheComponent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.players.LocalPlayer;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkMode;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.registry.In;
import org.terasology.logic.players.MinimapSystem;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RegisterSystem(RegisterMode.CLIENT)
public class MinimapOverlaySystem extends BaseComponentSystem {


    @In
    private MinimapSystem minimapSystem;

    @In
    private LocalPlayer localPlayer;

    @In
    private EntityManager entityManager;

    @In
    private NetworkSystem networkSystem;

    private Logger logger = LoggerFactory.getLogger(MinimapOverlaySystem.class);

    private EntityRef clientEntity;

    private Map<EntityRef, Boolean> isOverlayAdded;

    private boolean isOverlaySinglePlayerAdded;

    @Override
    public void initialise() {
        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            clientEntity = networkSystem.getServer().getClientEntity();
        }
        isOverlayAdded = new HashMap<>();
    }

    /**
     * Checks network constraints and adds the DistrictOverlay to all settlement entities
     * @param event
     * @param entityRef
     */
    @ReceiveEvent
    public void onAddDistrictOverlayEvent(AddDistrictOverlayEvent event, EntityRef entityRef) {
        if (networkSystem.getMode() == NetworkMode.NONE && !isOverlaySinglePlayerAdded) {
            Iterator<EntityRef> entityRefs =  entityManager.getEntitiesWith(SettlementsCacheComponent.class).iterator();
            if (entityRefs.hasNext()) {
                minimapSystem.addOverlay(new DistrictOverlay((entityRefs.next())));
                isOverlaySinglePlayerAdded = true;
            } else {
                logger.error("No SettlementCache found! Unable to create district overlay");
            }
        } else if(isOverlaySinglePlayerAdded) {
            return;
        }
        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            if (clientEntity.getComponent(ClientComponent.class).character.getId() == entityRef.getId() && !isOverlayAdded.getOrDefault(entityRef, false)) {
                Iterator<EntityRef> entityRefs =  entityManager.getEntitiesWith(SettlementsCacheComponent.class).iterator();
                if (entityRefs.hasNext()) {
                    minimapSystem.addOverlay(new DistrictOverlay((entityRefs.next())));
                    isOverlayAdded.put(entityRef, true);
                } else {
                    logger.error("No SettlementCache found! Unable to create district overlay");
                }
            }
        }
        if (networkSystem.getMode() == NetworkMode.DEDICATED_SERVER && !isOverlayAdded.getOrDefault(entityRef, false)) {
            if (localPlayer.getCharacterEntity() == entityRef) {
                Iterator<EntityRef> entityRefs =  entityManager.getEntitiesWith(SettlementsCacheComponent.class).iterator();
                if (entityRefs.hasNext()) {
                    minimapSystem.addOverlay(new DistrictOverlay((entityRefs.next())));
                } else {
                    logger.error("No SettlementCache found! Unable to create district overlay");
                }
            }
        }
    }

    /**
     * Checks network constraints and adds the CentreOverlay to all settlement entities
     * @param event
     * @param entityRef
     */
    @ReceiveEvent
    public void onAddCentreOverlayEvent(AddCentreOverlayEvent event, EntityRef entityRef) {
        if (networkSystem.getMode() == NetworkMode.NONE) {
            Iterator<EntityRef> entities = entityManager.getEntitiesWith(SettlementsCacheComponent.class).iterator();
            if (entities.hasNext()) {
                minimapSystem.addOverlay(new CentreOverlay(entities.next()));
            }
        }

        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            if (clientEntity.getComponent(ClientComponent.class).character.getId() == entityRef.getId() && !isOverlayAdded.getOrDefault(entityRef, false)) {
                Iterator<EntityRef> entities = entityManager.getEntitiesWith(SettlementsCacheComponent.class).iterator();
                if (entities.hasNext()) {
                    minimapSystem.addOverlay(new CentreOverlay(entities.next()));
                }
            }
        }

        if (networkSystem.getMode() == NetworkMode.DEDICATED_SERVER && !isOverlayAdded.getOrDefault(entityRef, false)) {
            if (localPlayer.getCharacterEntity() == entityRef) {
                Iterator<EntityRef> entities = entityManager.getEntitiesWith(SettlementsCacheComponent.class).iterator();
                if (entities.hasNext()) {
                    minimapSystem.addOverlay(new CentreOverlay(entities.next()));
                }
            }
        }
    }

    /**
     * Removes DistrictOverlay from the map
     * @param event
     * @param entityRef
     */
    @ReceiveEvent
    public void onRemoveDistrictOverlayEvent(RemoveDistrictOverlayEvent event, EntityRef entityRef) {
        if (networkSystem.getMode() == NetworkMode.CLIENT) {
            if (clientEntity.getComponent(ClientComponent.class).character.equals(entityRef.getId())) {
                if (isOverlayAdded.containsKey(entityRef)) {
                    isOverlayAdded.replace(entityRef, false);
                }
            }
        }
    }


}
