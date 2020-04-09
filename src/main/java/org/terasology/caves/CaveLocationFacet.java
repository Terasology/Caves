/*
 * Copyright 2019 MovingBlocks
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
package org.terasology.caves;

import org.terasology.math.Region3i;
import org.terasology.world.generation.Border3D;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.facets.base.BaseObjectFacet2D;

/**
 * This {@link Facet} adds to the {@link CaveFacet} by indicating the Y (height) value throughout the
 * {@link Region3i} for both the floor and ceiling of the caves in the {@code CaveFacet}, if present.
 * If no Cave floor or ceiling is present then {@link Float#NaN} will be the value for that location.
 *
 * <p><em>Usage notes:</em> The value of the floor / ceiling is the height of the last relevant solid block.
 * So in order to place something "on the floor" / "on the ceiling" of the cave you should place it at
 * {@code caveLocation.floor + 1} / {@code caveLocation.ceiling - 1}</p>
 */
public class CaveLocationFacet extends BaseObjectFacet2D<CaveLocation[]> {
    public CaveLocationFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border, CaveLocation[].class);
    }
}
