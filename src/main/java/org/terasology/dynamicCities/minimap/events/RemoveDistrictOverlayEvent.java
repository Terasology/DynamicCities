// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.minimap.events;


import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;


@BroadcastEvent
public class RemoveDistrictOverlayEvent implements Event {

    public RemoveDistrictOverlayEvent() {
    }

}
