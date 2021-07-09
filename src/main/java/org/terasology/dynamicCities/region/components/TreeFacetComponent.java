// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;

import com.google.common.collect.Maps;
import org.joml.RoundingMode;
import org.joml.Vector3f;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.dynamicCities.world.trees.RecursiveTreeGeneratorLSystem;
import org.terasology.dynamicCities.world.trees.TreeFacet;
import org.terasology.dynamicCities.world.trees.TreeGenerator;
import org.terasology.dynamicCities.world.trees.TreeGeneratorCactus;
import org.terasology.dynamicCities.world.trees.TreeGeneratorLSystem;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.component.Component;
import org.terasology.reflection.MappedContainer;

import java.util.Map;

@MappedContainer
public final class TreeFacetComponent implements Component<TreeFacetComponent> {


    public boolean privateToOwner = true;

    public final Map<Vector3i, TreeGeneratorContainer> relData = Maps.newHashMap();
    public BlockRegion relativeRegion = new BlockRegion(0,0,0);
    public BlockRegion worldRegion = new BlockRegion(0,0,0);
    public Vector3i center = new Vector3i();

    public TreeFacetComponent() { }

    public TreeFacetComponent(TreeFacet treeFacet) {

        relativeRegion = treeFacet.getRelativeRegion();
        worldRegion = treeFacet.getWorldRegion();
        center = new Vector3i(worldRegion.center(new Vector3f()), RoundingMode.FLOOR);
        for (Map.Entry<Vector3ic, TreeGenerator> entry : treeFacet.getRelativeEntries().entrySet()) {

            if (entry.getValue().getClass() == TreeGeneratorLSystem.class) {
                TreeGeneratorLSystem treeGen = (TreeGeneratorLSystem) entry.getValue();
                RecursiveTreeGeneratorLSystem recursiveTreeGeneratorLSystem = treeGen.getRecursiveGenerator();
                TreeGeneratorContainer container = new TreeGeneratorContainer(treeGen.getLeafType().toString(),
                        treeGen.getBarkType().toString(), treeGen.getInitialAxiom(), TreeGeneratorLSystem.class.toString(), recursiveTreeGeneratorLSystem.getMaxDepth(),
                        recursiveTreeGeneratorLSystem.getAngle(), recursiveTreeGeneratorLSystem.getRuleSet());
                relData.put(new Vector3i(entry.getKey()), container);
            } else if (entry.getValue().getClass() == TreeGeneratorCactus.class) {
                TreeGeneratorCactus treeGen = (TreeGeneratorCactus) entry.getValue();
                TreeGeneratorContainer container = new TreeGeneratorContainer(treeGen.getCactusType().toString());
                relData.put(new Vector3i(entry.getKey()), container);
            }


        }
    }

    public TreeGeneratorContainer get(int x, int y, int z) {
        return get(new Vector3i(x, y, z));
    }


    public TreeGeneratorContainer get(Vector3ic pos) {
        checkRelativeCoords(pos.x(), pos.y(), pos.z());

        return relData.get(pos.toString());
    }


    public void set(int x, int y, int z, TreeGeneratorContainer value) {
        set(new Vector3i(x, y, z), value);
    }


    public void set(Vector3ic pos, TreeGeneratorContainer value) {
        checkRelativeCoords(pos.x(), pos.y(), pos.z());

        relData.put(new Vector3i(pos), value); // TODO: consider using an immutable vector here
    }


    public TreeGeneratorContainer getWorld(Vector3ic pos) {
        return getWorld(pos.x(), pos.y(), pos.z());
    }


    public TreeGeneratorContainer getWorld(int x, int y, int z) {
        checkWorldCoords(x, y, z);

        Vector3i index = worldToRelative(x, y, z);
        return relData.get(index.toString());
    }


    public void setWorld(Vector3ic pos, TreeGeneratorContainer value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }


    public void setWorld(int x, int y, int z, TreeGeneratorContainer value) {
        checkWorldCoords(x, y, z);

        Vector3i index = worldToRelative(x, y, z);
        relData.put(index, value);
    }

    /**
     * @return an unmodifiable view on the relative entries
     */
    public Map<Vector3ic, TreeGeneratorContainer> getRelativeEntries() {
        Map<Vector3ic, TreeGeneratorContainer> vectorMap = Maps.newLinkedHashMap();
        for (Map.Entry<Vector3i, TreeGeneratorContainer> entry : relData.entrySet()) {
            vectorMap.put(entry.getKey(), entry.getValue());
        }
        return vectorMap;
    }

    /**
     * @return a <b>new</b> map with world-based position entries
     */
    public Map<Vector3ic, TreeGeneratorContainer> getWorldEntries() {

        Map<Vector3ic, TreeGeneratorContainer> result = Maps.newLinkedHashMap();

        for (Map.Entry<Vector3ic, TreeGeneratorContainer> entry : getRelativeEntries().entrySet()) {
            Vector3ic relPos = entry.getKey();
            Vector3ic worldPos = relativeToWorld(relPos.x(), relPos.y(), relPos.z());

            result.put(worldPos, entry.getValue());
        }

        return result;
    }

    /**
     * @throws IllegalArgumentException if not within bounds
     */
    protected void checkWorldCoords(int x, int y, int z) {
        if (!worldRegion.contains(x, y, z)) {
            String text = "Out of bounds: (%d, %d, %d) for region %s";
            String msg = String.format(text, x, y, z, worldRegion.toString());
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * @throws IllegalArgumentException if not within bounds
     */
    protected void checkRelativeCoords(int x, int y, int z) {
        if (!relativeRegion.contains(x, y, z)) {
            String text = "Out of bounds: (%d, %d, %d) for region %s";
            String msg = String.format(text, x, y, z, relativeRegion.toString());
            throw new IllegalArgumentException(msg);
        }
    }

    public final Vector3i worldToRelative(int x, int y, int z) {

        return new Vector3i(
                x - worldRegion.minX() + relativeRegion.minX(),
                y - worldRegion.minY() + relativeRegion.minY(),
                z - worldRegion.minZ() + relativeRegion.minZ());
    }

    public final Vector3i relativeToWorld(int x, int y, int z) {

        return new Vector3i(
                x - relativeRegion.minX() + worldRegion.minX(),
                y - relativeRegion.minY() + worldRegion.minY(),
                z - relativeRegion.minZ() + worldRegion.minZ());
    }


    public String toString() {
        Vector3i worldMin = worldRegion.getMin(new Vector3i());
        Vector3i relMin = relativeRegion.getMin(new Vector3i());
        Vector3i size = relativeRegion.getSize(new Vector3i());
        return String.format("TreeFacetComponent [worldMin=%s, relativeMin=%s, size=%s]", worldMin, relMin, size);
    }


}
