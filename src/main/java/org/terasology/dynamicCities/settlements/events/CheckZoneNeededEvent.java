// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements.events;


import org.terasology.engine.entitySystem.event.ConsumableEvent;

/**
 * Sent to check whether an entity needs a parcel of the given zone type
 */
public class CheckZoneNeededEvent implements ConsumableEvent {
    public boolean needed;
    public String zone;
    private boolean consumed;

    public CheckZoneNeededEvent(String zone) {
        this.zone = zone;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void consume() {
        this.consumed = true;
    }
}
