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
import org.terasology.world.generation.facets.base.BaseFieldFacet2D;

/**
 * This {@link Facet} adds to the {@link CaveFacet} by indicating the Y (height) value throughout the
 * {@link Region3i} that represents the floor of the {@code CaveFacet}, if present. If no Cave floor is present then
 * {@link Float#NaN} will be the value for that location.
 *
 * <p><em>Usage notes:</em> The value of the {@code CaveFloor} is the height of the first solid block. So in order
 * to place something "on the floor" of the cave you should place it at {@code caveFloorFacet.get(x, z) + 1}</p>
 */
public class CaveFloorFacet extends BaseFieldFacet2D {
    public CaveFloorFacet(Region3i targetRegion, Border3D border) {
        super(targetRegion, border);
    }
}
