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

import org.joml.Vector3ic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.region.RegionEntityManager;
import org.terasology.dynamicCities.region.components.RoughnessFacetComponent;
import org.terasology.dynamicCities.region.components.TreeFacetComponent;
import org.terasology.dynamicCities.region.components.TreeGeneratorContainer;
import org.terasology.dynamicCities.settlements.SettlementConstants;
import org.terasology.dynamicCities.world.trees.TreeGeneratorCactus;
import org.terasology.dynamicCities.world.trees.TreeGeneratorLSystem;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Matrix4f;
import org.terasology.math.geom.Quat4f;
import org.terasology.math.geom.Rect2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.CoreRegistry;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.utilities.collection.CharSequenceIterator;
import org.terasology.utilities.random.FastRandom;
import org.terasology.utilities.random.Random;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;

import java.util.List;
import java.util.Map;


@Share(value = TreeRemovalSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class TreeRemovalSystem extends BaseComponentSystem {

    @In
    private WorldProvider worldProvider;

    @In
    private BlockManager blockManager;

    @In
    private RegionEntityManager regionEntityManager;

    @In
    private BlockBufferSystem blockBufferSystem;


    private Block air;
    private Logger logger = LoggerFactory.getLogger(TreeRemovalSystem.class);
    private RecursiveTreeGeneratorLSystemRemover recursiveTreeRemover;

    public void initialise() {
        blockManager = CoreRegistry.get(BlockManager.class);
        air = blockManager.getBlock("engine:air");
        recursiveTreeRemover = new RecursiveTreeGeneratorLSystemRemover(99, 99, null, blockBufferSystem);
    }

    //TODO Get it work with different seeds
    public void removeTreesInRegion(EntityRef region) {
        TreeFacetComponent trees = region.getComponent(TreeFacetComponent.class);
        if (trees.getWorldEntries().isEmpty()) {
            return;
        }
        for (Map.Entry<Vector3ic, TreeGeneratorContainer> tree : trees.getWorldEntries().entrySet()) {
                removeTree(tree.getKey(), tree.getValue());
                trees.relData.remove(trees.worldToRelative(tree.getKey().x(), tree.getKey().y(), tree.getKey().z()).toString());

        }
    }
    public boolean removeTreesInRegion(EntityRef region, Rect2i area) {
        TreeFacetComponent trees = region.getComponent(TreeFacetComponent.class);
        LocationComponent loc = region.getComponent(LocationComponent.class);
        Rect2i relevantArea = area.expand(SettlementConstants.MAX_TREE_RADIUS, SettlementConstants.MAX_TREE_RADIUS);
        Region3i treeRegion = Region3i.createFromMinAndSize(new Vector3i(relevantArea.minX(), loc.getLocalPosition().y(), relevantArea.minY()),
                new Vector3i(relevantArea.sizeX(), 32, relevantArea.sizeY()));
        if (!worldProvider.isRegionRelevant(treeRegion)) {
            return false;
        }

        for (Map.Entry<Vector3ic, TreeGeneratorContainer> tree : trees.getWorldEntries().entrySet()) {
            if (area.contains(tree.getKey().x(), tree.getKey().z())) {
                removeTree(tree.getKey(), tree.getValue());
                trees.relData.remove(trees.worldToRelative(tree.getKey().x(), tree.getKey().y(), tree.getKey().z()).toString());
            }
        }
        return true;
    }

    public boolean removeTreesInRegions(Rect2i area) {
        List<EntityRef> regions = regionEntityManager.getRegionsInArea(area);
        for (EntityRef region : regions) {
            if (!region.hasComponent(RoughnessFacetComponent.class)) {
                return false;
            }
            if (region.getComponent(RoughnessFacetComponent.class).worldRegion.overlaps(area)) {
                if (!removeTreesInRegion(region, area)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void removeTree(Vector3ic pos, TreeGeneratorContainer tree) {
        int seed = pos.hashCode();
        Random random = new FastRandom(0);
        Vector3f position = new Vector3f(0f, 0f, 0f);

        Matrix4f rotation = new Matrix4f(new Quat4f(new Vector3f(0f, 0f, 1f), (float) Math.PI / 2f), Vector3f.ZERO, 1.0f);

        float angleOffset = random.nextFloat(-TreeGeneratorLSystem.MAX_ANGLE_OFFSET, TreeGeneratorLSystem.MAX_ANGLE_OFFSET);

        if (tree.className.equals(TreeGeneratorLSystem.class.toString())) {
            recursiveTreeRemover.applySettings(tree);
            recursiveTreeRemover.recurse(random, pos.x(), pos.y(), pos.z(), angleOffset, new CharSequenceIterator(tree.initialAxiom), position, rotation, air, 0);
        } else if (tree.className.equals(TreeGeneratorCactus.class.toString())) {
            for (int y = pos.y(); y < pos.y() + 3; y++) {
                Vector3i blockPosition = new Vector3i(pos.x(), y, pos.z());
                worldProvider.setBlock(blockPosition, air);
            }
        } else {
            logger.error("Failed to remove tree due to an unknown TreeGenerator.class " + tree.className);
        }
    }



}
