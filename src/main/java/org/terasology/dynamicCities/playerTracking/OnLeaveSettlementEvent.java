// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.playerTracking;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;

public class OnLeaveSettlementEvent implements Event {
    private final EntityRef settlement;

    /**
     * @param settlement the settlement that was left
     */
    public OnLeaveSettlementEvent(EntityRef settlement) {
        this.settlement = settlement;
    }

    /**
     * @return the area that was left
     */
    public EntityRef getSettlement() {
        return settlement;
    }
}
