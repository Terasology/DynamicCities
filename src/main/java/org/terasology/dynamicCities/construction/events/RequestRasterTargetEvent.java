/*
 * Copyright 2017 MovingBlocks
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
package org.terasology.dynamicCities.construction.events;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.entitySystem.event.ConsumableEvent;
import org.terasology.math.geom.Rect2i;

/**
 * Emitted when DynamicCities needs a {@link org.terasology.cities.raster.RasterTarget} for rasterizing buildings
 */
public class RequestRasterTargetEvent implements ConsumableEvent {
    public RasterTarget rasterTarget;
    public BlockTheme theme;
    public Rect2i shape;
    private boolean consumed;

    public RequestRasterTargetEvent(BlockTheme theme, Rect2i shape) {
        this.shape = shape;
        this.theme = theme;
    }

    @Override
    public boolean isConsumed() {
        return consumed;
    }

    @Override
    public void consume() {
        consumed = true;
    }
}
