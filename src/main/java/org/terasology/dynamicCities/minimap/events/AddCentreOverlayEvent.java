// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.minimap.events;

import org.terasology.engine.entitySystem.event.Event;
import org.terasology.engine.network.BroadcastEvent;

/**
 * Event to be used to add the centre overlay to the minimap when the player first enters a city.
 */
@BroadcastEvent
public class AddCentreOverlayEvent implements Event {
    public AddCentreOverlayEvent() {

    }
}
