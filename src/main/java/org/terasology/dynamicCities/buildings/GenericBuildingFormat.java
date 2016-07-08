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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.assets.ResourceUrn;
import org.terasology.assets.format.AbstractAssetFileFormat;
import org.terasology.assets.format.AssetDataFile;
import org.terasology.assets.module.annotations.RegisterAssetFileFormat;

import java.io.IOException;
import java.util.List;

@RegisterAssetFileFormat
public class GenericBuildingFormat extends AbstractAssetFileFormat<GenericBuildingData> {


    private Logger logger = LoggerFactory.getLogger(GenericBuildingFormat.class);

    public GenericBuildingFormat() {
        // Supported file extensions
        super("building");
    }

    @Override
    public GenericBuildingData load(ResourceUrn urn, List<AssetDataFile> inputs) throws IOException {/*
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputs.get(0).openStream(), Charsets.UTF_8))) {
            BuildingGenerator buildingGenerator;
            String generator = null;
            while (reader.ready()) {
                String nextLine = reader.readLine();
                String[] attributes = nextLine.split(" ");
                generator = attributes[1];
            }
            if (generator == null) {
                logger.error("Can not load buildinggenerator from reader " + reader.toString());
                return null;
            }
            return new GenericBuildingData(generator);
        }*/
        return null;
    }
}

