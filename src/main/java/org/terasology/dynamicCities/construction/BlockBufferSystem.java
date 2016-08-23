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


import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.construction.components.BlockBufferComponent;
import org.terasology.dynamicCities.settlements.SettlementConstants;
import org.terasology.dynamicCities.settlements.events.SettlementGrowthEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.delay.DelayManager;
import org.terasology.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

@Share(BlockBufferSystem.class)
@RegisterSystem
public class BlockBufferSystem extends BaseComponentSystem {

    public static final String PLACE_BLOCKS_ACTION_ID = "BlockBufferSystem:placeBlocksAction";
    public static final int BLOCKS_PER_UPDATE = 10_000;

    private static final Logger logger = LoggerFactory.getLogger(BlockBufferSystem.class);

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    @In
    private DelayManager delayManager;

    private EntityRef blockBufferEntity;
    private BlockBufferComponent blockBufferComponent;

    private Map<Vector3i, Block> buffer = new LinkedHashMap<>(SettlementConstants.BLOCKBUFFER_SIZE);

    @Override
    public void postBegin() {
        Iterator<EntityRef> blockBufferIterator = entityManager.getEntitiesWith(BlockBufferComponent.class).iterator();
        blockBufferEntity = blockBufferIterator.hasNext() ? blockBufferIterator.next() : null;
        if (blockBufferEntity == null) {
            blockBufferComponent = new BlockBufferComponent();
            blockBufferEntity = entityManager.create(blockBufferComponent);
            blockBufferEntity.setAlwaysRelevant(true);
        }
        blockBufferComponent = blockBufferEntity.getComponent(BlockBufferComponent.class);
        if (blockBufferComponent.blockBuffer != null) {
            blockBufferComponent.blockBuffer.forEach(b -> buffer.put(b.pos, b.blockType));
        }

        delayManager.addPeriodicAction(blockBufferEntity, PLACE_BLOCKS_ACTION_ID, 1000, 1000);
    }

    public void saveBlock(Vector3i pos, Block block) {
        buffer.put(pos, block);
    }

    public void setBlock() {
        if (!buffer.isEmpty()) {
            Iterator<Map.Entry<Vector3i, Block>> iterator = buffer.entrySet().iterator();
            Map.Entry<Vector3i, Block> blockToPlace = iterator.next();
            if (worldProvider.isBlockRelevant(blockToPlace.getKey())) {
                worldProvider.setBlock(blockToPlace.getKey(), blockToPlace.getValue());
                iterator.remove();
            }
        }
    }

    public void setBlocks(int maxBlocksToSet) {
        Map<Vector3i, Block> blocksToPlace = new HashMap<>();
        int removed = 0;
        int oldBufferSize = getBlockBufferSize();

        Iterator<Map.Entry<Vector3i, Block>> iter = buffer.entrySet().iterator();
        while (iter.hasNext() && blocksToPlace.size() < maxBlocksToSet) {
            Map.Entry<Vector3i, Block> block = iter.next();
            if (worldProvider.isBlockRelevant(block.getKey())) {
                blocksToPlace.put(block.getKey(), block.getValue());
                iter.remove();
                removed++;
            }
        }

        if (!blocksToPlace.isEmpty()) {
            logger.info("Buffer before: {}, Placed: {}, Removed: {}, Buffer after: {}", oldBufferSize, blocksToPlace.size(), removed, getBlockBufferSize());
            //worldProvider.setBlocks(blocksToPlace);
            blocksToPlace.forEach((pos, block) -> worldProvider.setBlock(pos, block));
        }
    }

    public boolean isRegionProcessed(Region3i region3i) {
        if (!blockBufferComponent.blockBuffer.isEmpty()) {
            for (Vector3i pos : buffer.keySet()) {
                if (region3i.encompasses(pos)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getBlockBufferSize() {
        return buffer.size();
    }

    private void saveBufferToComponent() {
        BlockBufferComponent component = blockBufferEntity.getComponent(BlockBufferComponent.class);
        component.blockBuffer.clear();
        component.blockBuffer.addAll(buffer.entrySet().stream().map(entry -> new BufferedBlock(entry.getKey(), entry.getValue())).collect(Collectors.toList()));
        blockBufferEntity.saveComponent(component);
    }

    @ReceiveEvent
    public void onWorldPurge(SettlementGrowthEvent event, EntityRef entityRef) {
        blockBufferEntity.saveComponent(blockBufferComponent);
    }

    @ReceiveEvent(components = BlockBufferComponent.class)
    public void onWorldPurge(BeforeDeactivateComponent event, EntityRef entityRef) {
        blockBufferEntity.saveComponent(blockBufferComponent);
    }

    @ReceiveEvent
    public void onPeriodicPlaceBlocks(PeriodicActionTriggeredEvent event, EntityRef entity) {
        if (event.getActionId().equals(PLACE_BLOCKS_ACTION_ID)) {
            setBlocks(BLOCKS_PER_UPDATE);
        }
    }

    @Override
    public void preSave() {
        saveBufferToComponent();
    }
}
