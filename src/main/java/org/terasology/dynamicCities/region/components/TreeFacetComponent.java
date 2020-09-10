// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.region.components;

import com.google.common.collect.Maps;
import org.terasology.dynamicCities.utilities.Toolbox;
import org.terasology.dynamicCities.world.trees.RecursiveTreeGeneratorLSystem;
import org.terasology.dynamicCities.world.trees.TreeFacet;
import org.terasology.dynamicCities.world.trees.TreeGenerator;
import org.terasology.dynamicCities.world.trees.TreeGeneratorCactus;
import org.terasology.dynamicCities.world.trees.TreeGeneratorLSystem;
import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.math.Region3i;
import org.terasology.math.geom.BaseVector3i;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3i;
import org.terasology.nui.reflection.MappedContainer;

import java.util.Map;

@MappedContainer
public final class TreeFacetComponent implements Component {


    public final Map<String, TreeGeneratorContainer> relData = Maps.newLinkedHashMap();
    public boolean privateToOwner = true;
    public Region3i relativeRegion = Region3i.EMPTY;
    public Region3i worldRegion = Region3i.EMPTY;
    public Vector3i center = new Vector3i();

    public TreeFacetComponent() {
    }

    public TreeFacetComponent(TreeFacet treeFacet) {

        relativeRegion = treeFacet.getRelativeRegion();
        worldRegion = treeFacet.getWorldRegion();
        center = new Vector3i(worldRegion.center());
        for (Map.Entry<BaseVector3i, TreeGenerator> entry : treeFacet.getRelativeEntries().entrySet()) {

            if (entry.getValue().getClass() == TreeGeneratorLSystem.class) {
                TreeGeneratorLSystem treeGen = (TreeGeneratorLSystem) entry.getValue();
                RecursiveTreeGeneratorLSystem recursiveTreeGeneratorLSystem = treeGen.getRecursiveGenerator();
                TreeGeneratorContainer container = new TreeGeneratorContainer(treeGen.getLeafType().toString(),
                        treeGen.getBarkType().toString(), treeGen.getInitialAxiom(),
                        TreeGeneratorLSystem.class.toString(), recursiveTreeGeneratorLSystem.getMaxDepth(),
                        recursiveTreeGeneratorLSystem.getAngle(), recursiveTreeGeneratorLSystem.getRuleSet());
                relData.put(entry.getKey().toString(), container);
            } else if (entry.getValue().getClass() == TreeGeneratorCactus.class) {
                TreeGeneratorCactus treeGen = (TreeGeneratorCactus) entry.getValue();
                TreeGeneratorContainer container = new TreeGeneratorContainer(treeGen.getCactusType().toString());
                relData.put(entry.getKey().toString(), container);
            }


        }
    }


    private Rect2i copyRect2i(Rect2i value) {
        return Rect2i.createFromMinAndMax(value.minX(), value.minY(), value.maxX(), value.maxY());
    }

    public TreeGeneratorContainer get(int x, int y, int z) {
        return get(new Vector3i(x, y, z));
    }


    public TreeGeneratorContainer get(BaseVector3i pos) {
        checkRelativeCoords(pos.x(), pos.y(), pos.z());

        return relData.get(pos.toString());
    }


    public void set(int x, int y, int z, TreeGeneratorContainer value) {
        set(new Vector3i(x, y, z), value);
    }


    public void set(BaseVector3i pos, TreeGeneratorContainer value) {
        checkRelativeCoords(pos.x(), pos.y(), pos.z());

        relData.put(pos.toString(), value); // TODO: consider using an immutable vector here
    }


    public TreeGeneratorContainer getWorld(BaseVector3i pos) {
        return getWorld(pos.x(), pos.y(), pos.z());
    }


    public TreeGeneratorContainer getWorld(int x, int y, int z) {
        checkWorldCoords(x, y, z);

        Vector3i index = worldToRelative(x, y, z);
        return relData.get(index.toString());
    }


    public void setWorld(BaseVector3i pos, TreeGeneratorContainer value) {
        setWorld(pos.x(), pos.y(), pos.z(), value);
    }


    public void setWorld(int x, int y, int z, TreeGeneratorContainer value) {
        checkWorldCoords(x, y, z);

        Vector3i index = worldToRelative(x, y, z);
        relData.put(index.toString(), value);
    }

    /**
     * @return an unmodifiable view on the relative entries
     */
    public Map<BaseVector3i, TreeGeneratorContainer> getRelativeEntries() {
        Map<BaseVector3i, TreeGeneratorContainer> vectorMap = Maps.newLinkedHashMap();
        for (Map.Entry<String, TreeGeneratorContainer> entry : relData.entrySet()) {
            vectorMap.put(Toolbox.stringToVector3i(entry.getKey()), entry.getValue());
        }
        return vectorMap;
    }

    /**
     * @return a <b>new</b> map with world-based position entries
     */
    public Map<BaseVector3i, TreeGeneratorContainer> getWorldEntries() {

        Map<BaseVector3i, TreeGeneratorContainer> result = Maps.newLinkedHashMap();

        for (Map.Entry<BaseVector3i, TreeGeneratorContainer> entry : getRelativeEntries().entrySet()) {
            BaseVector3i relPos = entry.getKey();
            BaseVector3i worldPos = relativeToWorld(relPos.x(), relPos.y(), relPos.z());

            result.put(worldPos, entry.getValue());
        }

        return result;
    }

    /**
     * @throws IllegalArgumentException if not within bounds
     */
    protected void checkWorldCoords(int x, int y, int z) {
        if (!worldRegion.encompasses(x, y, z)) {
            String text = "Out of bounds: (%d, %d, %d) for region %s";
            String msg = String.format(text, x, y, z, worldRegion.toString());
            throw new IllegalArgumentException(msg);
        }
    }

    /**
     * @throws IllegalArgumentException if not within bounds
     */
    protected void checkRelativeCoords(int x, int y, int z) {
        if (!relativeRegion.encompasses(x, y, z)) {
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
        Vector3i worldMin = worldRegion.min();
        Vector3i relMin = relativeRegion.min();
        Vector3i size = relativeRegion.size();
        return String.format("TreeFacetComponent [worldMin=%s, relativeMin=%s, size=%s]", worldMin, relMin, size);
    }


}
