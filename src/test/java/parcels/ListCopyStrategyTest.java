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
package parcels;

import org.junit.Test;
import org.terasology.commonworld.Orientation;
import org.terasology.dynamicCities.parcels.DynParcel;
import org.terasology.dynamicCities.parcels.Zone;
import org.terasology.math.geom.Rect2i;
import org.terasology.reflection.copy.CopyStrategy;
import org.terasology.reflection.copy.CopyStrategyLibrary;
import org.terasology.reflection.copy.strategy.ListCopyStrategy;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 */
public class ListCopyStrategyTest {

    @Test
    public void testDynParcelList() throws NoSuchMethodException {


        List<DynParcel> list = new ArrayList<>();
        DynParcel parcel = new DynParcel(Rect2i.EMPTY, Orientation.EAST, Zone.CLERICAL, 0);
        list.add(parcel);
        list.add(parcel);
        list.add(parcel);
        list.add(parcel);

        ListCopyStrategy<DynParcel> strategy = new ListCopyStrategy<>(new CopyStrategyLibrary.ReturnAsIsStrategy());
        List<DynParcel> copiedList = strategy.copy(list);
        assertEquals(4, copiedList.size());
        assertEquals(parcel.getShape(), copiedList.get(0).getShape());
        assertEquals(parcel.getGenericBuildings(), copiedList.get(1).getGenericBuildings());
        assertEquals(parcel.getOrientation(), copiedList.get(2).getOrientation());
        assertEquals(parcel.getHeight(), copiedList.get(3).getHeight());
    }

    /**
     * The default copy strategy - returns the original value.
     *
     * @param <T>
     */
    private static class ReturnAsIsStrategy<T> implements CopyStrategy<T> {

        @Override
        public T copy(T value) {
            return value;
        }
    }
}
