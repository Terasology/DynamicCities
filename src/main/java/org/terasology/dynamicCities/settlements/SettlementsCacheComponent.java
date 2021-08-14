// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.settlements;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.joml.RoundingMode;
import org.joml.Vector2i;
import org.joml.Vector3f;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.logic.location.LocationComponent;
import org.terasology.engine.network.Replicate;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;
import java.util.Map;

public final class SettlementsCacheComponent implements Component<SettlementsCacheComponent> {
    @Replicate
    public Map<Vector2i, EntityRef> settlementEntities = Maps.newHashMap();

    @Replicate
    public List<EntityRef> networkCache = Lists.newArrayList();

    public SettlementsCacheComponent() { }

    public void add(EntityRef settlement) {
        Vector3f pos3f = settlement.getComponent(LocationComponent.class).getWorldPosition(new Vector3f());
        Vector2i pos = new Vector2i(pos3f.x(), pos3f.z(), RoundingMode.FLOOR);
        settlementEntities.put(pos, settlement);
    }

    public EntityRef get(Vector2i position) {
        return settlementEntities.get(position);
    }

    @Override
    public void copyFrom(SettlementsCacheComponent other) {
        this.settlementEntities = Maps.newHashMap(other.settlementEntities);
        this.networkCache = Lists.newArrayList(other.networkCache);
    }
}
