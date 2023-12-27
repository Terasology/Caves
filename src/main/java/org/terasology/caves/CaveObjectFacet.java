// Copyright 2019 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.caves;

import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.SparseObjectFacet3D;

/**
 * An example Facet for placing objects inside caves.
 */
public class CaveObjectFacet extends SparseObjectFacet3D<CaveObjectType> {

    public CaveObjectFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
