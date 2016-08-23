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
package org.terasology.dynamicCities.settlements;


public abstract class SettlementConstants {
    public static final int SETTLEMENT_RADIUS = 160;
    public static final int DISTRICT_GRIDSIZE = 8;
    public static final int MIN_POPULATIONSIZE = 200;
    public static final int MAX_POPULATIONSIZE = 900;
    public static final int MAX_BUILDINGSPAWN = 2;
    public static final int MAX_DISTRICTS = 300;
    public static final int BUILD_RADIUS_INTERVALL = 50;
    public static final int BLOCKS_SET_PER_TICK = 10_000;
    public static final int BLOCKBUFFER_SIZE = 50_000;
    public static final int MAX_TREE_RADIUS = 13;
    public static final float MAX_BUILDABLE_ROUGHNESS = 0.3f;
    public static final int NEEDED_USABLE_REGIONS_FOR_CITY_SPAWN = 15;
}
