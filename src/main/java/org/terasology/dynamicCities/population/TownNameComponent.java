// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.population;


import org.terasology.engine.entitySystem.Component;

import java.util.List;

public class TownNameComponent implements Component {
    public String themeName;
    public List<String> prefixes;
    public List<String> postfixes;
    public List<String> nameList;
}
