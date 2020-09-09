// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.construction;

import org.terasology.dynamicCities.region.components.TreeGeneratorContainer;
import org.terasology.engine.math.LSystemRule;
import org.terasology.engine.utilities.collection.CharSequenceIterator;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.Block;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;

import java.util.HashMap;
import java.util.Map;

/**
 * Encapsulates the recursive algorithm for the generator of trees
 */

public class RecursiveTreeGeneratorLSystemRemover {

    private final BlockBufferSystem blockBufferSystem;
    public int maxDepth;
    public float angle;
    public Map<Character, LSystemRule> ruleSet;

    public RecursiveTreeGeneratorLSystemRemover(int maxDepth, float angle, Map<Character, LSystemRule> ruleSet,
                                                BlockBufferSystem blockBufferSystem) {
        this.angle = angle;
        this.maxDepth = maxDepth;
        this.ruleSet = ruleSet;
        this.blockBufferSystem = blockBufferSystem;
    }

    public void recurse(Random rand, int posX, int posY, int posZ, float angleOffset,
                        CharSequenceIterator axiomIterator, Vector3f position, Matrix4f rotation,
                        Block air, int depth) {
        Matrix4f tempRotation = new Matrix4f();
        while (axiomIterator.hasNext()) {
            char c = axiomIterator.nextChar();
            switch (c) {
                case 'G':
                case 'F':
                    // Tree trunk
                    Vector3i pos1 = new Vector3i(posX + (int) position.x + 1, posY + (int) position.y,
                            posZ + (int) position.z);
                    Vector3i pos2 = new Vector3i(posX + (int) position.x - 1, posY + (int) position.y,
                            posZ + (int) position.z);
                    Vector3i pos3 = new Vector3i(posX + (int) position.x, posY + (int) position.y,
                            posZ + (int) position.z + 1);
                    Vector3i pos4 = new Vector3i(posX + (int) position.x, posY + (int) position.y,
                            posZ + (int) position.z - 1);
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
                                    pos1 = new Vector3i(posX + (int) position.x + x + 1, posY + (int) position.y + y,
                                            posZ + z + (int) position.z);
                                    pos2 = new Vector3i(posX + (int) position.x + x - 1, posY + (int) position.y + y,
                                            posZ + z + (int) position.z);
                                    pos3 = new Vector3i(posX + (int) position.x + x, posY + (int) position.y + y,
                                            posZ + z + (int) position.z + 1);
                                    pos4 = new Vector3i(posX + (int) position.x + x, posY + (int) position.y + y,
                                            posZ + z + (int) position.z - 1);
                                    blockBufferSystem.saveBlock(pos1, air);
                                    blockBufferSystem.saveBlock(pos2, air);
                                    blockBufferSystem.saveBlock(pos3, air);
                                    blockBufferSystem.saveBlock(pos4, air);
                                }
                            }
                        }
                    }

                    Vector3f dir = new Vector3f(1f, 0f, 0f);
                    rotation.transformVector(dir);

                    position.add(dir);
                    break;
                case '[':
                    recurse(rand, posX, posY, posZ, angleOffset, axiomIterator, new Vector3f(position),
                            new Matrix4f(rotation), air, depth);
                    break;
                case ']':
                    return;
                case '+':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(0f, 0f, 1f), angle + angleOffset),
                            Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '-':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(0f, 0f, -1f), angle + angleOffset),
                            Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '&':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(0f, 1f, 0f), angle + angleOffset),
                            Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '^':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(0f, -1f, 0f), angle + angleOffset),
                            Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '*':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(1f, 0f, 0f), angle), Vector3f.ZERO, 1.0f);
                    rotation.mul(tempRotation);
                    break;
                case '/':
                    tempRotation = new Matrix4f(new Quat4f(new Vector3f(-1f, 0f, 0f), angle), Vector3f.ZERO, 1.0f);
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
            ruleSet.put(entry.getKey().charAt(0), new LSystemRule(entry.getValue().axiom,
                    entry.getValue().probability));
        }
    }

}
