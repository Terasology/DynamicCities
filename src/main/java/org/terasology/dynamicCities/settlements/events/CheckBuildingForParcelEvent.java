// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements.events;

import org.terasology.dynamicCities.buildings.GenericBuildingComponent;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.engine.entitySystem.event.ConsumableEvent;

import java.util.Optional;

/**
 * Sent to check which building should be used for a given parcel
 */
public class CheckBuildingForParcelEvent implements ConsumableEvent {
    public DynParcel dynParcel;
    public Optional<GenericBuildingComponent> building;
    private boolean consumed = false;

    public CheckBuildingForParcelEvent(DynParcel dynParcel) {
        this.dynParcel = dynParcel;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void consume() {
        consumed = true;
    }
}
