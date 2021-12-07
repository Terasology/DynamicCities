// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction;

import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.dynamicCities.construction.components.BlockBufferComponent;
import org.terasology.dynamicCities.settlements.SettlementConstants;
import org.terasology.dynamicCities.settlements.events.SettlementGrowthEvent;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.delay.DelayManager;
import org.terasology.engine.logic.delay.PeriodicActionTriggeredEvent;
import org.terasology.engine.registry.In;
import org.terasology.engine.registry.Share;
import org.terasology.engine.world.WorldProvider;
import org.terasology.engine.world.block.Block;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.gestalt.entitysystem.event.ReceiveEvent;

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
        // If the entity does not exist we create it with a `BlockBufferComponent`, but we don't create the component if the entity already
        // exists but does not have that component - why is that? And should we change that?
        if (blockBufferComponent.blockBuffer != null) {
            blockBufferComponent.blockBuffer.forEach(b -> buffer.put(b.pos, b.blockType));
        }

        // This results in a null delayManager if the system is not marked as Authority as the DelayManager is an authority-only system.
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

    /**
     * Dequeue blocks from the internal buffer and set them in the world.
     * <p>
     * The blocks to set are taken at random from the buffer. Only blocks at {@link WorldProvider#isBlockRelevant(Vector3fc) relevant
     * positions} are considered for placement.
     *
     * @param maxBlocksToSet the upper limit of blocks that should be set at once.
     */
    public void setBlocks(int maxBlocksToSet) {
        int oldBufferSize = getBlockBufferSize();

        //TODO: Can we do better on selecting which blocks to set next?
        //      Similar to chunk generation, we may score this by some notion of relevance, e.g., the distance to active players, or we
        //      could try to cluster blocks together such that buildings and other structures appear in a single step.
        Map<Vector3i, Block> blocksToPlace = buffer.entrySet().stream()
                .filter(block -> worldProvider.isBlockRelevant(block.getKey()))
                .limit(maxBlocksToSet)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        blocksToPlace.forEach((pos, block) -> {
            worldProvider.setBlock(pos, block);
            buffer.remove(pos);
        });

        if (logger.isDebugEnabled() && !blocksToPlace.isEmpty()) {
            logger.debug("Buffer before: {}, Placed: {}, Buffer after: {}", oldBufferSize, blocksToPlace.size(), getBlockBufferSize());
        }
    }

    public boolean isRegionProcessed(BlockRegion region) {
        //FIXME: why do we check whether the block buffer component is empty, but compare the internal buffer (potentially newer that the
        //       component against the region?
        if (!blockBufferComponent.blockBuffer.isEmpty()) {
            for (Vector3i pos : buffer.keySet()) {
                if (region.contains(pos)) {
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
