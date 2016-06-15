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

package org.terasology.dynamicCities.rasterizer.window;

import org.terasology.cities.BlockTheme;
import org.terasology.cities.raster.Pen;
import org.terasology.cities.raster.Pens;
import org.terasology.cities.raster.RasterTarget;
import org.terasology.cities.raster.RasterUtil;
import org.terasology.cities.window.RectWindow;
import org.terasology.commonworld.heightmap.HeightMap;

/**
 * Converts {@link RectWindow} into blocks (or air actually)
 */
public class RectWindowRasterizer extends WindowRasterizer<RectWindow> {

    /**
     * @param theme the block theme to use
     */
    public RectWindowRasterizer(BlockTheme theme) {
        super(theme, RectWindow.class);
    }

    @Override
    public void raster(RasterTarget target, RectWindow wnd, HeightMap hm) {
        Pen pen = Pens.fill(target, wnd.getBaseHeight(), wnd.getTopHeight(), wnd.getBlockType());
        RasterUtil.fillRect(pen, wnd.getArea());
    }

}
