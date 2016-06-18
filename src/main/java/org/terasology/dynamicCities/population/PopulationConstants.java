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

import org.terasology.math.geom.Vector2i;

/**
 *
 * This should be defined as a culture component later on.
 */
public abstract class PopulationConstants {

    
    //Define genericBuilding space needs per person
    public static final int RESIDENTIAL_PER_PERSON = 8;
    public static final int COMMERCIAL_PER_PERSON = 4;
    public static final int GOVERNMENTAL_PER_PERSON = 2;
    public static final int MILITARY_PER_PERSON = 2;
    public static final int CLERICAL_PER_PERSON = 2;

    //Define x=max, y=min size of buildings
    public static final Vector2i MAXMIN_RESIDENTIAL = new Vector2i(300, 100);
    public static final Vector2i MAXMIN_COMMERCIAL = new Vector2i(600, 300);
    public static final Vector2i MAXMIN_GOVERNMENTAL = new Vector2i(600, 300);
    public static final Vector2i MAXMIN_MILITARY = new Vector2i(200, 50);
    public static final Vector2i MAXMIN_CLERICAL = new Vector2i(600, 300);


    public static final float GROWTH_RATE = 1f;
    /*
    //Define resource needs per person
    public static final float wood_per_person;
    public static final float stone_per_person;
    public static final float water_per_person;
    public static final float food_per_person;

    //Define at which population size a wall will be built
    int WALL_POP_THRESHOLD;

    //Define productivity modifier for resources
    public static final float WOOD_MOD;
    public static final float STONE_MOD;
    public static final float WATER_MOD;
    public static final float FOOD_MOD;
    */
}

