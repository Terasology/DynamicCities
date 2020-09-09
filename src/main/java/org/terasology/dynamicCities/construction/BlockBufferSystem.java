// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.construction.components.BlockBufferComponent;
import org.terasology.dynamicCities.settlements.SettlementConstants;
import org.terasology.dynamicCities.settlements.events.SettlementGrowthEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.math.Region3i;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.math.geom.Vector3i;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Share(BlockBufferSystem.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class BlockBufferSystem extends BaseComponentSystem {

    public static final String PLACE_BLOCKS_ACTION_ID = "BlockBufferSystem:placeBlocksAction";
    public static final int BLOCKS_PER_UPDATE = 200;

    private static final Logger logger = LoggerFactory.getLogger(BlockBufferSystem.class);
    private final Map<Vector3i, Block> buffer = new LinkedHashMap<>(SettlementConstants.BLOCKBUFFER_SIZE);
    @In
    private EntityManager entityManager;
    @In
    private WorldProvider worldProvider;
    @In
    private DelayManager delayManager;
    private EntityRef blockBufferEntity;
    private BlockBufferComponent blockBufferComponent;

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

        // This results in a null delayManager if the system is not marked as Authority (non-host client timing issue?)
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
            logger.info("Buffer before: {}, Placed: {}, Removed: {}, Buffer after: {}", oldBufferSize,
                    blocksToPlace.size(), removed, getBlockBufferSize());
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
        component.blockBuffer.addAll(buffer.entrySet().stream().map(entry -> new BufferedBlock(entry.getKey(),
                entry.getValue())).collect(Collectors.toList()));
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
