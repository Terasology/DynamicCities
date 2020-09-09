// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.minimap.events.AddCentreOverlayEvent;
import org.terasology.dynamicCities.minimap.events.AddDistrictOverlayEvent;
import org.terasology.dynamicCities.minimap.events.RemoveDistrictOverlayEvent;
import org.terasology.dynamicCities.playerTracking.OnEnterSettlementEvent;
import org.terasology.dynamicCities.settlements.components.ActiveSettlementComponent;
import org.terasology.dynamicCities.settlements.events.SettlementRegisterEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.network.ClientComponent;
import org.terasology.engine.network.NetworkComponent;
import org.terasology.engine.network.NetworkSystem;
import org.terasology.engine.network.events.DisconnectedEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

@Share(SettlementCachingSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class SettlementCachingSystem extends BaseComponentSystem {

    private final Logger logger = LoggerFactory.getLogger(SettlementCachingSystem.class);
    private EntityRef settlementEntities;
    @In
    private EntityManager entityManager;
    @In
    private NetworkSystem networkSystem;
    private boolean isInitialised;

    @Override
    public void postBegin() {
        Iterator<EntityRef> settlementEntitiesIterator =
                entityManager.getEntitiesWith(SettlementsCacheComponent.class).iterator();
        settlementEntities = settlementEntitiesIterator.hasNext() ? settlementEntitiesIterator.next() : null;
        if (settlementEntities == null) {
            NetworkComponent networkComponent = new NetworkComponent();
            networkComponent.replicateMode = NetworkComponent.ReplicateMode.ALWAYS;
            SettlementsCacheComponent settlementsCacheComponent = new SettlementsCacheComponent();
            settlementsCacheComponent.settlementEntities = new HashMap<>();
            settlementsCacheComponent.networkCache = new ArrayList<>();
            settlementEntities = entityManager.create(settlementsCacheComponent, networkComponent);
            settlementEntities.setAlwaysRelevant(true);
        } else {
            SettlementsCacheComponent settlementsCacheComponent =
                    settlementEntities.getComponent(SettlementsCacheComponent.class);
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
        player.send(new AddCentreOverlayEvent());
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
