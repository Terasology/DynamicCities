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
import org.terasology.entitySystem.Component;
import org.terasology.rendering.nui.Color;

import java.util.Set;

//TODO: Convert it into a prefab system aaaaaaaand give mixing factors for zones
public class DistrictType implements Component {

    public String name;
    public Color color;
    public Set<String> zones;

    public DistrictType ( ) { }

    public boolean isValidType(DynParcel parcel) {
        String zone = parcel.getZone();
        return isValidType(zone);
    }

    public boolean isValidType(String zone) {
        return zones.contains(zone);
    }

}
