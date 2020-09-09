// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.world.trees;

import com.google.common.collect.ImmutableMap;
import org.terasology.engine.math.LSystemRule;
import org.terasology.engine.world.block.BlockUri;

/**
 * Creates trees based on the original
 */
public final class Trees {

    private Trees() {
        // no instances!
    }

    public static TreeGenerator oakTree() {
        return new TreeGeneratorLSystem(
                "FFFFFFA", ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFA]", 1.0f))
                .put('B', new LSystemRule("[&FFFA]////[&FFFA]////[&FFFA]", 0.8f)).build(),
                4, (float) Math.toRadians(30))
                .setLeafType(new BlockUri("CoreAssets:GreenLeaf"))
                .setBarkType(new BlockUri("CoreAssets:OakTrunk"));
    }

    public static TreeGenerator oakVariationTree() {
        return new TreeGeneratorLSystem(
                "FFFFFFA", ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFBFA]////[&BFFFA]////[&FBFFAFFA]", 1.0f))
                .put('B', new LSystemRule("[&FFFAFFFF]////[&FFFAFFF]////[&FFFAFFAA]", 0.8f)).build(),
                4, (float) Math.toRadians(35))
                .setLeafType(new BlockUri("CoreAssets:GreenLeaf"))
                .setBarkType(new BlockUri("CoreAssets:OakTrunk"));
    }

    public static TreeGenerator pineTree() {
        return new TreeGeneratorLSystem(
                "FFFFAFFFFFFFAFFFFA", ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFFFFA]////[&FFFFFA]////[&FFFFFA]", 1.0f)).build(),
                4, (float) Math.toRadians(35))
                .setLeafType(new BlockUri("CoreAssets:DarkLeaf"))
                .setBarkType(new BlockUri("CoreAssets:PineTrunk"));
    }

    public static TreeGenerator birchTree() {
        return new TreeGeneratorLSystem(
                "FFFFAFFFFBFFFFAFFFFBFFFFAFFFFBFF", ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFFAFFF]////[&FFAFFF]////[&FFFAFFF]", 1.0f))
                .put('B', new LSystemRule("[&FAF]////[&FAF]////[&FAF]", 0.8f)).build(), 4, (float) Math.toRadians(35))
                .setLeafType(new BlockUri("CoreAssets:DarkLeaf"))
                .setBarkType(new BlockUri("CoreAssets:BirchTrunk"));
    }

    public static TreeGenerator redTree() {
        return new TreeGeneratorLSystem("FFFFFAFAFAF", ImmutableMap.<Character, LSystemRule>builder()
                .put('A', new LSystemRule("[&FFAFF]////[&FFAFF]////[&FFAFF]", 1.0f)).build(),
                4, (float) Math.toRadians(40))
                .setLeafType(new BlockUri("CoreAssets:RedLeaf"))
                .setBarkType(new BlockUri("CoreAssets:OakTrunk"));
    }

    public static TreeGenerator cactus() {
        return new TreeGeneratorCactus()
                .setTrunkType(new BlockUri("CoreAssets:Cactus"));
    }
}
