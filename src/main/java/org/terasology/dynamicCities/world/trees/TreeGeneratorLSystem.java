// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.world.trees;

import org.terasology.dynamicCities.facets.ResourceFacet;
import org.terasology.engine.math.LSystemRule;
import org.terasology.engine.utilities.collection.CharSequenceIterator;
import org.terasology.engine.utilities.random.Random;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.BlockUri;
import org.terasology.engine.world.chunks.CoreChunk;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Vector3f;

import java.util.Map;

/**
 * Allows the generator of complex trees based on L-Systems.
 */
public class TreeGeneratorLSystem extends AbstractTreeGenerator {

    public static final float MAX_ANGLE_OFFSET = (float) Math.toRadians(5);
    /* RULES */
    private final String initialAxiom;
    private final RecursiveTreeGeneratorLSystem recursiveGenerator;
    /* SETTINGS */
    private BlockUri leafType;
    private BlockUri barkType;

    /**
     * Init. a new L-System based tree generator.
     *
     * @param initialAxiom The initial axiom to use
     * @param ruleSet The rule set to use
     * @param maxDepth The maximum recursion depth
     * @param angle The angle
     */
    public TreeGeneratorLSystem(String initialAxiom, Map<Character, LSystemRule> ruleSet, int maxDepth, float angle) {
        this.initialAxiom = initialAxiom;

        recursiveGenerator = new RecursiveTreeGeneratorLSystem(maxDepth, angle, ruleSet);
    }

    @Override
    public void generate(BlockManager blockManager, CoreChunk view, Random rand, int posX, int posY, int posZ,
                         ResourceFacet resourceFacet) {
        Vector3f position = new Vector3f(0f, 0f, 0f);

        Matrix4f rotation = new Matrix4f(new Quat4f(new Vector3f(0f, 0f, 1f), (float) Math.PI / 2f), Vector3f.ZERO,
                1.0f);

        float angleOffset = rand.nextFloat(-MAX_ANGLE_OFFSET, MAX_ANGLE_OFFSET);

        Block bark = blockManager.getBlock(barkType);
        Block leaf = blockManager.getBlock(leafType);
        recursiveGenerator.recurse(view, rand, posX, posY, posZ, angleOffset, new CharSequenceIterator(initialAxiom),
                position, rotation, bark, leaf, 0, this, resourceFacet);
    }

    public RecursiveTreeGeneratorLSystem getRecursiveGenerator() {
        return recursiveGenerator;
    }

    public BlockUri getLeafType() {
        return leafType;
    }

    public TreeGeneratorLSystem setLeafType(BlockUri b) {
        leafType = b;
        return this;
    }

    public BlockUri getBarkType() {
        return barkType;
    }

    public TreeGeneratorLSystem setBarkType(BlockUri b) {
        barkType = b;
        return this;
    }

    public String getInitialAxiom() {
        return initialAxiom;
    }
}
