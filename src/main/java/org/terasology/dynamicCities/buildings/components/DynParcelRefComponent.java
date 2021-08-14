// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings.components;


import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.gestalt.entitysystem.component.Component;

public final class DynParcelRefComponent implements Component<DynParcelRefComponent> {

    public DynParcel dynParcel;

    public DynParcelRefComponent(DynParcel dynParcel) {
        this.dynParcel = dynParcel;
    }
    public DynParcelRefComponent() {

    }

    @Override
    public void copyFrom(DynParcelRefComponent other) {
        this.dynParcel = other.dynParcel;
    }
}
