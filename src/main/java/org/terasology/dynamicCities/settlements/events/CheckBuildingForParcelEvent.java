/*
 * Copyright 2017 MovingBlocks
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
