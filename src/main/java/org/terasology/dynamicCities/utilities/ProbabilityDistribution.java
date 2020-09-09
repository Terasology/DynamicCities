// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.utilities;

import com.google.common.collect.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.utilities.random.MersenneRandom;
import org.terasology.math.TeraMath;

import java.util.HashMap;
import java.util.Map;

/**
 * Used to define a probability distribution for values of type T. They are retrieved with a uniform random number
 * generation.
 *
 * @param <T>
 */
public class ProbabilityDistribution<T> {

    private final Logger logger = LoggerFactory.getLogger(ProbabilityDistribution.class);
    private final MersenneRandom rng;
    private Map<Range, T> ranges;


    public ProbabilityDistribution(long seed) {
        rng = new MersenneRandom(seed);
    }

    public void initialise(Map<T, Float> probabilites) {
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
        for (Map.Entry<T, Float> entry : probabilites.entrySet()) {
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
