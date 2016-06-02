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
package org.terasology.dynamicCities.sectors;


import org.terasology.dynamicCities.region.components.ActiveRegionComponent;
import org.terasology.dynamicCities.region.components.RegionEntities;
import org.terasology.dynamicCities.region.components.UnassignedRegionComponent;
import org.terasology.dynamicCities.region.components.UnregisteredRegionComponent;
import org.terasology.dynamicCities.region.events.AssignRegionEvent;
import org.terasology.dynamicCities.region.events.RegionRegisterEvent;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.math.geom.Vector2i;
import org.terasology.registry.In;
import org.terasology.rendering.nui.Color;

import java.util.HashMap;
import java.util.Map;


public class SectorManager extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    private Map<Vector2i, RegionEntities> sectors;

    @Override
    public void initialise() {
        sectors = new HashMap<>();
    }

    @Override
    public void update(float delta) {
        Iterable<EntityRef> unregisteredRegions = entityManager.getEntitiesWith(UnassignedRegionComponent.class);
        for (EntityRef region : unregisteredRegions) {
            region.send(new RegionRegisterEvent());
            region.removeComponent(UnregisteredRegionComponent.class);
            region.addComponent(new UnassignedRegionComponent());
            NameTagComponent nT = region.getComponent(NameTagComponent.class);
            nT.textColor = Color.GREEN;
            region.saveComponent(nT);
        }
    }



    @ReceiveEvent(components = {UnassignedRegionComponent.class})
    public void assignRegion(AssignRegionEvent event, EntityRef region) {
        region.addComponent(new ActiveRegionComponent());
        region.removeComponent(UnassignedRegionComponent.class);
        NameTagComponent nT = region.getComponent(NameTagComponent.class);
        nT.textColor = Color.YELLOW;
        region.saveComponent(nT);
    }

    public Map<Vector2i, RegionEntities> getSectors() {
        return sectors;
    }
}
