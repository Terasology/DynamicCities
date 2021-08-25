// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.population;


import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CultureComponent implements Component<CultureComponent> {

    public Map<String, Float> buildingNeedPerZone = Maps.newHashMap();
    public String name;
    public List<String> availableBuildings = Lists.newArrayList();
    public List<String> residentialZones = Lists.newArrayList();
    public float growthRate;
    public String theme = null;


    public float getBuildingNeedsForZone(String zone) {
        if (buildingNeedPerZone.containsKey(zone)) {
            return buildingNeedPerZone.get(zone);
        } else {
            return 0;
        }
    }

    public float getProcentualOfZone(String zone) {
        float total = 0;
        for (Float need : buildingNeedPerZone.values()) {
            total += need;
        }
        if (total == 0) {
            return -1;
        }
        return getBuildingNeedsForZone(zone) / total;
    }
    public Map<String, Float> getProcentualsForZone () {
        Map<String, Float> procentuals = new HashMap<>(buildingNeedPerZone.size());
        for (String zone : buildingNeedPerZone.keySet()) {
            procentuals.put(zone, getProcentualOfZone(zone));
        }
        return procentuals;
    }

    @Override
    public void copyFrom(CultureComponent other) {
        this.buildingNeedPerZone = Maps.newHashMap(other.buildingNeedPerZone);
        this.name = other.name;
        this.availableBuildings = Lists.newArrayList(other.availableBuildings);
        this.residentialZones = Lists.newArrayList(other.residentialZones);
        this.growthRate = other.growthRate;
        this.theme = other.theme;
    }
}
