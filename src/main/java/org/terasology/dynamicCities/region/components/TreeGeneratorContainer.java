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
package org.terasology.dynamicCities.region.components;


import org.terasology.dynamicCities.world.trees.TreeGeneratorCactus;
import org.terasology.math.LSystemRule;
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
    public Map<String, LSystemRule> ruleSet;

    //For standard trees
    public TreeGeneratorContainer(String leafType, String barkType, String initialAxiom, String className, int maxDepth, float angle, Map<Character, LSystemRule> ruleSet) {
        this.leafType = leafType;
        this.barkType = barkType;
        this.initialAxiom = initialAxiom;
        this.className = className;
        this.maxDepth = maxDepth;
        this.angle = angle;
        this.ruleSet = new HashMap<>();
        for (Map.Entry<Character, LSystemRule> entry : ruleSet.entrySet()) {
            this.ruleSet.put(entry.getKey().toString(), entry.getValue());
        }
    }
    //For cacti
    public TreeGeneratorContainer(String cactusType) {
        barkType = cactusType;
        className = TreeGeneratorCactus.class.toString();
    }

    public TreeGeneratorContainer(){}

}
