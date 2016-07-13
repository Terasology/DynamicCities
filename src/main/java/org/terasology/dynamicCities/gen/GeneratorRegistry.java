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
package org.terasology.dynamicCities.gen;


import com.google.common.collect.ImmutableMap;
import org.terasology.cities.bldg.gen.CommercialBuildingGenerator;
import org.terasology.cities.bldg.gen.RectHouseGenerator;
import org.terasology.cities.bldg.gen.SimpleChurchGenerator;
import org.terasology.cities.bldg.gen.TownHallGenerator;

import java.util.Map;

public abstract class GeneratorRegistry {

    public static final Map<String, Class> GENERATORS = new ImmutableMap.Builder<String, Class>()
            .put("CommercialBuildingGenerator", CommercialBuildingGenerator.class)
            .put("RectHouseGenerator", RectHouseGenerator.class)
            .put("SimpleChurchGenerator", SimpleChurchGenerator.class)
            .put("TownHallGenerator", TownHallGenerator.class)
            .build();



}
