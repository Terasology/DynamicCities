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
package org.terasology.dynamicCities.settlements;


import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;
import org.terasology.reflection.MappedContainer;

import java.util.HashMap;
import java.util.Map;

@MappedContainer
public final class SettlementEntities implements Component {

    public Map<String, EntityRef> settlementEntities = new HashMap<>();

    public SettlementEntities() { }

    public void add(EntityRef settlement) {
        Vector3f pos3f = settlement.getComponent(LocationComponent.class).getWorldPosition();
        Vector2i pos = new Vector2i(pos3f.x(), pos3f.z());
        settlementEntities.put(pos.toString(), settlement);

    }

    public EntityRef get(Vector2i position) {
        return settlementEntities.get(position.toString());
    }

    public Map<String, EntityRef> getMap() {
        return settlementEntities;
    }

}
