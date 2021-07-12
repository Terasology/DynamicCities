// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.population;


import com.google.common.collect.Lists;
import org.terasology.gestalt.entitysystem.component.Component;

import java.util.List;

public class TownNameComponent implements Component<TownNameComponent> {
    public String themeName;
    public List<String> prefixes = Lists.newArrayList();
    public List<String> postfixes = Lists.newArrayList();
    public List<String> nameList = Lists.newArrayList();

    @Override
    public void copy(TownNameComponent other) {
        this.themeName = other.themeName;
        this.prefixes = Lists.newArrayList(other.prefixes);
        this.postfixes = Lists.newArrayList(other.postfixes);
        this.nameList = Lists.newArrayList(other.nameList);
    }
}
