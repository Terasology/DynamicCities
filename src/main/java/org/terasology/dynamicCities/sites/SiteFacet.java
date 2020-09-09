// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.dynamicCities.sites;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseFacet2D;

/**
 *
 */
public class SiteFacet extends BaseFacet2D {

    private SiteComponent siteComponent = null;

    public SiteFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    public SiteComponent getSiteComponent() {
        return siteComponent;
    }

    public void setSiteComponent(SiteComponent siteComponent) {
        this.siteComponent = siteComponent;
    }

}
