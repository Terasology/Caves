// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;

/**
 * An example Facet for placing objects inside caves.
 */
public class CaveObjectFacet extends SparseObjectFacet3D<CaveObjectType> {

    public CaveObjectFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
