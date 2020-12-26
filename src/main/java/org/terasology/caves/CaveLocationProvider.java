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

import org.terasology.world.block.BlockRegion;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Produces;
import org.terasology.world.generation.Requires;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.LinkedList;
import java.util.List;

@RegisterPlugin
@Produces(CaveLocationFacet.class)
@Requires(@Facet(CaveFacet.class))
public class CaveLocationProvider implements FacetProviderPlugin {

    /**
     * <em>Note: </em>If no {@code FacetProvider} or {@code WorldRasterizer} calls
     * region.getFacet(<the provided facet class>) then this method 'process' will never be called
     * @param region
     */
    @Override
    public void process(GeneratingRegion region) {
        CaveFacet caveFacet = region.getRegionFacet(CaveFacet.class);
        CaveLocationFacet locationFacet =
                new CaveLocationFacet(region.getRegion(), region.getBorderForFacet(CaveLocationFacet.class));

        BlockRegion worldRegion = caveFacet.getWorldRegion();
        for (int x = worldRegion.minX(); x <= worldRegion.maxX(); ++x) {
            for (int z = worldRegion.minZ(); z <= worldRegion.maxZ(); ++z) {
                boolean insideCave = false;
                int y;
                // The cave locations in this x/z vertical
                List<CaveLocation> caveLocations = new LinkedList<>();
                CaveLocation currentLocation = null;
                // As we are looping from the top of the region to the bottom, the logic is as follows
                // - if insideCave == false && cavePresent == true, it is the start of a cave
                // --- insideCave = true, set currentLocation to a new location, ceiling = y+1
                // - if insideCave == true && cavePresent == true, still inside cave, do nothing
                // - if insideCave == false && cavePresent == false, in between caves, do nothing
                // - if insideCave == true && cavePresent == false, found cave floor
                // --- currentLocation floor = y, cavesLocations add current, current = null, insideCave = false
                for (y = worldRegion.maxY(); y >= worldRegion.minY(); --y) {
                    boolean cavePresent = caveFacet.getWorld(x, y, z);

                    if (!insideCave && cavePresent) {
                        insideCave = true;
                        currentLocation = new CaveLocation();
                        if (y < worldRegion.maxY()) {
                            currentLocation.ceiling = y + 1;
                            // In the case where y == worldRegion.maxY, then ceiling will remain float.NaN (unknown)
                        }
                    } else if (insideCave && !cavePresent) {
                        currentLocation.floor = y;
                        caveLocations.add(currentLocation);
                        currentLocation = null;
                        insideCave = false;
                    }
                }

                // If no locations were found then the facet will contain an empty array
                locationFacet.setWorld(x, z, caveLocations.toArray(new CaveLocation[0]));
            }
        }
        region.setRegionFacet(CaveLocationFacet.class, locationFacet);
    }
}
