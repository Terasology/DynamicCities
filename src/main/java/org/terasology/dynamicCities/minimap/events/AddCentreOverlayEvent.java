/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.dynamicCities.minimap.events;

import org.terasology.entitySystem.event.Event;
import org.terasology.network.BroadcastEvent;

/**
 * Event to be used to add the centre overlay to the minimap when the player first enters a city.
 */
@BroadcastEvent
public class AddCentreOverlayEvent implements Event {
    public AddCentreOverlayEvent() {

    }
}
