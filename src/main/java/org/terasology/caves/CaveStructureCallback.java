/*
 * Copyright 2014 MovingBlocks
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

import org.terasology.customOreGen.Structure;
import org.terasology.customOreGen.StructureNodeType;
import org.terasology.math.Vector3i;
import org.terasology.world.generation.facets.base.BaseBooleanFieldFacet3D;

public class CaveStructureCallback implements Structure.StructureCallback {
    private BaseBooleanFieldFacet3D facet;

    public CaveStructureCallback(BaseBooleanFieldFacet3D facet) {
        this.facet = facet;
    }

    @Override
    public boolean canReplace(int x, int y, int z) {
        return facet.getRelativeRegion().encompasses(x, y, z);
    }

    @Override
    public void replaceBlock(Vector3i position, StructureNodeType structureNodeType, Vector3i distanceToCenter) {
        if (canReplace(position.x, position.y, position.z)) {
            facet.set(position, true);
        }
    }
}
