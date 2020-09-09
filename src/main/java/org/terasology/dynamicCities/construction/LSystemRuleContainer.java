// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction;


import org.terasology.engine.math.LSystemRule;
import org.terasology.reflection.MappedContainer;

@MappedContainer
public final class LSystemRuleContainer {
    public String axiom;
    public float probability;

    public LSystemRuleContainer(String axiom, float probability) {
        this.axiom = axiom;
        this.probability = probability;
    }

    public LSystemRuleContainer(LSystemRule lSystemRule) {
        this.axiom = lSystemRule.getAxiom();
        this.probability = lSystemRule.getProbability();
    }

    public LSystemRuleContainer() {
    }
}
