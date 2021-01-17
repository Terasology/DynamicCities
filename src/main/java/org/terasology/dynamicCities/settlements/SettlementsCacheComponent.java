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


import org.joml.RoundingMode;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.terasology.entitySystem.Component;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.logic.location.LocationComponent;
import org.terasology.network.Replicate;

import java.util.List;
import java.util.Map;


public final class SettlementsCacheComponent implements Component {
    @Replicate
    public Map<Vector2i, EntityRef> settlementEntities;

    @Replicate
    public List<EntityRef> networkCache;

    public SettlementsCacheComponent() { }

    public void add(EntityRef settlement) {
        Vector3f pos3f = settlement.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        Vector2i pos = new Vector2i(new Vector2f(pos3f.x(), pos3f.z()), RoundingMode.FLOOR);
        settlementEntities.put(pos, settlement);

    }

    public EntityRef get(Vector2i position) {
        return settlementEntities.get(position.toString());
    }


}
