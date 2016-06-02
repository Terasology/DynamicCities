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
package org.terasology.dynamicCities.facets;

import com.google.common.base.Preconditions;
import org.terasology.dynamicCities.resource.Resource;
import org.terasology.math.Region3i;
import org.terasology.math.geom.BaseVector2i;
import org.terasology.math.geom.Vector2i;
import org.terasology.world.generation.Border3D;

import java.util.HashMap;
import java.util.Map;

public class ResourceFacet extends Grid2DFacet {


    private Region3i region;
    private Map<String, Resource>[] data;

    @SuppressWarnings(value = "unchecked")
    public ResourceFacet(Region3i targetRegion, Border3D border, int gridSize) {
        super(targetRegion, border, gridSize);
        data = new HashMap[gridWorldRegion.area()];
        for (int i = 0; i < data.length; i++) {
            data[i] = new HashMap<>();
        }
    }

    //Modify that to get resources per grid cell!
    public void addResource(Resource resource, Vector2i pos) {
        if (get(pos).containsKey(resource.getType().toString())) {
            get(pos).get(resource.getType().toString()).amount += resource.amount;
        } else {
            get(pos).put(resource.getType().toString(), resource);
        }
    }

    public int getResourceSum(String resourceType) {
        int sum = 0;
        for (Map<String, Resource> map : data) {
            if (map.containsKey(resourceType)) {
                sum += map.get(resourceType).amount;
            }
        }
        return sum;
    }

    public Map<String, Resource> get(int x, int y) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        return data[getRelativeGridIndex(gridPos.x(), gridPos.y())];
    }

    public Map<String, Resource> get(BaseVector2i pos) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        return get(gridPos.x(), gridPos.y());
    }

    public Map<String, Resource> getWorld(int x, int y) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        return data[getWorldGridIndex(gridPos.x(), gridPos.y())];
    }

    public Map<String, Resource> getWorld(BaseVector2i pos) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        return getWorld(gridPos.x(), gridPos.y());
    }

    public Map<String, Resource>[] getInternal() {
        return data;
    }

    public void set(int x, int y, Map<String, Resource> value) {
        BaseVector2i gridPos = getRelativeGridPoint(x, y);
        data[getRelativeGridIndex(gridPos.x(), gridPos.y())] = value;
    }

    public void set(BaseVector2i pos, Map<String, Resource> value) {
        BaseVector2i gridPos = getRelativeGridPoint(pos.x(), pos.y());
        set(pos.x(), pos.y(), value);
    }

    public void setWorld(int x, int y, Map<String, Resource> value) {
        BaseVector2i gridPos = getWorldGridPoint(x, y);
        data[getWorldGridIndex(gridPos.x(), gridPos.y())] = value;
    }

    public void setWorld(BaseVector2i pos, Map<String, Resource> value) {
        BaseVector2i gridPos = getWorldGridPoint(pos.x(), pos.y());
        setWorld(gridPos.x(), gridPos.y(), value);
    }

    public void set(Map[] newData) {
        Preconditions.checkArgument(newData.length == data.length, "New data must have same length as existing");
        System.arraycopy(newData, 0, data, 0, newData.length);
    }

}


