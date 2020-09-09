// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements.components;


import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;

public class MarketComponent implements Component {

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
}
