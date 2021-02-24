// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0

package org.terasology.caves;

import com.google.common.collect.Sets;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.terasology.world.generation.Facet;
import org.terasology.world.generation.FacetBorder;
import org.terasology.world.generation.FacetProviderPlugin;
import org.terasology.world.generation.GeneratingRegion;
import org.terasology.world.generation.Requires;
import org.terasology.world.generation.Updates;
import org.terasology.world.generation.facets.DensityFacet;
import org.terasology.world.generation.facets.SeaLevelFacet;
import org.terasology.world.generation.facets.SurfacesFacet;
import org.terasology.world.generator.plugin.RegisterPlugin;

import java.util.Set;

/**
 * Removes the surfaces where they're broken by caves, moving them to the exposed
 * cave floor instead. Also remove the caves above the surface and breaking the
 * sea-floor.
 */
@RegisterPlugin
@Updates({
    @Facet(value = SurfacesFacet.class, border = @FacetBorder(sides = CaveToSurfaceProvider.SURFACE_SPREAD, bottom = CaveToSurfaceProvider.SURFACE_SPREAD, top = 20)),
    @Facet(value = CaveFacet.class, border = @FacetBorder(sides = CaveToSurfaceProvider.SURFACE_SPREAD, bottom = CaveToSurfaceProvider.SURFACE_SPREAD, top = 20))
})
@Requires({
    @Facet(SeaLevelFacet.class),
    @Facet(value = DensityFacet.class, border = @FacetBorder(sides = CaveToSurfaceProvider.SURFACE_SPREAD, bottom = CaveToSurfaceProvider.SURFACE_SPREAD, top = 20))
})
public class CaveToSurfaceProvider implements FacetProviderPlugin {
    public static final int SURFACE_SPREAD = 3;

    @Override
    public void setSeed(long seed) {
    }

    @Override
    public void process(GeneratingRegion region) {
        CaveFacet caveFacet = region.getRegionFacet(CaveFacet.class);
        DensityFacet densityFacet = region.getRegionFacet(DensityFacet.class);
        SurfacesFacet surfacesFacet = region.getRegionFacet(SurfacesFacet.class);
        SeaLevelFacet seaLevel = region.getRegionFacet(SeaLevelFacet.class);

        Set<Vector3ic> cavePositions = Sets.newHashSetWithExpectedSize(caveFacet.getWorldRegion().getSizeX() * caveFacet.getWorldRegion().getSizeZ());

        for (Vector3ic pos : caveFacet.getWorldRegion()) {
            if (caveFacet.getWorld(pos) && densityFacet.getWorldRegion().contains(pos) && surfacesFacet.getWorldRegion().contains(pos)) {
                cavePositions.add(new Vector3i(pos));
            }
        }

        Vector3i belowPos = new Vector3i();
        // Ensure that the ocean can't immediately fall into a cave.
        for (Vector3ic pos : densityFacet.getWorldRegion()) {
            if (densityFacet.getWorld(pos) <= 0) {
                if (cavePositions.contains(pos)) {
                    cavePositions.remove(pos);
                    caveFacet.setWorld(pos, false);
                }
                if (pos.y() <= seaLevel.getSeaLevel() + 1) {
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            belowPos.set(pos.x() + x, pos.y() - 1, pos.z() + z);
                            if (cavePositions.contains(belowPos)) {
                                cavePositions.remove(belowPos);
                                caveFacet.setWorld(belowPos, false);
                            }
                        }
                    }
                }
            }
        }

        // Mark any cave floors exposed to the sky as surface.
        Set<Vector3i> newSurfaces = Sets.newHashSet();
        for (Vector3ic pos : cavePositions) {
            if (surfacesFacet.getWorld(pos)) {
                surfacesFacet.setWorld(pos, false);
                Vector3i newSurface = new Vector3i(pos);
                while (cavePositions.contains(newSurface)) {
                    newSurface.add(0, -1, 0);
                }
                if (newSurface.y >= surfacesFacet.getWorldRegion().minY()) {
                    newSurfaces.add(newSurface);
                    surfacesFacet.setWorld(newSurface, true);
                }
            }
        }

        Vector3i a1 = new Vector3i();
        Vector3i a2 = new Vector3i();
        Vector3i a3 = new Vector3i();
        Vector3i a4 = new Vector3i();


        Set<Vector3i> newerSurfaces = Sets.newHashSetWithExpectedSize(newSurfaces.size());
        // Mark cave floors near to those exposed to the sky as surface.
        for (int i = 0; i < SURFACE_SPREAD; i++) {
            newerSurfaces.clear();
            for (Vector3i surface : newSurfaces) {
                for (Vector3i adjacent : new Vector3i[]{
                        a1.set(surface).sub(1, 0, 0),
                        a2.set(surface).add(1, 0, 0),
                        a3.set(surface).sub(0, 0, 1),
                        a4.set(surface).add(0, 0, 1)
                }) {
                    while (!cavePositions.contains(adjacent) && densityFacet.getWorldRegion().contains(adjacent) && densityFacet.getWorld(adjacent) > 0) {
                        adjacent.add(0, 1, 0);
                    }
                    // Only continue if the selected position is actually in a cave, rather than on the surface or above the selected region.
                    if (cavePositions.contains(adjacent)) {
                        while (cavePositions.contains(adjacent)) {
                            adjacent.sub(0, 1, 0);
                        }
                        if (
                                surfacesFacet.getWorldRegion().contains(adjacent) &&
                                        densityFacet.getWorldRegion().contains(adjacent) &&
                                        densityFacet.getWorld(adjacent) > 0 &&
                                        !surfacesFacet.getWorld(adjacent)
                        ) {
                            surface.set(adjacent); // reuse vector from last set
                            newerSurfaces.add(surface);
                            surfacesFacet.setWorld(adjacent, true);
                        }
                    }
                }
            }
            Set<Vector3i> temp = newSurfaces;
            newSurfaces = newerSurfaces;
            newerSurfaces = temp;
        }
    }
}
