// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.world.trees;

import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.engine.math.LSystemRule;
import org.terasology.engine.utilities.collection.CharSequenceIterator;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.math.TeraMath;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Map;

/**
 * Encapsulates the recursive algorithm for the generator of trees
 */

public class RecursiveTreeGeneratorLSystem {

    private final int maxDepth;
    private final float angle;
    private final Map<Character, LSystemRule> ruleSet;

    public RecursiveTreeGeneratorLSystem(int maxDepth, float angle, Map<Character, LSystemRule> ruleSet) {
        this.angle = angle;
        this.maxDepth = maxDepth;
        this.ruleSet = ruleSet;
    }

    public void recurse(CoreChunk view, Random rand, int posX, int posY, int posZ, float angleOffset,
                        CharSequenceIterator axiomIterator, Vector3f position, Matrix4f rotation,
                        Block bark, Block leaf, int depth, AbstractTreeGenerator treeGenerator,
                        ResourceFacet resourceFacet) {
        Matrix4f tempRotation = new Matrix4f();
        while (axiomIterator.hasNext()) {
            char c = axiomIterator.nextChar();
            switch (c) {
                case 'G':
                case 'F':
                    // Tree trunk

                    treeGenerator.safelySetBlock(view, posX + (int) position.x + 1, posY + (int) position.y,
                            posZ + (int) position.z, bark, resourceFacet);
                    treeGenerator.safelySetBlock(view, posX + (int) position.x - 1, posY + (int) position.y,
                            posZ + (int) position.z, bark, resourceFacet);
                    treeGenerator.safelySetBlock(view, posX + (int) position.x, posY + (int) position.y,
                            posZ + (int) position.z + 1, bark, resourceFacet);
                    treeGenerator.safelySetBlock(view, posX + (int) position.x, posY + (int) position.y,
                            posZ + (int) position.z - 1, bark, resourceFacet);

                    // Generate leaves
                    if (depth > 1) {
                        int size = 1;

                        for (int x = -size; x <= size; x++) {
                            for (int y = -size; y <= size; y++) {
                                for (int z = -size; z <= size; z++) {
                                    if (Math.abs(x) == size && Math.abs(y) == size && Math.abs(z) == size) {
                                        continue;
                                    }

                                    treeGenerator.safelySetBlock(view, posX + (int) position.x + x + 1,
                                            posY + (int) position.y + y, posZ + z + (int) position.z, leaf,
                                            resourceFacet);
                                    treeGenerator.safelySetBlock(view, posX + (int) position.x + x - 1,
                                            posY + (int) position.y + y, posZ + z + (int) position.z, leaf,
                                            resourceFacet);
                                    treeGenerator.safelySetBlock(view, posX + (int) position.x + x,
                                            posY + (int) position.y + y, posZ + z + (int) position.z + 1, leaf,
                                            resourceFacet);
                                    treeGenerator.safelySetBlock(view, posX + (int) position.x + x,
                                            posY + (int) position.y + y, posZ + z + (int) position.z - 1, leaf,
                                            resourceFacet);
                                }
                            }
                        }
                    }

                    Vector3f dir = new Vector3f(1f, 0f, 0f);
                    rotation.transformVector(dir);

                    position.add(dir);
                    break;
                case '[':
                    recurse(view, rand, posX, posY, posZ, angleOffset, axiomIterator, new Vector3f(position),
                            new Matrix4f(rotation), bark, leaf, depth, treeGenerator, resourceFacet);
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

                    recurse(view, rand, posX, posY, posZ, angleOffset, new CharSequenceIterator(rule.getAxiom()),
                            position, rotation, bark, leaf, depth + 1, treeGenerator, resourceFacet);
            }
        }
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public float getAngle() {
        return angle;
    }

    public Map<Character, LSystemRule> getRuleSet() {
        return ruleSet;
    }

}
