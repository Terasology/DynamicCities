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

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.terasology.dynamicCities.region.components.TreeGeneratorContainer;
import org.terasology.math.LSystemRule;
import org.terasology.math.TeraMath;
import org.terasology.utilities.collection.CharSequenceIterator;
import org.terasology.utilities.random.Random;
import org.terasology.world.block.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the recursive algorithm for the generator of trees
 */

public class RecursiveTreeGeneratorLSystemRemover {

    public int maxDepth;
    public float angle;
    public Map<Character, LSystemRule> ruleSet;
    private BlockBufferSystem blockBufferSystem;

    public RecursiveTreeGeneratorLSystemRemover(int maxDepth, float angle, Map<Character, LSystemRule> ruleSet, BlockBufferSystem blockBufferSystem) {
        this.angle = angle;
        this.maxDepth = maxDepth;
        this.ruleSet = ruleSet;
        this.blockBufferSystem = blockBufferSystem;
    }

    public void recurse(Random rand, int posX, int posY, int posZ, float angleOffset,
                        CharSequenceIterator axiomIterator, Vector3f position, Quaternionf rotation,
                        Block air, int depth) {
        Quaternionf tempRotation = new Quaternionf();
        while (axiomIterator.hasNext()) {
            char c = axiomIterator.nextChar();
            switch (c) {
                case 'G':
                case 'F':
                    // Tree trunk
                    Vector3i pos1 = new Vector3i(posX + (int) position.x + 1, posY + (int) position.y, posZ + (int) position.z);
                    Vector3i pos2 = new Vector3i(posX + (int) position.x - 1, posY + (int) position.y, posZ + (int) position.z);
                    Vector3i pos3 = new Vector3i(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z + 1);
                    Vector3i pos4 = new Vector3i(posX + (int) position.x, posY + (int) position.y, posZ + (int) position.z - 1);
                    blockBufferSystem.saveBlock(pos1, air);
                    blockBufferSystem.saveBlock(pos2, air);
                    blockBufferSystem.saveBlock(pos3, air);
                    blockBufferSystem.saveBlock(pos4, air);

                    // Generate leaves
                    if (depth > 1) {
                        int size = 1;

                        for (int x = -size; x <= size; x++) {
                            for (int y = -size; y <= size; y++) {
                                for (int z = -size; z <= size; z++) {
                                    if (Math.abs(x) == size && Math.abs(y) == size && Math.abs(z) == size) {
                                        continue;
                                    }
                                    pos1 = new Vector3i(posX + (int) position.x + x + 1, posY + (int) position.y + y, posZ + z + (int) position.z);
                                    pos2 = new Vector3i(posX + (int) position.x + x - 1, posY + (int) position.y + y, posZ + z + (int) position.z);
                                    pos3 = new Vector3i(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z + 1);
                                    pos4 = new Vector3i(posX + (int) position.x + x, posY + (int) position.y + y, posZ + z + (int) position.z - 1);
                                    blockBufferSystem.saveBlock(pos1, air);
                                    blockBufferSystem.saveBlock(pos2, air);
                                    blockBufferSystem.saveBlock(pos3, air);
                                    blockBufferSystem.saveBlock(pos4, air);
                                }
                            }
                        }
                    }

                    Vector3f dir = new Vector3f(1f, 0f, 0f);
                    rotation.transform(dir);

                    position.add(dir);
                    break;
                case '[':
                    recurse(rand, posX, posY, posZ, angleOffset, axiomIterator, new Vector3f(position), new Quaternionf(rotation), air, depth);
                    break;
                case ']':
                    return;
                case '+':
                    tempRotation.setAngleAxis(angle + angleOffset, 0, 0, 1);
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation.setAngleAxis(angle + angleOffset, 0, 0, -1);
                    rotation.mul(tempRotation);
                    break;
                case '&':
                    tempRotation.setAngleAxis(angle + angleOffset, 0, 1, 0);
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation.setAngleAxis(angle + angleOffset, 0, -1, 0);
                    rotation.mul(tempRotation);
                    break;
                case '*':
                    tempRotation.setAngleAxis(angle + angleOffset, 1, 0, 0);
                    rotation.mul(tempRotation);
                    break;
                case '/':
                    tempRotation.setAngleAxis(angle + angleOffset, -1, 0, 0);
                    rotation.mul(tempRotation);
                    break;
                default:
                    // If we have already reached the maximum depth, don't ever bother to lookup in the map
                    if (depth == maxDepth - 1) {
                        break;
                    }
                    LSystemRule rule = ruleSet.get(c);
                    if (rule == null) {
                        break;
                    }

                    float weightedFailureProbability = TeraMath.pow(1f - rule.getProbability(), maxDepth - depth);
                    if (rand.nextFloat() < weightedFailureProbability) {
                        break;
                    }

                    recurse(rand, posX, posY, posZ, angleOffset, new CharSequenceIterator(rule.getAxiom()),
                        position, rotation, air, depth + 1);
            }
        }
    }

    public void applySettings(TreeGeneratorContainer treeGeneratorContainer) {
        maxDepth = treeGeneratorContainer.maxDepth;
        angle = treeGeneratorContainer.angle;
        ruleSet = new HashMap<>();
        for (Map.Entry<String, LSystemRuleContainer> entry : treeGeneratorContainer.ruleSet.entrySet()) {
            ruleSet.put(entry.getKey().charAt(0), new LSystemRule(entry.getValue().axiom, entry.getValue().probability));
        }
    }

}
