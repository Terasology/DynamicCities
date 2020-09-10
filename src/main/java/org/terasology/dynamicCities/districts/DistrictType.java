// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.districts;


import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.engine.entitySystem.Component;
import org.terasology.nui.Color;
import org.terasology.nui.reflection.MappedContainer;

import java.util.List;

//TODO: give mixing factors for zones
//TODO: display color as hexadecimal
@MappedContainer
public class DistrictType implements Component {

    public String name;
    public int color;
    public List<String> zones;

    public DistrictType() {
    }

    public boolean isValidType(DynParcel parcel) {
        String zone = parcel.getZone();
        return isValidType(zone);
    }

    public boolean isValidType(String zone) {
        return zones.contains(zone);
    }

    public Color getColor() {
        return new Color(color);
    }

}
