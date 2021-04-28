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
package org.terasology.dynamicCities.utilities;

import com.google.common.collect.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.utilities.random.MersenneRandom;
import org.terasology.math.TeraMath;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to define a probability distribution for values of type T.
 * They are retrieved with a uniform random number generation.
 * @param <T>
 */
public class ProbabilityDistribution<T> {

    private Logger logger = LoggerFactory.getLogger(ProbabilityDistribution.class);
    private Map<Range, T> ranges;
    private MersenneRandom rng;


    public ProbabilityDistribution (long seed) {
        rng = new MersenneRandom(seed);
    }

    public void initialise (Map<T, Float> probabilites) {
        ranges = new HashMap<>();
        //check if sum of probabilites is 1
        Float sum = 0f;
        for (Float probability : probabilites.values()) {
            sum += probability;
        }
        if (!(TeraMath.fastAbs(sum - 1) < 0.01f)) {
            logger.error("Error initialising ProbabilityDistribution! Sum of probabilites was not 1!");
            return;
        }
        float lastIndex = 0;
        for(Map.Entry<T, Float> entry : probabilites.entrySet()) {
            Range<Float> range = Range.closedOpen(lastIndex, lastIndex + entry.getValue());
            lastIndex += entry.getValue();
            ranges.put(range, entry.getKey());
        }



    }

    public T get() {
        float random = rng.nextFloat(0, 1);
        for (Range<Float> range : ranges.keySet()) {
            if (range.contains(random)) {
                return ranges.get(range);
            }
        }
        logger.error("Could not retrieve a valid value with a random number value of " + random);
        return null;
    }

}
