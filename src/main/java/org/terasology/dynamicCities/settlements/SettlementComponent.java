// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.settlements;

import org.joml.Vector2i;
import org.terasology.dynamicCities.sites.SiteComponent;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

/**
 * Provides information on a settlement.
  */
@MappedContainer
public class SettlementComponent implements Component<SettlementComponent> {

    public Vector2i coords = new Vector2i();
    public int population;
    public String name;

    public SettlementComponent() {
    }

    public SettlementComponent(SiteComponent siteComponent, int population) {
        this.coords = new Vector2i(siteComponent.getPos());
        this.population = population;
    }

    @Override
    public String toString() {
        return name + " (" + coords + ")";
    }

}
