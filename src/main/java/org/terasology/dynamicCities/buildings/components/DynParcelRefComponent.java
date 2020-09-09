// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings.components;


import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.engine.entitySystem.Component;

public final class DynParcelRefComponent implements Component {

    public DynParcel dynParcel;

    public DynParcelRefComponent(DynParcel dynParcel) {
        this.dynParcel = dynParcel;
    }

    public DynParcelRefComponent() {

    }
}
