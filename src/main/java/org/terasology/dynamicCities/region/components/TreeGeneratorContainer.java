// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;


import org.terasology.dynamicCities.construction.LSystemRuleContainer;
import org.terasology.dynamicCities.world.trees.TreeGeneratorCactus;
import org.terasology.engine.math.LSystemRule;
import org.terasology.reflection.MappedContainer;

import java.util.HashMap;
import java.util.Map;

@MappedContainer
public final class TreeGeneratorContainer {

    public String leafType;
    public String barkType;
    public String initialAxiom;
    //If it's a cactus or tree gen
    public String className;

    //RecursiveTreeGeneratorLSystem settings
    public int maxDepth;
    public float angle;
    public Map<String, LSystemRuleContainer> ruleSet;

    //For standard trees
    public TreeGeneratorContainer(String leafType, String barkType, String initialAxiom, String className,
                                  int maxDepth, float angle, Map<Character, LSystemRule> ruleSet) {
        this.leafType = leafType;
        this.barkType = barkType;
        this.initialAxiom = initialAxiom;
        this.className = className;
        this.maxDepth = maxDepth;
        this.angle = angle;
        this.ruleSet = new HashMap<>();
        for (Map.Entry<Character, LSystemRule> entry : ruleSet.entrySet()) {
            this.ruleSet.put(entry.getKey().toString(), new LSystemRuleContainer(entry.getValue()));
        }
    }

    //For cacti
    public TreeGeneratorContainer(String cactusType) {
        barkType = cactusType;
        className = TreeGeneratorCactus.class.toString();
    }

    public TreeGeneratorContainer() {
    }

}
