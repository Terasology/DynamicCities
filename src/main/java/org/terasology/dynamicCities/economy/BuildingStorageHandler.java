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
package org.terasology.dynamicCities.economy;


import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.management.AssetManager;
import org.terasology.dynamicCities.buildings.components.ChestStorageComponent;
import org.terasology.economy.StorageComponentHandler;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.logic.inventory.InventoryComponent;
import org.terasology.logic.inventory.InventoryManager;
import org.terasology.logic.inventory.InventoryUtils;
import org.terasology.logic.inventory.ItemComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.entity.BlockCommands;
import org.terasology.world.block.family.BlockFamily;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.ArrayList;
import java.util.Set;

/**
 * This handles entities with multiple storage entities, mainly building-entities with chests
 * TODO: Delete consumption/production chest differentiation
 */
@Share(BuildingStorageHandler.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class BuildingStorageHandler  extends BaseComponentSystem implements StorageComponentHandler<ChestStorageComponent> {

    @In
    private AssetManager assetManager;

    @In
    private EntityManager entityManager;

    @In
    private BlockCommands blockCommands;

    @In
    private BlockManager blockManager;

    @In
    private InventoryManager inventoryManager;

    private BlockItemFactory blockItemFactory;
    private Logger logger = LoggerFactory.getLogger(BuildingStorageHandler.class);

    @Override
    public void initialise() {
        blockItemFactory = new BlockItemFactory(entityManager);
    }

    @Override
    public int store(ChestStorageComponent chestStorageComponent, String resource, int amount) {
        EntityRef item = getItemEntity(resource);
        byte byteAmount;
        if (item == EntityRef.NULL) {
            return amount;
        }




        for (EntityRef entityRef : chestStorageComponent.chests) {
            int amountForChest = getItemCapacityForChest(entityRef, item);
            amountForChest = (amountForChest > amount) ? amount : amountForChest;
            if (amountForChest >= Byte.MAX_VALUE) {
                byteAmount = Byte.MAX_VALUE;
            } else {
                byteAmount = (byte) amountForChest;
            }
            ItemComponent itemComponent = item.getComponent(ItemComponent.class);
            itemComponent.stackCount = byteAmount;
            item.saveComponent(itemComponent);
            inventoryManager.giveItem(entityRef, EntityRef.NULL, item);
            amount -= amountForChest;

            if (amount == 0) {
                return 0;
            }
            /*
            GiveItemEvent giveItemEvent = new GiveItemEvent(item);
            entityRef.send(giveItemEvent);
            if (giveItemEvent.isHandled()) {
                return 0;
            }*/
        }
        return amount;

    }

    @Override
    public int draw(ChestStorageComponent chestStorageComponent, String resource, int amount) {
        EntityRef item = getItemEntity(resource);

        for (EntityRef entityRef : chestStorageComponent.chests) {
            int amountForChest = getItemCountForChest(entityRef, item);
            while (amountForChest != 0) {
                int slot = getSlotWithItem(entityRef, item);
                if (slot == -1) {
                    break;
                }
                int amountForSlot = InventoryUtils.getStackCount(InventoryUtils.getItemAt(entityRef, slot));
                if (inventoryManager.removeItem(entityRef, EntityRef.NULL, slot, true, amountForSlot) != null) {
                    amount -= amountForSlot;
                }
            }

        }
        return amount;
    }

    @Override
    public int availableResourceAmount(ChestStorageComponent chestStorageComponent, String resource) {
        int amount = 0;
        EntityRef item = getItemEntity(resource);
        for (EntityRef entityRef : chestStorageComponent.chests) {
            amount += getItemCountForChest(entityRef, item);
        }
        return amount;
    }

    @Override
    public int availableResourceCapacity(ChestStorageComponent chestStorageComponent, String resource) {

        int capacity = 0;
        EntityRef item = getItemEntity(resource);
        for (EntityRef entityRef : chestStorageComponent.chests) {
            capacity += getItemCapacityForChest(entityRef, item);
        }
        return capacity;
    }

    @Override
    public Class getStorageComponentClass() {
        return ChestStorageComponent.class;
    }

    @Override
    public Component getTestComponent() {
        ChestStorageComponent chestStorageComponent = new ChestStorageComponent();
        chestStorageComponent.chests = new ArrayList<>();
        ResourceUrn resourceUrn = assetManager.resolve("Core:chest", Prefab.class).iterator().next();
        Prefab chestPrefab = assetManager.getAsset(resourceUrn, Prefab.class).get();
        EntityRef chest = entityManager.create(chestPrefab);
        chestStorageComponent.chests.add(chest);
        return chestStorageComponent;
    }
    @Override
    public String getTestResource() {
        return "core:torch";
    }

    private EntityRef getItemEntity(String resource) {
        Set<ResourceUrn> matches = assetManager.resolve(resource, Prefab.class);
        switch(matches.size()) {
            case 0:
                logger.error("No item found matching resource string " + resource);
                return EntityRef.NULL;
            case 1:
                Prefab prefab = assetManager.getAsset(matches.iterator().next(), Prefab.class).orElse(null);
                if (prefab != null && prefab.getComponent(ItemComponent.class) != null) {
                    EntityRef item = entityManager.create(prefab);
                    return item;
                } else {
                    BlockFamily blockFamily = blockManager.getBlockFamily(resource);
                    EntityRef item = blockItemFactory.newInstance(blockFamily, 1);
                    if (!item.exists()) {
                        throw new IllegalArgumentException("Unknown block or item");
                    }
                    return item;
                }
            default:
                StringBuilder builder = new StringBuilder();
                builder.append("Requested item \"");
                builder.append(resource);
                builder.append("\": matches ");
                Joiner.on(" and ").appendTo(builder, matches);
                builder.append(". Please fully specify one.");
                logger.error(builder.toString());
                return EntityRef.NULL;
        }
    }

    private int getItemCountForChest(EntityRef entityRef, EntityRef item) {
        int amount = 0;
        int slotCount = InventoryUtils.getSlotCount(entityRef);
        for (int i = 0; i < slotCount; i++) {
            if (InventoryUtils.isSameItem(InventoryUtils.getItemAt(entityRef, i), item)) {
                amount += InventoryUtils.getStackCount(InventoryUtils.getItemAt(entityRef, i));
            }
        }
        return amount;
    }

    private int getItemCapacityForChest(EntityRef entityRef, EntityRef item) {
        int capacity = 0;
        InventoryComponent inventoryComponent = entityRef.getComponent(InventoryComponent.class);
        int slotCount = InventoryUtils.getSlotCount(entityRef);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            logger.error("No item component found for item " + item.toFullDescription());
            return 0;
        }
        for (int i = 0; i < slotCount; i++) {
            EntityRef targetItem = InventoryUtils.getItemAt(entityRef, i);
            if (InventoryUtils.canStackInto(item, targetItem)) {
                capacity += itemComponent.maxStackSize - InventoryUtils.getStackCount(targetItem);
            }
        }
        return capacity;
    }

    private int getSlotWithItem(EntityRef entity, EntityRef item) {
        int slotCount = InventoryUtils.getSlotCount(entity);
        for (int i = 0; i < slotCount; i++) {
            if (InventoryUtils.isSameItem(InventoryUtils.getItemAt(entity, i), item)) {
                return i;
            }
        }
        return -1;
    }
}
