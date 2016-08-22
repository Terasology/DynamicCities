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
package org.terasology.dynamicCities.resource;

import org.terasology.reflection.MappedContainer;

/**
 * This is not to be confused with the economy resource definition, although they can possibly converted into such later or replaced by them.
 * This is to keep track of a region's resources as per actual blocks.
 */
@MappedContainer
public class Resource {

    public int amount;
    private ResourceType type;

    public Resource(ResourceType type, int amount) {
        this.type = type;
        this.amount = amount;
    }

    public Resource(ResourceType type) {
        this.type = type;
        this.amount = 1;
    }
    public Resource() {}
    public ResourceType getType() {
        return type;
    }


}

