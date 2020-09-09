// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements;


import org.terasology.engine.entitySystem.Component;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.Replicate;
import org.terasology.math.geom.Vector2i;
import org.terasology.math.geom.Vector3f;

import java.util.List;
import java.util.Map;


public final class SettlementsCacheComponent implements Component {
    @Replicate
    public Map<String, EntityRef> settlementEntities;

    @Replicate
    public List<EntityRef> networkCache;

    public SettlementsCacheComponent() {
    }

    public void add(EntityRef settlement) {
        Vector3f pos3f = settlement.getComponent(LocationComponent.class).getWorldPosition();
        Vector2i pos = new Vector2i(pos3f.x(), pos3f.z());
        settlementEntities.put(pos.toString(), settlement);

    }

    public EntityRef get(Vector2i position) {
        return settlementEntities.get(position.toString());
    }


}
