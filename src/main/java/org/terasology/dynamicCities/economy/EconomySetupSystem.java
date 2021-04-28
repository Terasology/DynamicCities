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
package org.terasology.dynamicCities.economy;


import org.terasology.economy.handler.MultiInvStorageHandler;
import org.terasology.economy.systems.StorageHandlerLibrary;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.registry.In;

@RegisterSystem(RegisterMode.AUTHORITY)
public class EconomySetupSystem extends BaseComponentSystem {

    @In
    private StorageHandlerLibrary storageHandlerLibrary;

    @In
    private MultiInvStorageHandler multiInvStorageHandler;
    @Override
    public void postBegin() {

        storageHandlerLibrary.registerHandler(new PopulationStorageHandler());
        storageHandlerLibrary.registerHandler(multiInvStorageHandler);
    }
}
