/*
 * Copyright 2016 MovingBlocks
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
package org.terasology.dynamicCities.districts;


import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.Zone;
import org.terasology.rendering.nui.Color;

public enum DistrictType {
    RESIDENTIAL (Color.YELLOW),
    COMMERCIAL (Color.GREEN),
    CITYCENTER (Color.BLUE),
    OUTSKIRTS (Color.GREY),
    BLOCKED (Color.TRANSPARENT);

    private final Color color;

    DistrictType(Color color) {
        this.color = color;
    }
    public boolean isValidType(DynParcel parcel) {
        Zone zone = parcel.getZone();
        return isValidType(zone);
    }

    public boolean isValidType(Zone zone) {
        switch (this) {
            case RESIDENTIAL:   return (zone == Zone.RESIDENTIAL);
            case COMMERCIAL:    return (zone == Zone.COMMERCIAL);
            case CITYCENTER:    return (zone == Zone.CLERICAL || zone == Zone.GOVERNMENTAL);
            case OUTSKIRTS:     return (zone == Zone.RESIDENTIAL || zone == Zone.COMMERCIAL);
            case BLOCKED:       return false;
            default:        return true;
        }
    }

    public Color getColor() {
        return color;
    }

}
