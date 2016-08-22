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


import org.terasology.dynamicCities.construction.components.BlockBufferComponent;
import org.terasology.dynamicCities.settlements.SettlementConstants;
import org.terasology.dynamicCities.settlements.events.SettlementGrowthEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.entity.lifecycleEvents.BeforeDeactivateComponent;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.math.Region3i;
import org.terasology.math.geom.Vector3i;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.WorldProvider;
import org.terasology.world.block.Block;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Share(BlockBufferSystem.class)
@RegisterSystem
public class BlockBufferSystem extends BaseComponentSystem {

    @In
    private EntityManager entityManager;

    @In
    private WorldProvider worldProvider;

    private EntityRef blockBufferEntity;
    private BlockBufferComponent blockBufferComponent;

    @Override
    public void postBegin() {
        Iterator<EntityRef> blockBufferIterator = entityManager.getEntitiesWith(BlockBufferComponent.class).iterator();
        blockBufferEntity = blockBufferIterator.hasNext() ? blockBufferIterator.next() : null;
        if (blockBufferEntity == null) {
            blockBufferComponent = new BlockBufferComponent();
            blockBufferComponent.blockBuffer = new ArrayList<>();
            blockBufferEntity = entityManager.create(blockBufferComponent);
            blockBufferEntity.setAlwaysRelevant(true);
        }
        blockBufferComponent = blockBufferEntity.getComponent(BlockBufferComponent.class);
        if (blockBufferComponent.blockBuffer == null) {
            blockBufferComponent.blockBuffer = new ArrayList<>(SettlementConstants.BLOCKBUFFER_SIZE);
        }
    }

    public void saveBlock(Vector3i pos, Block block) {
        blockBufferComponent.blockBuffer.add(new BufferedBlock(pos, block));
    }

    public void setBlock() {
        if (!blockBufferComponent.blockBuffer.isEmpty()){
            Iterator<BufferedBlock> iterator = blockBufferComponent.blockBuffer.iterator();
            BufferedBlock block = iterator.next();
            if (worldProvider.isBlockRelevant(block.pos)) {
                worldProvider.setBlock(block.pos, block.blockType);
                blockBufferComponent.blockBuffer.remove(block);
            }
        }
    }

    public void setBlocks() {
        List<BufferedBlock> removedBlocks = new ArrayList<>();
        if (!blockBufferComponent.blockBuffer.isEmpty()) {
            for (BufferedBlock block : blockBufferComponent.blockBuffer) {
                if (worldProvider.isBlockRelevant(block.pos)) {
                    worldProvider.setBlock(block.pos, block.blockType);
                    removedBlocks.add(block);
                }
            }
            blockBufferComponent.blockBuffer.removeAll(removedBlocks);
        }
    }

    public boolean isRegionProcessed(Region3i region3i) {
        if (!blockBufferComponent.blockBuffer.isEmpty()) {
            for (BufferedBlock block : blockBufferComponent.blockBuffer) {
                if (region3i.encompasses(block.pos)) {
                    return false;
                }
            }
        }
        return true;
    }

    public int getBlockBufferSize() {
        return blockBufferComponent.blockBuffer.size();
    }

    @ReceiveEvent
    public void onWorldPurge(SettlementGrowthEvent event, EntityRef entityRef) {
        blockBufferEntity.saveComponent(blockBufferComponent);
    }
    @ReceiveEvent(components = BlockBufferComponent.class)
    public void onWorldPurge(BeforeDeactivateComponent event, EntityRef entityRef) {
        blockBufferEntity.saveComponent(blockBufferComponent);
    }

}
