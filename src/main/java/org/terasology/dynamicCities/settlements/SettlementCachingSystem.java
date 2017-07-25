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
package org.terasology.dynamicCities.settlements;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.minimap.events.AddDistrictOverlayEvent;
import org.terasology.dynamicCities.minimap.events.RemoveDistrictOverlayEvent;
import org.terasology.dynamicCities.playerTracking.OnEnterSettlementEvent;
import org.terasology.dynamicCities.settlements.components.ActiveSettlementComponent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.network.ClientComponent;
import org.terasology.network.NetworkComponent;
import org.terasology.network.NetworkSystem;
import org.terasology.network.events.DisconnectedEvent;
import org.terasology.registry.In;
import org.terasology.registry.Share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@Share(SettlementCachingSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class SettlementCachingSystem extends BaseComponentSystem {

    private EntityRef settlementEntities;

    @In
    private EntityManager entityManager;

    @In
    private NetworkSystem networkSystem;


    private Logger logger = LoggerFactory.getLogger(SettlementCachingSystem.class);

    private boolean isInitialised;

    @Override
    public void postBegin() {
        Iterator<EntityRef> settlementEntitiesIterator = entityManager.getEntitiesWith(SettlementsCacheComponent.class).iterator();
        settlementEntities = settlementEntitiesIterator.hasNext() ? settlementEntitiesIterator.next() : null;
        if (settlementEntities == null) {
            NetworkComponent networkComponent = new NetworkComponent();
            networkComponent.replicateMode = NetworkComponent.ReplicateMode.ALWAYS;
            SettlementsCacheComponent settlementsCacheComponent = new SettlementsCacheComponent();
            settlementsCacheComponent.settlementEntities = new HashMap<>();
            settlementsCacheComponent.networkCache = new ArrayList<>();
            settlementEntities = entityManager.create(settlementsCacheComponent, networkComponent);
            settlementEntities.setSectorScope(10);
            settlementEntities.setAlwaysRelevant(true);
        } else {
            SettlementsCacheComponent settlementsCacheComponent = settlementEntities.getComponent(SettlementsCacheComponent.class);
            if (settlementsCacheComponent.networkCache == null) {
                settlementsCacheComponent.networkCache = new ArrayList<>();
            }
            if (settlementsCacheComponent.settlementEntities == null) {
                settlementsCacheComponent.settlementEntities = new HashMap<>();
            }
        }
        isInitialised = true;

    }

    @ReceiveEvent(components = {ActiveSettlementComponent.class})
    public void registerSettlement(SettlementRegisterEvent event, EntityRef settlement) {
        SettlementsCacheComponent container = settlementEntities.getComponent(SettlementsCacheComponent.class);
        container.add(settlement);
        container.networkCache.add(settlement);
        settlementEntities.saveComponent(container);
    }

    @ReceiveEvent
    public void addOverlayToClient(OnEnterSettlementEvent event, EntityRef player) {
        player.send(new AddDistrictOverlayEvent());
    }

    @ReceiveEvent
    public void removeOverlayOfClient(DisconnectedEvent event, EntityRef client) {
        client.getComponent(ClientComponent.class).character.send(new RemoveDistrictOverlayEvent());
    }
    public SettlementsCacheComponent getSettlementEntitiesComponent() {
        if (settlementEntities.hasComponent(SettlementsCacheComponent.class)) {
            return settlementEntities.getComponent(SettlementsCacheComponent.class);
        } else {
            logger.error("No settlementEntitiesComponent found!");
            return null;
        }
    }
    public EntityRef getSettlementCacheEntity() {
        return settlementEntities;
    }

    public boolean isInitialised() {
        return isInitialised;
    }
}
