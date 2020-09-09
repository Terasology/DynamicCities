// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.buildings.events;

import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.Event;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * A little in between event to link chests to the parcel building entity
 */
public class OnSpawnDynamicStructureEvent implements Event {
    private final BlockRegionTransform transformation;
    private final EntityRef buildingEntity;

    public OnSpawnDynamicStructureEvent(BlockRegionTransform transform, EntityRef buildingEntity) {
        this.transformation = transform;
        this.buildingEntity = buildingEntity;
    }

    public BlockRegionTransform getTransformation() {
        return transformation;
    }

    public EntityRef getBuildingEntity() {
        return buildingEntity;
    }
}
