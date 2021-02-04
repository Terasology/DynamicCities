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
package org.terasology.dynamicCities.buildings;


import org.joml.Vector2i;
import org.terasology.entitySystem.Component;

import java.util.List;

public class GenericBuildingComponent implements Component {

    /**
     *  Can either store a composite genericBuildingData from the module cities
     *  or a structured template
     */
    public String name;
    public List<String> templateNames;
    public List<String> generatorNames;
    public String zone;
    public Vector2i minSize;
    public Vector2i maxSize;
    public boolean isEntity;
    public boolean isScaledDown;
    public String resourceUrn;
}
