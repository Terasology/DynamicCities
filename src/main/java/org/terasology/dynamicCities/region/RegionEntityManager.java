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
package org.terasology.dynamicCities.region;


import org.terasology.dynamicCities.region.components.ActiveRegionComponent;
import org.terasology.dynamicCities.region.components.RegionEntitiesComponent;
import org.terasology.dynamicCities.region.components.UnassignedRegionComponent;
import org.terasology.dynamicCities.region.components.UnregisteredRegionComponent;
import org.terasology.dynamicCities.region.events.AssignRegionEvent;
import org.terasology.dynamicCities.settlements.SettlementEntityManager;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.entitySystem.event.ReceiveEvent;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterMode;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.entitySystem.systems.UpdateSubscriberSystem;
import org.terasology.logic.nameTags.NameTagComponent;
import org.terasology.registry.In;
import org.terasology.registry.Share;
import org.terasology.rendering.nui.Color;

@Share(value = RegionEntityManager.class)
@RegisterSystem(RegisterMode.AUTHORITY)
public class RegionEntityManager extends BaseComponentSystem implements UpdateSubscriberSystem {

    @In
    private EntityManager entityManager;

    @In
    private SettlementEntityManager settlementEntities;

    private RegionEntitiesComponent regionEntitiesComponent;

    @Override
    public void initialise() {
        regionEntitiesComponent = new RegionEntitiesComponent(96);
    }

    private int counter = 100;

    /**
     * Current Task: play around with intervals of deletion
     * @param delta The time (in seconds) since the last engine update.
     */
    @Override
    public void update(float delta) {
        Iterable<EntityRef> unregisteredRegions = entityManager.getEntitiesWith(UnregisteredRegionComponent.class);
        for (EntityRef region : unregisteredRegions) {
            regionEntitiesComponent.add(region);
            region.removeComponent(UnregisteredRegionComponent.class);
            region.addComponent(new UnassignedRegionComponent());
            NameTagComponent nT = region.getComponent(NameTagComponent.class);
            nT.textColor = Color.GREEN;
            region.saveComponent(nT);
        }

        counter++;

        if (counter != 0) {
            return;
        }

        for (String posString : regionEntitiesComponent.cellGrid.keySet()) {
            if (!regionEntitiesComponent.processed.contains(posString) && (!settlementEntities.checkMinDistanceCell(posString)
                    /*|| regionEntitiesComponent.checkSidesLoadedLong(posString)*/)) {
                regionEntitiesComponent.clearCell(posString);
            }
        }
        counter = 100;
    }

    @ReceiveEvent(components = {UnassignedRegionComponent.class})
    public void assignRegion(AssignRegionEvent event, EntityRef region) {
        region.addComponent(new ActiveRegionComponent());
        region.removeComponent(UnassignedRegionComponent.class);
        NameTagComponent nT = region.getComponent(NameTagComponent.class);
        nT.textColor = Color.YELLOW;
        region.saveComponent(nT);
    }

    
    public RegionEntitiesComponent getRegionEntitiesComponent() {
        return regionEntitiesComponent;
    }
}
