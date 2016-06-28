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

public enum DistrictTypes {
    RESIDENTIAL, COMMERCIAL, CITYCENTER, OUTSKIRTS;


    public boolean isValidType(DynParcel parcel) {
        Zone zone = parcel.getZoneDyn();
        return isValidType(zone);
    }

    public boolean isValidType(Zone zone) {
        switch (this) {
            case RESIDENTIAL:   if (zone != Zone.RESIDENTIAL) {
                return false;
            } else {
                return true;
            }
            case COMMERCIAL:    if (zone != Zone.COMMERCIAL) {
                return false;
            } else {
                return true;
            }
            case CITYCENTER:    if (zone == Zone.RESIDENTIAL || zone == Zone.COMMERCIAL) {
                return false;
            } else {
                return true;
            }
            case OUTSKIRTS:     if (zone == Zone.CLERICAL || zone == Zone.GOVERNMENTAL) {
                return false;
            } else {
                return true;
            }
            default:        return true;
        }
    }
}
