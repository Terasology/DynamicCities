// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements.events;

import org.terasology.engine.entitySystem.event.ConsumableEvent;

/**
 * Issued when the settlement manager wants to know if a site is suitable for creating a settlement.
 */
public class CheckSiteSuitabilityEvent implements ConsumableEvent {
    private SettlementFilterResult result = SettlementFilterResult.UNKNOWN;
    private boolean consumed;

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void consume() {
        this.consumed = true;
    }

    public SettlementFilterResult getResult() {
        return result;
    }

    public void setResult(SettlementFilterResult result) {
        this.result = result;
    }
}
