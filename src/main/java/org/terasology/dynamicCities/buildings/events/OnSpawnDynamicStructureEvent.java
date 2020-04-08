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
package org.terasology.dynamicCities.buildings.events;

import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.Event;
import org.terasology.structureTemplates.util.BlockRegionTransform;

/**
 * A little in between event to link chests to the parcel building entity
 */
public class OnSpawnDynamicStructureEvent implements Event {
    private BlockRegionTransform transformation;
    private EntityRef buildingEntity;

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
