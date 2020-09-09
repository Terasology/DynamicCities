// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.settlements;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseFacet2D;

/**
 *
 */
public class SettlementFacet extends BaseFacet2D {

    private SettlementComponent settlementComponent = null;

    public SettlementFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public SettlementComponent getSettlement() {
        return settlementComponent;
    }

    public void setSettlement(SettlementComponent settlement) {
        this.settlementComponent = settlement;
    }

}
