// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.resource;

public enum ResourceType {
    WOOD, STONE, FOOD, WATER, GRASS, NULL;

    public String toString() {
        switch (this) {
            case WOOD:
                return "Wood";
            case STONE:
                return "Stone";
            case FOOD:
                return "FOOD";
            case WATER:
                return "Water";
            case GRASS:
                return "Grass";
        }
        return "NULL";
    }
}
