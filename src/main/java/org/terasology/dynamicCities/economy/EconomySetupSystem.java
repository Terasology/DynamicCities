// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
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
