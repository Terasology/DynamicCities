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


import org.terasology.utilities.procedural.WhiteNoise;
import org.terasology.world.generation.FacetProvider;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;

/**
 *
 */
@Produces(SettlementFacet.class)
public class SettlementFacetProvider implements FacetProvider {

    private WhiteNoise nameNoiseGen;

    @Override
    public void setSeed(long seed) {
        this.nameNoiseGen = new WhiteNoise(seed ^ 6207934);
    }

    @Override
    public void process(GeneratingRegion region) {


    }
}
