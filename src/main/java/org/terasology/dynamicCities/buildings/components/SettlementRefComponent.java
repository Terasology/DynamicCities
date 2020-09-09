// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings.components;


import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.network.Replicate;

public final class SettlementRefComponent implements Component {

    @Replicate
    public EntityRef settlement;

    public SettlementRefComponent() {

    }

    public SettlementRefComponent(EntityRef settlement) {
        this.settlement = settlement;
    }
}
