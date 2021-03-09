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

    public LSystemRuleContainer() {}
}
