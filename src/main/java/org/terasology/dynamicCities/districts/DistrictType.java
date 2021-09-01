// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.districts;


import com.google.common.collect.Lists;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.nui.Color;
import org.terasology.reflection.MappedContainer;

import java.util.List;

//TODO: give mixing factors for zones
//TODO: display color as hexadecimal
@MappedContainer
public class DistrictType implements Component<DistrictType> {

    public String name;
    public int color;
    public List<String> zones = Lists.newArrayList();

    public DistrictType ( ) { }

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

    @Override
    public void copyFrom(DistrictType other) {
        this.name = other.name;
        this.color = other.color;
        this.zones = Lists.newArrayList(other.zones);
    }
}
