// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements.components;


import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

public class MarketComponent implements Component<MarketComponent> {

    public EntityRef market;

    @Replicate
    public long marketId;

    public MarketComponent() {
    }
    public MarketComponent(EntityRef market) {
        this.market = market;
    }

    public long getMarketId() {
        return marketId;
    }

    @Override
    public void copy(MarketComponent other) {
        this.market = other.market;
        this.marketId = other.marketId;
    }
}
