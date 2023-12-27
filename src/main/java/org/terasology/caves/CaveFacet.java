// Copyright 2014 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.caves;

import org.joml.Vector3ic;
import org.terasology.engine.world.block.BlockRegion;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.facets.base.BaseBooleanFieldFacet3D;

public class CaveFacet extends BaseBooleanFieldFacet3D {
    public CaveFacet(BlockRegion targetRegion, Border3D border) {
        super(targetRegion, border);
    }

    /**
     * The index of the given position in arrays corresponding to the region this facet covers.
     */
    public int getWorldIndex(Vector3ic pos) {
        return getWorldIndex(pos.x(), pos.y(), pos.z());
    }
}
