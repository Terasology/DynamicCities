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

package org.terasology.dynamicCities.population;


import org.terasology.engine.entitySystem.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CultureComponent implements Component {

    public Map<String, Float> buildingNeedPerZone;
    public String name;
    public List<String> availableBuildings;
    public List<String> residentialZones;
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
}
