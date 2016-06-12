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
package org.terasology.dynamicCities.buildings;

import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.registry.Share;

import java.util.List;

/**
 * This is used to keep track of possible buildings, their construction plans and attributes
 */
@Share(value = BuildingManager.class)
@RegisterSystem
public class BuildingManager extends BaseComponentSystem {

    //List of available residential buildings
    private List<GenericBuilding> resiBldgs;
    //List of available clerical buildings
    private List<GenericBuilding> cleriBldgs;
    //List of available governmental buildings
    private List<GenericBuilding> governBldgs;
    //List of available commercial buildings
    private List<GenericBuilding> comBlgds;

    @Override
    public void initialise() {
        //get all genericBuilding plans and add it to the corresponding list
    }
}
