// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.caves;

import org.terasology.engine.math.Region3i;
import org.terasology.engine.world.generation.Facet;
import org.terasology.engine.world.generation.FacetProviderPlugin;
import org.terasology.engine.world.generation.GeneratingRegion;
import org.terasology.engine.world.generation.Produces;
import org.terasology.engine.world.generation.Requires;
import org.terasology.engine.world.generator.plugin.RegisterPlugin;

import java.util.LinkedList;
import java.util.List;

@RegisterPlugin
@Produces(CaveLocationFacet.class)
@Requires(@Facet(CaveFacet.class))
public class CaveLocationProvider implements FacetProviderPlugin {

    /**
     * <em>Note: </em>If no {@code FacetProvider} or {@code WorldRasterizer} calls
     * region.getFacet(<the provided facet class>) then this method 'process' will never be called
     *
     * @param region
     */
    @Override
    public void process(GeneratingRegion region) {
        CaveFacet caveFacet = region.getRegionFacet(CaveFacet.class);
        CaveLocationFacet locationFacet =
                new CaveLocationFacet(region.getRegion(), region.getBorderForFacet(CaveLocationFacet.class));

        Region3i worldRegion = caveFacet.getWorldRegion();
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
