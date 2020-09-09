// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.dynamicCities.construction.events;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.engine.entitySystem.event.ConsumableEvent;
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
