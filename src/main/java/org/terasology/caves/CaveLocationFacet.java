// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Border3D;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.facets.base.BaseObjectFacet2D;

/**
 * This {@link Facet} adds to the {@link CaveFacet} by indicating the Y (height) value throughout the {@link Region3i}
 * for both the floor and ceiling of the caves in the {@code CaveFacet}, if present. If no Cave floor or ceiling is
 * present then {@link Float#NaN} will be the value for that location.
 *
 * <p><em>Usage notes:</em> The value of the floor / ceiling is the height of the last relevant solid block.
 * So in order to place something "on the floor" / "on the ceiling" of the cave you should place it at {@code
 * caveLocation.floor + 1} / {@code caveLocation.ceiling - 1}</p>
 */
public class CaveLocationFacet extends BaseObjectFacet2D<CaveLocation[]> {
    public CaveLocationFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border, CaveLocation[].class);
    }
}
